package com.example.a2049.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.MutableContextWrapper
import android.os.Handler
import android.os.Looper
import android.view.ContextThemeWrapper
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdMobManager {
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
    const val MEDIUM_RECTANGLE_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    var isAdFree: Boolean = false

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    private var isRewardedLoading = false
    private var isInterstitialLoading = false

    var restartCount: Int = 0
    var homeNavCount: Int = 0
    var lastHomeAdTime: Long = 0L

    private fun runOnMainThread(action: () -> Unit) {
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                action()
            } else {
                Handler(Looper.getMainLooper()).post(action)
            }
        } catch (_: Exception) {
            try {
                action()
            } catch (_: Exception) {}
        }
    }

    fun loadRewardedAd(context: Context, onLoaded: (() -> Unit)? = null) {
        if (rewardedAd != null || isRewardedLoading) {
            if (rewardedAd != null) {
                onLoaded?.invoke()
            }
            return
        }
        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context.applicationContext,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewardedAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    loadRewardedAd(activity)
                    onRewardEarned()
                }
            }
            ad.show(activity, OnUserEarnedRewardListener {
                onRewardEarned()
            })
        } else {
            loadRewardedAd(activity) {
                val reloaded = rewardedAd
                if (reloaded != null) {
                    reloaded.show(activity, OnUserEarnedRewardListener {
                        onRewardEarned()
                    })
                } else {
                    onRewardEarned()
                }
            }
        }
    }

    private fun Context?.findActivity(): Activity? {
        if (this == null) return null
        var currentContext: Context? = this
        val visited = mutableSetOf<Context>()
        while (currentContext != null && visited.add(currentContext)) {
            if (currentContext is Activity) return currentContext
            currentContext = when (currentContext) {
                is ContextThemeWrapper -> currentContext.baseContext
                is MutableContextWrapper -> currentContext.baseContext
                is ContextWrapper -> currentContext.baseContext
                else -> null
            }
        }
        return null
    }

    fun loadInterstitialAd(context: Context, onLoaded: (() -> Unit)? = null) {
        if (isAdFree) return
        if (interstitialAd != null || isInterstitialLoading) {
            if (interstitialAd != null) {
                onLoaded?.invoke()
            }
            return
        }
        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context.applicationContext,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    onLoaded?.invoke()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    fun showInterstitial(
        context: Context?,
        onAdDismissed: (() -> Unit)? = null
    ) {
        if (isAdFree) {
            runOnMainThread { onAdDismissed?.invoke() }
            return
        }
        val activity = context.findActivity()
        if (activity == null) {
            runOnMainThread { onAdDismissed?.invoke() }
            return
        }
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    runOnMainThread { onAdDismissed?.invoke() }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    runOnMainThread { onAdDismissed?.invoke() }
                }
            }
            try {
                ad.show(activity)
            } catch (_: Exception) {
                interstitialAd = null
                loadInterstitialAd(activity)
                runOnMainThread { onAdDismissed?.invoke() }
            }
        } else {
            if (context != null) {
                loadInterstitialAd(context)
            }
            runOnMainThread { onAdDismissed?.invoke() }
        }
    }

    fun showInterstitialAd(
        activity: Activity,
        isAdFreeCheck: Boolean = isAdFree,
        onAdDismissed: (() -> Unit)? = null
    ) {
        if (isAdFreeCheck) {
            runOnMainThread { onAdDismissed?.invoke() }
            return
        }
        showInterstitial(activity, onAdDismissed)
    }

    fun onGameRestart(context: Context, onAdDismissed: (() -> Unit)? = null) {
        if (isAdFree) {
            runOnMainThread { onAdDismissed?.invoke() }
            return
        }
        restartCount++
        if (restartCount % 2 == 0) {
            showInterstitial(context, onAdDismissed)
        } else {
            runOnMainThread { onAdDismissed?.invoke() }
        }
    }

    fun onHomeNavigation(context: Context?, onAdDismissed: (() -> Unit)? = null) {
        if (isAdFree) {
            runOnMainThread { onAdDismissed?.invoke() }
            return
        }
        homeNavCount++
        if (homeNavCount % 4 == 0) {
            showInterstitial(context, onAdDismissed)
        } else {
            runOnMainThread { onAdDismissed?.invoke() }
        }
    }

    fun onBackNavigation(context: Context?, onAdDismissed: (() -> Unit)? = null) {
        onHomeNavigation(context, onAdDismissed)
    }
}
