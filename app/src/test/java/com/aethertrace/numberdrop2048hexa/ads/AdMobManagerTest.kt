package com.aethertrace.numberdrop2048hexa.ads

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AdMobManagerTest {

    @Before
    fun setUp() {
        AdMobManager.homeNavCount = 0
        AdMobManager.isAdFree = false
    }

    @Test
    fun testHomeNavigationAdFrequency() {
        var dismissCount = 0
        val onDismiss: () -> Unit = { dismissCount++ }

        // 1st call: homeNavCount becomes 1 (1 % 4 != 0) -> should dismiss immediately
        AdMobManager.onHomeNavigation(null, onDismiss)
        assertEquals(1, AdMobManager.homeNavCount)
        assertEquals(1, dismissCount)

        // 2nd call: homeNavCount becomes 2 (2 % 4 != 0) -> should dismiss immediately
        AdMobManager.onHomeNavigation(null, onDismiss)
        assertEquals(2, AdMobManager.homeNavCount)
        assertEquals(2, dismissCount)

        // 3rd call: homeNavCount becomes 3 (3 % 4 != 0) -> should dismiss immediately
        AdMobManager.onHomeNavigation(null, onDismiss)
        assertEquals(3, AdMobManager.homeNavCount)
        assertEquals(3, dismissCount)

        // 4th call: homeNavCount becomes 4 (4 % 4 == 0) -> attempts interstitial / dismisses
        AdMobManager.onHomeNavigation(null, onDismiss)
        assertEquals(4, AdMobManager.homeNavCount)
        assertEquals(4, dismissCount)
    }

    @Test
    fun testBackNavigationAdFrequency() {
        var dismissCount = 0
        val onDismiss: () -> Unit = { dismissCount++ }

        // 1st call: homeNavCount becomes 1
        AdMobManager.onBackNavigation(null, onDismiss)
        assertEquals(1, AdMobManager.homeNavCount)
        assertEquals(1, dismissCount)

        // 2nd call: homeNavCount becomes 2
        AdMobManager.onBackNavigation(null, onDismiss)
        assertEquals(2, AdMobManager.homeNavCount)
        assertEquals(2, dismissCount)

        // 3rd call: homeNavCount becomes 3
        AdMobManager.onBackNavigation(null, onDismiss)
        assertEquals(3, AdMobManager.homeNavCount)
        assertEquals(3, dismissCount)

        // 4th call: homeNavCount becomes 4
        AdMobManager.onBackNavigation(null, onDismiss)
        assertEquals(4, AdMobManager.homeNavCount)
        assertEquals(4, dismissCount)
    }
}
