package com.example.a2049.ui.model

data class FeedbackEvent(
    val id: Long,
    val text: String,
    val tier: Int
)

object FeedbackWords {
    val TIER_1 = listOf("Good", "Nice", "Sweet", "Cool", "Neat", "Smoooth!", "Nice Move!")
    val TIER_2 = listOf("Great!", "Awesome!", "Super!", "Brilliant!", "Perfect!", "Boom!")
    val TIER_3 = listOf("Epic!", "Legendary!", "Phenomenal!", "Majestic!", "Godlike!", "UNSTOPPABLE!")
    val TIER_4 = listOf("Combo!", "Double Merge!", "On Fire!", "Genius!", "Chain Reaction!", "Multi-Combo!")

    fun getRandomWordForTier(tier: Int): String {
        val list = when (tier) {
            1 -> TIER_1
            2 -> TIER_2
            3 -> TIER_3
            4 -> TIER_4
            else -> TIER_1
        }
        return list.random()
    }
}
