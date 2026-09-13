package com.game.a2048

object MissionCatalog {
    val missions: List<Mission> = listOf(
        // Easy Missions (M1 - M8)
        Mission(
            id = 1,
            title = "First Step",
            description = "Create a 32 Tile",
            type = MissionType.TILE_REACH,
            targetValue = 32,
            rewardStars = 1,
            category = MissionCategory.EASY
        ),
        Mission(
            id = 2,
            title = "Quick Start",
            description = "Create a 64 Tile",
            type = MissionType.TILE_REACH,
            targetValue = 64,
            rewardStars = 1,
            category = MissionCategory.EASY
        ),
        Mission(
            id = 3,
            title = "Double Trouble",
            description = "Perform a Double Merge in 1 move",
            type = MissionType.COMBO_MERGE,
            targetValue = 2,
            rewardStars = 1,
            category = MissionCategory.EASY
        ),
        Mission(
            id = 4,
            title = "Point Collector",
            description = "Score 500 Points",
            type = MissionType.SCORE_REACH,
            targetValue = 500,
            rewardStars = 1,
            category = MissionCategory.EASY
        ),
        Mission(
            id = 5,
            title = "Speedy 128",
            description = "Create a 128 Tile in 35 moves or less",
            type = MissionType.MOVE_LIMIT_TILE,
            targetValue = 128,
            moveLimit = 35,
            rewardStars = 1,
            category = MissionCategory.EASY
        ),
        Mission(
            id = 6,
            title = "Combo Maker",
            description = "Perform a 2-Combo Merge in 1 move",
            type = MissionType.COMBO_MERGE,
            targetValue = 2,
            rewardStars = 1,
            category = MissionCategory.EASY
        ),
        Mission(
            id = 7,
            title = "Thousand Club",
            description = "Score 1,000 Points",
            type = MissionType.SCORE_REACH,
            targetValue = 1000,
            rewardStars = 1,
            category = MissionCategory.EASY
        ),
        Mission(
            id = 8,
            title = "Halfway There",
            description = "Reach 256 Tile",
            type = MissionType.TILE_REACH,
            targetValue = 256,
            rewardStars = 1,
            category = MissionCategory.EASY
        ),

        // Medium Missions (M9 - M17)
        Mission(
            id = 9,
            title = "Express 128",
            description = "Create a 128 Tile in 25 moves or less",
            type = MissionType.MOVE_LIMIT_TILE,
            targetValue = 128,
            moveLimit = 25,
            rewardStars = 2,
            category = MissionCategory.MEDIUM
        ),
        Mission(
            id = 10,
            title = "Triple Combo",
            description = "Perform a 3-Combo Merge in 1 move",
            type = MissionType.COMBO_MERGE,
            targetValue = 3,
            rewardStars = 2,
            category = MissionCategory.MEDIUM
        ),
        Mission(
            id = 11,
            title = "High Scorer",
            description = "Score 2,500 Points",
            type = MissionType.SCORE_REACH,
            targetValue = 2500,
            rewardStars = 2,
            category = MissionCategory.MEDIUM
        ),
        Mission(
            id = 12,
            title = "Quarter Mark",
            description = "Reach 512 Tile",
            type = MissionType.TILE_REACH,
            targetValue = 512,
            rewardStars = 2,
            category = MissionCategory.MEDIUM
        ),
        Mission(
            id = 13,
            title = "Swift 256",
            description = "Create a 256 Tile in 50 moves or less",
            type = MissionType.MOVE_LIMIT_TILE,
            targetValue = 256,
            moveLimit = 50,
            rewardStars = 2,
            category = MissionCategory.MEDIUM
        ),
        Mission(
            id = 14,
            title = "Score Master",
            description = "Score 5,000 Points",
            type = MissionType.SCORE_REACH,
            targetValue = 5000,
            rewardStars = 2,
            category = MissionCategory.MEDIUM
        ),
        Mission(
            id = 15,
            title = "Multi-Merge",
            description = "Perform a 4-Combo Merge in 1 move",
            type = MissionType.COMBO_MERGE,
            targetValue = 4,
            rewardStars = 2,
            category = MissionCategory.MEDIUM
        ),
        Mission(
            id = 16,
            title = "Tactical 512",
            description = "Reach 512 Tile in 70 moves or less",
            type = MissionType.MOVE_LIMIT_TILE,
            targetValue = 512,
            moveLimit = 70,
            rewardStars = 2,
            category = MissionCategory.MEDIUM
        ),
        Mission(
            id = 17,
            title = "Kilotile",
            description = "Reach 1024 Tile",
            type = MissionType.TILE_REACH,
            targetValue = 1024,
            rewardStars = 2,
            category = MissionCategory.MEDIUM
        ),

        // Normal Missions (M18 - M25)
        Mission(
            id = 18,
            title = "Five Figures",
            description = "Score 10,000 Points",
            type = MissionType.SCORE_REACH,
            targetValue = 10000,
            rewardStars = 3,
            category = MissionCategory.NORMAL
        ),
        Mission(
            id = 19,
            title = "Fast 512",
            description = "Reach 512 Tile in 60 moves or less",
            type = MissionType.MOVE_LIMIT_TILE,
            targetValue = 512,
            moveLimit = 60,
            rewardStars = 3,
            category = MissionCategory.NORMAL
        ),
        Mission(
            id = 20,
            title = "Mega Combo",
            description = "Perform a 5-Combo Merge in 1 move",
            type = MissionType.COMBO_MERGE,
            targetValue = 5,
            rewardStars = 3,
            category = MissionCategory.NORMAL
        ),
        Mission(
            id = 21,
            title = "Efficiency Expert",
            description = "Reach 1024 Tile in 100 moves or less",
            type = MissionType.MOVE_LIMIT_TILE,
            targetValue = 1024,
            moveLimit = 100,
            rewardStars = 3,
            category = MissionCategory.NORMAL
        ),
        Mission(
            id = 22,
            title = "Point Legend",
            description = "Score 15,000 Points",
            type = MissionType.SCORE_REACH,
            targetValue = 15000,
            rewardStars = 3,
            category = MissionCategory.NORMAL
        ),
        Mission(
            id = 23,
            title = "Grand Master",
            description = "Reach 2048 Tile",
            type = MissionType.TILE_REACH,
            targetValue = 2048,
            rewardStars = 3,
            category = MissionCategory.NORMAL
        ),
        Mission(
            id = 24,
            title = "Speedy 2048",
            description = "Reach 2048 Tile in 200 moves or less",
            type = MissionType.MOVE_LIMIT_TILE,
            targetValue = 2048,
            moveLimit = 200,
            rewardStars = 3,
            category = MissionCategory.NORMAL
        ),
        Mission(
            id = 25,
            title = "Beyond 2048",
            description = "Reach 4096 Tile",
            type = MissionType.TILE_REACH,
            targetValue = 4096,
            rewardStars = 3,
            category = MissionCategory.NORMAL
        )
    )

    fun getMissionById(id: Int?): Mission? {
        if (id == null) return null
        return missions.find { it.id == id }
    }

    fun getMissionsByCategory(category: MissionCategory): List<Mission> {
        return missions.filter { it.category == category }
    }

    fun getNextMission(currentId: Int): Mission? {
        val currentIndex = missions.indexOfFirst { it.id == currentId }
        if (currentIndex != -1 && currentIndex + 1 < missions.size) {
            return missions[currentIndex + 1]
        }
        return null
    }
}
