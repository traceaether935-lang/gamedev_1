package com.example.a2049.ads

import android.app.Activity
import android.content.Context
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

    private var rewardedAd: RewardedAd? = null
    private var interstitialAd: InterstitialAd? = null

    private var isRewardedLoading = false
    private var isInterstitialLoading = false

    var restartCount: Int = 0
    var lastHomeAdTime: Long = 0L

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
            // If rewarded ad wasn't preloaded, attempt load or invoke reward directly for smooth experience
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

    fun loadInterstitialAd(context: Context, onLoaded: (() -> Unit)? = null) {
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

    fun showInterstitialAd(activity: Activity, onAdDismissed: (() -> Unit)? = null) {
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitialAd(activity)
                    onAdDismissed?.invoke()
                }
            }
            ad.show(activity)
        } else {
            loadInterstitialAd(activity)
            onAdDismissed?.invoke()
        }
    }

    fun onGameRestart(activity: Activity, onAdDismissed: (() -> Unit)? = null) {
        restartCount++
        if (restartCount % 2 == 0) {
            showInterstitialAd(activity, onAdDismissed)
        } else {
            onAdDismissed?.invoke()
        }
    }

    fun onHomeNavigation(activity: Activity, onAdDismissed: (() -> Unit)? = null) {
        val now = System.currentTimeMillis()
        if (now - lastHomeAdTime >= 7 * 60 * 1000L) {
            lastHomeAdTime = now
            showInterstitialAd(activity, onAdDismissed)
        } else {
            onAdDismissed?.invoke()
        }
    }
}
