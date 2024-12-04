package citadels.project.game

import androidx.compose.ui.graphics.Color

/**
 * 代表城堡游戏中的区域卡片。
 */
data class District(
    val name: String="DEFAULT", // 区域名称
    val type: DistrictType=DistrictType.NOBLE, // 区域类型
    val cost: Int=-1, // 建造成本
    val points: Int=-1 // 为拥有者提供的分数
){
    override fun toString(): String {
        // 设置每个属性的显示宽度
        val nameWidth = 5
        val typeWidth = 5
        val costWidth = 3


        // 格式化每个属性以保持对齐
        val formattedName = name.padEnd(nameWidth)
        val formattedType = type.toString().padEnd(typeWidth)
        val formattedCost = cost.toString().padStart(costWidth)


        // 构建并返回最终的字符串
        return "$formattedName, $formattedType, $formattedCost¥"
    }

    fun toShortSting():String{
        // 设置每个属性的显示宽度
        val nameWidth = 9

        val costWidth = 2


        // 格式化每个属性以保持对齐
        val formattedName = name.padEnd(nameWidth)

        val formattedCost = cost.toString().padStart(costWidth)


        // 构建并返回最终的字符串
        return "$formattedName $formattedCost¥"
    }
}

/**
 * 区域类型枚举，代表不同种类的区域。
 */
enum class DistrictType {
    TRADE { // 贸易区域
        override val color= Color.Green
    },
    RELIGIOUS {
        override val color= Color.Blue
    },// 宗教区域
    MILITARY{
        override val color= Color.Red
    }, // 军事区域
    NOBLE{
        override val color= Color.Yellow
    }, // 贵族区域
    // 更多类型可以根据游戏规则添加
    ;
    abstract val color:Color
}