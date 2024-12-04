package citadels.project.game

fun getNumberOfCardsSetAside(playerCount: Int): Pair<Int, Int> {
    return when (playerCount) {
        in 2..3 -> Pair(0, 0)  // 特殊规则，具体卡片数视版本而定
        4 -> Pair(2, 1)  // 两张面朝上，一张面朝下
        5 -> Pair(1, 1)  // 一张面朝上，一张面朝下
        in 6..7 -> Pair(0, 1)  // 一张面朝下
        else -> Pair(0, 0)  // 对于其他玩家数量，返回默认值
    }
}

// 函数返回一个 Pair，第一个值为面朝上的卡片数，第二个值为面朝下的卡片数
