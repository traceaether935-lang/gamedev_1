package com.example.a2049.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.a2049.billing.BillingManager
import com.example.a2049.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    billingManager: BillingManager?,
    gameViewModel: GameViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val isAdFree by gameViewModel.isAdFree.collectAsState()
    val hammerUses by gameViewModel.hammerUses.collectAsState()
    val switchUses by gameViewModel.switchUses.collectAsState()
    val undoUses by gameViewModel.undoUses.collectAsState()

    val productDetailsMap by billingManager?.productDetailsMap?.collectAsState()
        ?: remember { mutableStateOf(emptyMap()) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.ShoppingBag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Item Store",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Remove Ads Non-Consumable
            val removeAdsDetails = productDetailsMap[BillingManager.SKU_REMOVE_ADS]
            val removeAdsPrice = removeAdsDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$0.99"

            StoreItemCard(
                title = "Remove Ads",
                description = "Enjoy uninterrupted gameplay without any ads",
                icon = Icons.Rounded.Block,
                isOwned = isAdFree,
                buttonText = if (isAdFree) "Purchased" else "Buy ($removeAdsPrice)",
                onBuyClick = {
                    if (!isAdFree) {
                        if (activity != null && billingManager != null) {
                            billingManager.launchBillingFlow(activity, BillingManager.SKU_REMOVE_ADS)
                        } else {
                            gameViewModel.setAdFree(true)
                            Toast.makeText(context, "Ads removed! (Test Mode)", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            // Hammer Pack Consumable (+5)
            val hammerDetails = productDetailsMap[BillingManager.SKU_BUY_HAMMER_PACK]
            val hammerPrice = hammerDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$0.99"

            StoreItemCard(
                title = "Hammer Pack (+5 Uses)",
                description = "Smash any unwanted tile from the board. Current: $hammerUses uses",
                icon = Icons.Rounded.Build,
                isOwned = false,
                buttonText = "Buy 5 Hammers ($hammerPrice)",
                onBuyClick = {
                    if (activity != null && billingManager != null) {
                        billingManager.launchBillingFlow(activity, BillingManager.SKU_BUY_HAMMER_PACK)
                    } else {
                        gameViewModel.onConsumablePurchased(BillingManager.SKU_BUY_HAMMER_PACK)
                        Toast.makeText(context, "+5 Hammers granted!", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Switch Pack Consumable (+5)
            val switchDetails = productDetailsMap[BillingManager.SKU_BUY_SWITCH_PACK]
            val switchPrice = switchDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$0.99"

            StoreItemCard(
                title = "Switch Pack (+5 Uses)",
                description = "Swap any two adjacent tiles on the board. Current: $switchUses uses",
                icon = Icons.Rounded.SwapHoriz,
                isOwned = false,
                buttonText = "Buy 5 Switches ($switchPrice)",
                onBuyClick = {
                    if (activity != null && billingManager != null) {
                        billingManager.launchBillingFlow(activity, BillingManager.SKU_BUY_SWITCH_PACK)
                    } else {
                        gameViewModel.onConsumablePurchased(BillingManager.SKU_BUY_SWITCH_PACK)
                        Toast.makeText(context, "+5 Switches granted!", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            // Undo Pack Consumable (+5)
            val undoDetails = productDetailsMap[BillingManager.SKU_BUY_UNDO_PACK]
            val undoPrice = undoDetails?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$0.99"

            StoreItemCard(
                title = "Undo Pack (+5 Uses)",
                description = "Rewind your last move and try again. Current: $undoUses uses",
                icon = Icons.AutoMirrored.Rounded.Undo,
                isOwned = false,
                buttonText = "Buy 5 Undos ($undoPrice)",
                onBuyClick = {
                    if (activity != null && billingManager != null) {
                        billingManager.launchBillingFlow(activity, BillingManager.SKU_BUY_UNDO_PACK)
                    } else {
                        gameViewModel.onConsumablePurchased(BillingManager.SKU_BUY_UNDO_PACK)
                        Toast.makeText(context, "+5 Undos granted!", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Restore Purchases Button
            OutlinedButton(
                onClick = {
                    if (billingManager != null) {
                        billingManager.restorePurchases { success ->
                            if (success) {
                                Toast.makeText(context, "Purchases restored successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "No prior purchases found or failed to restore.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(context, "Purchases restored!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Restore Purchases",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StoreItemCard(
    title: String,
    description: String,
    icon: ImageVector,
    isOwned: Boolean,
    buttonText: String,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Button(
                onClick = onBuyClick,
                enabled = !isOwned,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (isOwned) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = buttonText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
