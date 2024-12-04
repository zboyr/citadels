package citadels.project.game

fun <T> shiftList(list: List<T>, n: Int): List<T> {
    val size = list.size
    if (size == 0) return list

    // 将移位数转换为正值，并确保它在0到size-1之间
    val shift = ((n % size) + size) % size
    if (shift == 0) return list

    // 根据计算出的正向移位数重新组合列表
    return list.drop(size - shift) + list.take(size - shift)
}
