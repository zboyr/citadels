package citadels.project.game

import citadels.project.User

data class Player(
    val user: User=User(),
    var gold: Int = 0, // 玩家金币数量
    val districts: MutableList<District> = mutableListOf(), // 玩家建造的区域
    var handCards: MutableList<District> = mutableListOf(),
    var character: CharacterType = CharacterType.UNSELECTED,// 玩家当前选择的角色
    var haveCrown: Boolean =false,
    var assassinated: Boolean=false,
    var stolen:Boolean=false,
    var tookAction:Boolean=false,
    var usedSkill:Boolean=false,
    var buildInTurn:Int=0,
) {
    
    fun reset(){
         gold = 0 // 玩家金币数量
         districts.clear()// 玩家建造的区域
        handCards.clear()
         character= CharacterType.UNSELECTED // 玩家当前选择的角色
         haveCrown =false
         assassinated=false
         stolen=false
         tookAction=false
         usedSkill=false
        buildInTurn=0
    }




    // 添加区域到玩家的城市中
    fun addToHand(district: District) {
        handCards.add(district)
        // 可以在这里更新分数或进行其他逻辑处理
    }

    fun getDistrictAmount(districtType:DistrictType):Int{
        return districts.filter { it.type==districtType }.size
    }


    // 移除区域（如果玩家失去某个区域）
    fun removeDistrict(district: District) {
        districts.remove(district)
        // 同样，更新分数或其他逻辑
    }

    fun buildAble(district: District): Boolean {
        return gold>=district.cost&&buildInTurn<1
    }

//    fun build(district: District){
//        handCards.remove(district)
//        districts.add(district)
//        gold-=district.cost
//    }

    fun turnReset(){
        assassinated=false
        stolen=false
        tookAction=false
        usedSkill=false
        buildInTurn=0
        character=CharacterType.UNSELECTED
    }

    // 更新玩家的金币数量
    fun updateGold(amount: Int) {
        gold += amount
        // 确保金币数量不会变成负数
        if (gold < 0) gold = 0
    }

    fun gainGold(amount: Int){
        gold+=amount
    }

    fun payGold(amount: Int):Boolean{
        return if (gold<amount){
            false
        }else{
            gold-=amount
            true
        }
    }

    override fun toString(): String {

        return "${user.name}(🃏:${handCards.size}|🏠:${districts.size})"
    }

    // 选择角色


    // 计算玩家的总分





}



enum class Action{

    GETMONEY,
    DRAWCARDS,
    CANCEL
}