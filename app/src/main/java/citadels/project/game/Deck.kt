package citadels.project.game

// Deck.kt


import android.util.Log
import kotlin.random.Random

/**
 * 代表游戏牌堆的类。
 */
data class Deck( var drawPile: MutableList<District> = mutableListOf()) {

     val discards: MutableList<District> = mutableListOf()



    fun  generateCards(){
        // 定义每种类型的地区名字
        val militaryNames = listOf("Fortress", "Barracks", "Armory", "Watchtower", "Castle", "Camp", "Garrison", "Outpost", "Bunker", "Citadel")
        val religiousNames = listOf("Temple", "Church", "Monastery", "Shrine", "Chapel", "Mosque", "Cathedral", "Sanctuary", "Abbey", "Convent")
        val tradeNames = listOf("Market", "Harbor", "Shop", "Warehouse", "Post", "Bazaar", "Emporium", "Exchange", "Mart", "Plaza")
        val nobleNames = listOf("Palace", "Mansion", "Villa", "Estate", "Manor", "Residence", "Chateau", "Hall", "Lodge", "Court")

        // 为每种类型生成牌
        for (name in militaryNames) {
            val value = Random.nextInt(1, 7)
            drawPile.add(District(name, DistrictType.MILITARY, value, value))
            drawPile.add(District(name, DistrictType.MILITARY, value, value))
        }
        for (name in religiousNames) {
            val value = Random.nextInt(1, 7)
            drawPile.add(District(name, DistrictType.RELIGIOUS, value, value))
            drawPile.add(District(name, DistrictType.RELIGIOUS, value, value))
        }
        for (name in tradeNames) {
            val value = Random.nextInt(1, 7)
            drawPile.add(District(name, DistrictType.TRADE, value, value))
            drawPile.add(District(name, DistrictType.TRADE, value, value))
        }
        for (name in nobleNames) {
            val value = Random.nextInt(1, 7)
            drawPile.add(District(name, DistrictType.NOBLE, value, value))
            drawPile.add(District(name, DistrictType.NOBLE, value, value))
        }
    }

    /**
     * 洗牌，随机打乱牌堆中的卡片顺序。
     */
    fun shuffle() {
        drawPile.shuffle()
    }

    /**
     * 抽取一张卡片。
     * @return 返回牌堆顶部的卡片，如果牌堆为空，则重新洗牌并返回抽牌。
     */
    fun draw(): District {
        Log.d("Deck","Draw Card:$drawPile")
        if (drawPile.isEmpty()) {
            discards.shuffle()
            drawPile = discards
            discards.clear()
        }
        return drawPile.removeFirst()
    }

    fun disCard(district: District){
        discards.add(district)
    }

}





