package com.example.game

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-2218749993435333/8632016051"
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-2218749993435333/5101493837"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    var isInterstitialLoading = false
    var isRewardedLoading = false

    fun loadInterstitialAd(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return
        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                }
            }
        )
    }

    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        val ad = interstitialAd
        if (ad != null) {
            interstitialAd = null
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadInterstitialAd(activity) // Load the next one
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    onAdDismissed()
                }
            }
            ad.show(activity)
        } else {
            onAdDismissed()
        }
    }

    fun loadRewardedAd(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return
        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                }
            }
        )
    }

    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit, onAdFailedOrDismissed: () -> Unit) {
        val ad = rewardedAd
        if (ad != null) {
            rewardedAd = null
            var rewardEarned = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    loadRewardedAd(activity)
                    if (!rewardEarned) {
                        onAdFailedOrDismissed()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    onAdFailedOrDismissed()
                }
            }
            ad.show(activity) {
                rewardEarned = true
                onRewardEarned()
            }
        } else {
            onAdFailedOrDismissed()
        }
    }
}
