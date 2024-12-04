package citadels.project.game

import android.util.Log
import citadels.project.R
import citadels.project.RoomManager
import kotlinx.coroutines.launch

/**
 * 代表城堡游戏中的角色卡片。
 */


/**
 * 角色类型枚举，代表游戏中的不同角色。
 */
enum class CharacterType {
    UNSELECTED{

        override fun skill(gameEngine: GameEngine, self: Player) {
            Log.e("Characters"," NOTSELECT use skill")
        }

        override val iconID= R.drawable.question_svgrepo_com
    },
    ASSASSIN{
        override fun skill(gameEngine: GameEngine, self: Player) {

            gameEngine.coroutineScope.launch {
                val character=gameEngine.characterTypeDialogFactory.selectNumber(CharacterType.values().drop(2))
                Log.d("Characters","ASSASSINATE: ${character}")
                if (character!=UNSELECTED){
                    gameEngine.assassinate(character)
                    self.usedSkill=true
                    RoomManager.syncData(gameEngine)
                }


            }
        }

        override val iconID= R.drawable.agent_halloween_japanese_man_ninja_svgrepo_com
            }, // 刺客
    THIEF{
        override fun skill(gameEngine: GameEngine, self: Player) {
            gameEngine.coroutineScope.launch {
                val character=gameEngine.characterTypeDialogFactory.selectNumber(CharacterType.values().drop(3))
                Log.d("Characters","stole: ${character}")
                gameEngine.stole(character)
                if (character!=UNSELECTED){
                    gameEngine.stole(character)
                    self.usedSkill=true
                    RoomManager.syncData(gameEngine)
                }
            }
        }

        override val iconID= R.drawable.thief_svgrepo_com
    }, // 盗贼
    MAGICIAN{
        override fun skill(gameEngine: GameEngine, self: Player) {
            gameEngine.coroutineScope.launch {

                val swapCardPlayer =
                    gameEngine.playerDialogFactory.selectNumber(gameEngine.players.dropWhile { it == self })

                Log.d("Characters","swap card with: $swapCardPlayer")
                if (swapCardPlayer!=Player()){
                    self.usedSkill=true
                    self.handCards=swapCardPlayer.handCards.also {swapCardPlayer.handCards= self.handCards }
                    RoomManager.syncData(gameEngine)
                }

            }

        }
        override val iconID= R.drawable.crystal_ball_magic_magician_witch_svgrepo_com
    }, // 魔术师
    KING{
        override fun skill(gameEngine: GameEngine, self: Player) {
            gameEngine.apply {
                self.usedSkill=true
                players.forEach{
                    if (it.haveCrown){
                        it.haveCrown=false
                    }
                }
                self.haveCrown=true

                self.gold+=self.getDistrictAmount(DistrictType.NOBLE)
                sync()
            }

        }
        override val iconID= R.drawable.bride_dead_ghost_halloween_horror_svgrepo_com
    }, // 国王
    BISHOP{
        override fun skill(gameEngine: GameEngine, self: Player) {
            gameEngine.apply {
                self.usedSkill=true
                self.gold+=self.getDistrictAmount(DistrictType.RELIGIOUS)
                sync()
            }
        }
        override val iconID= R.drawable.papacy_bishop_svgrepo_com
    }, // 主教
    MERCHANT{
        override fun skill(gameEngine: GameEngine, self: Player) {
            gameEngine.apply {
                self.usedSkill=true
                self.gold+=self.getDistrictAmount(DistrictType.TRADE)+1
                sync()
            }
        }
        override val iconID= R.drawable.businessman_svgrepo_com
    }, // 商人
    ARCHITECT{
        override fun skill(gameEngine: GameEngine, self: Player) {
            gameEngine.apply {
                self.usedSkill=true
                drawCard(self)
                drawCard(self)
                drawCard(self)
                self.buildInTurn-=2
                sync()
            }
        }
        override val iconID= R.drawable.architect_svgrepo_com
    }, // 建筑师
    WARLORD{
        override fun skill(gameEngine: GameEngine, self: Player) {
            gameEngine.apply {
                coroutineScope.launch {
                    val deconstructPlayer =
                        playerDialogFactory.selectNumber(gameEngine.players.dropWhile { it == self })

                    Log.d("Characters","Deconstruct: $deconstructPlayer")
                    if (deconstructPlayer!=Player()){
                        val deconstructDistrict=districtDialogFactory.selectNumber(deconstructPlayer.districts.filter { it.cost-1<=self.gold })
                        if (deconstructDistrict!=District()){
                            self.usedSkill=true
                            deconstructPlayer.districts.remove(deconstructDistrict)
                            self.gold-=deconstructDistrict.cost-1
                        }
                    }

                    RoomManager.syncData(gameEngine)

                }

            }
        }
        override val iconID= R.drawable.medieval_steel_warrior_armor_knight_svgrepo_com
    },// 战争领主
    ;
    abstract fun skill(gameEngine: GameEngine,self:Player)
    abstract val iconID:Int

    // 更多角色可以根据游戏的扩展包添加
}






