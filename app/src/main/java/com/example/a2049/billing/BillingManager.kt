package com.example.a2049.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    private val context: Context,
    private val onRemoveAdsPurchased: () -> Unit,
    private val onConsumablePurchased: (productId: String) -> Unit
) : PurchasesUpdatedListener {

    companion object {
        const val SKU_REMOVE_ADS = "sku_remove_ads"
        const val SKU_BUY_HAMMER_PACK = "sku_buy_hammer_pack"
        const val SKU_BUY_SWITCH_PACK = "sku_buy_switch_pack"
        const val SKU_BUY_UNDO_PACK = "sku_buy_undo_pack"

        val ALL_SKUS = listOf(
            SKU_REMOVE_ADS,
            SKU_BUY_HAMMER_PACK,
            SKU_BUY_SWITCH_PACK,
            SKU_BUY_UNDO_PACK
        )
    }

    private var billingClient: BillingClient? = null

    private val _productDetailsMap = MutableStateFlow<Map<String, ProductDetails>>(emptyMap())
    val productDetailsMap: StateFlow<Map<String, ProductDetails>> = _productDetailsMap.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        initBillingClient()
    }

    private fun initBillingClient() {
        try {
            val pendingParams = PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()

            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(pendingParams)
                .build()

            startConnection()
        } catch (_: Exception) {}
    }

    fun startConnection(onConnected: (() -> Unit)? = null) {
        val client = billingClient ?: return
        if (client.isReady) {
            _isConnected.value = true
            queryProductDetails()
            queryPurchases()
            onConnected?.invoke()
            return
        }

        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isConnected.value = true
                    queryProductDetails()
                    queryPurchases()
                    onConnected?.invoke()
                } else {
                    _isConnected.value = false
                }
            }

            override fun onBillingServiceDisconnected() {
                _isConnected.value = false
            }
        })
    }

    fun queryProductDetails() {
        val client = billingClient ?: return
        if (!client.isReady) return

        val productList = ALL_SKUS.map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val map = productDetailsList.associateBy { it.productId }
                _productDetailsMap.value = map
            }
        }
    }

    fun launchBillingFlow(activity: Activity, productId: String) {
        val client = billingClient ?: return
        if (!client.isReady) {
            startConnection {
                launchBillingFlow(activity, productId)
            }
            return
        }

        val details = _productDetailsMap.value[productId] ?: return
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        client.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            for (productId in purchase.products) {
                when (productId) {
                    SKU_REMOVE_ADS -> {
                        if (!purchase.isAcknowledged) {
                            val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                            billingClient?.acknowledgePurchase(acknowledgeParams) { result ->
                                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                    onRemoveAdsPurchased()
                                }
                            }
                        } else {
                            onRemoveAdsPurchased()
                        }
                    }
                    SKU_BUY_HAMMER_PACK,
                    SKU_BUY_SWITCH_PACK,
                    SKU_BUY_UNDO_PACK -> {
                        val consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient?.consumeAsync(consumeParams) { result, _ ->
                            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                                onConsumablePurchased(productId)
                            }
                        }
                    }
                }
            }
        }
    }

    fun queryPurchases() {
        restorePurchases()
    }

    fun restorePurchases(onComplete: ((Boolean) -> Unit)? = null) {
        val client = billingClient
        if (client == null || !client.isReady) {
            startConnection {
                restorePurchases(onComplete)
            }
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
                onComplete?.invoke(true)
            } else {
                onComplete?.invoke(false)
            }
        }
    }
}
