package citadels.project.game

import android.util.Log
import citadels.project.DialogFactory
import citadels.project.RoomManager
import com.google.firebase.firestore.Exclude
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 游戏引擎负责控制游戏逻辑和游戏状态的核心类。
 */

data class GameEngine(
    var roomID:String="DefaultID",


    val players: List<Player> = listOf(), // 游戏中的玩家列表

    var deck: Deck=Deck(), // 游戏中使用的牌堆


    @get:Exclude var characterTypeDialogFactory: DialogFactory<CharacterType> =DialogFactory(CharacterType.UNSELECTED,"Choose Character"),
    @get:Exclude var actionDialogFactory: DialogFactory<Action> =DialogFactory(Action.CANCEL,"Choose Action"),
    @get:Exclude var districtDialogFactory: DialogFactory<District> =DialogFactory(District(),"Choose Card You want"),
    @get:Exclude var playerDialogFactory: DialogFactory<Player> =DialogFactory(Player(),"Choose Player You want"),


    ) {

    @get:Exclude val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    var currentPlayerIndex: Int = 0// 当前行动的玩家索引
    var lastRound:Boolean=false

    var remainingCharacterList= CharacterType.values().drop(1).toMutableList()

    var gameStatus=GameStatus.ChooseCharacter

    val initialGoldAmount = 2

    var isGameRunning: Boolean = false
        // 只允许内部修改游戏运行状态
    
    val disposeCharacterMap= mapOf(Pair(CharacterType.UNSELECTED.name,false)).toMutableMap()

    var firstEightPlayer=Player()

    var winner=Player()

    fun drawCard(player: Player){
        player.handCards.add(deck.draw())
    }



    fun action(player: Player){
        coroutineScope.launch {
            val action =actionDialogFactory.selectNumber(Action.values().toList())
            if (action==Action.GETMONEY){
                player.gold+=2
                player.tookAction=true
            }else if (action==Action.DRAWCARDS){
                val card0=deck.draw()
                val card1=deck.draw()
                val selection=districtDialogFactory.selectNumber(listOf(card0,card1) )
                player.handCards.add(selection)
                deck.drawPile.add(if(selection==card0)card1 else card0)
                player.tookAction=true
            }
            RoomManager.syncData(this@GameEngine)
        }
    }

    fun chooseCharacter(player: Player){
            val thisEngine=this

            coroutineScope.launch {
                var characterSelection=CharacterType.UNSELECTED
                while (characterSelection==CharacterType.UNSELECTED){
                    characterSelection =
                        characterTypeDialogFactory.selectNumber(remainingCharacterList)
                }

                player.character = characterSelection
                remainingCharacterList.remove(characterSelection)
                moveToNextPlayer()
                if (players[currentPlayerIndex].character != CharacterType.UNSELECTED) {
                    GameStatus.TakeAction.start(thisEngine)

                }
                RoomManager.syncData(thisEngine)
            }

    }

    fun getScore(player: Player): Int {
        var districtScore=0
        player.districts.forEach{
            districtScore+=it.cost
        }

        if (DistrictType.values().all { district ->
                player.districts.any{
                    it.type == district
                }
            }
            ){
            districtScore+=4
        }

        if (player.districts.size>=8){
            districtScore+=4
        }

        if (firstEightPlayer==player){
            districtScore+=4
        }

        return districtScore
    }


    fun build(player: Player,district: District){
        player.handCards.remove(district)
        player.districts.add(district)
        player.gold-=district.cost
        player.buildInTurn+=1
        if (firstEightPlayer==Player()&&player.districts.size>=8){
            firstEightPlayer=player
            lastRound=true
        }
        sync()
    }

    fun giveNextPlayerChooseCharacter(player: Player){

    }

    fun moveToNextPlayer(){
        if (players.lastIndex==currentPlayerIndex){
            currentPlayerIndex=0
        }else{
            currentPlayerIndex++
        }
    }




    fun cheat(player: Player){
        player.tookAction=false
        player.usedSkill=false
        player.gold+=100
        val cheatDeck=Deck()
        cheatDeck.generateCards()
        player.handCards.addAll(cheatDeck.drawPile)
        player.buildInTurn=-20
        coroutineScope.launch{
            RoomManager.syncData(this@GameEngine)
        }
    }

    fun useSkill(player: Player){

        player.character.skill(this,player)

    }


    /**
     * 开始新游戏。
     */


     fun startGame() {
        isGameRunning = true
        // 游戏开始的初始化逻辑
        setupGame()

        Log.d("GameEngine","Game start!")


        // 进行第一个回合
        GameStatus.ChooseCharacter.start(this)
        sync()

    }

    fun reset(){
        isGameRunning=false
        deck= Deck()
        players.forEach{player->
            player.reset()
        }

        sync()
    }

    fun getIndexInPlayersList(player: Player): Int {
        return players.indexOf(player)
    }

    fun sync(){

        coroutineScope.launch {
            RoomManager.syncData(this@GameEngine)
        }
    }




    fun nextPlayerToTakeAction(){


        val currentIndex=CharacterType.values().indexOf(players[currentPlayerIndex].character)
        val nextPlayer = players
            .filter { CharacterType.values().indexOf(it.character) > currentIndex }
            .minByOrNull { CharacterType.values().indexOf(it.character) }

        if (nextPlayer==null){

                    GameStatus.ChooseCharacter.start(this)

            }else{
                currentPlayerIndex=players.indexOf(nextPlayer)
                if (nextPlayer.assassinated){
                    nextPlayerToTakeAction()
                    return
                }

                if (nextPlayer.stolen){
                    players.forEach{
                        if (it.character==CharacterType.THIEF){
                            it.gold+=nextPlayer.gold
                            nextPlayer.gold=0
                        }
                    }

                }

                if (nextPlayer.character==CharacterType.WARLORD){
                    nextPlayer.gold+=nextPlayer.getDistrictAmount(DistrictType.MILITARY)
                }
            }


        sync()
    }


    
    

    fun isYourTurn(player: Player): Boolean {
        return currentPlayerIndex==players.indexOf(player)
    }

    fun inTurnPlayer():Player{
        return players[currentPlayerIndex]
    }





    /**
     * 设置游戏，准备开始。
     */
    private fun setupGame() {
        deck.generateCards()
        // 初始化牌堆
        deck.shuffle()
        // 分发起始资源，例如金币和区域卡片
        distributeInitialResources()

    }

    /**
     * 分发初始资源给玩家。
     */
    private fun distributeInitialResources() {
        players[0].haveCrown=true
        // 给每个玩家分发金币和初始区域卡片
        players.forEach { player ->
            player.gold = initialGoldAmount
            player.addToHand(deck.draw())
            player.addToHand(deck.draw())
        }
    }

    fun assassinate(characterType: CharacterType){
        players.forEach{
            if (it.character==characterType){
                it.assassinated=true
            }
        }
    }

    fun stole(characterType: CharacterType){
        players.forEach{
            if (it.character==characterType){
                it.stolen=true
            }
        }
    }

    fun showCharacter(player: Player): Boolean {
        return gameStatus==GameStatus.TakeAction&&CharacterType.values().indexOf(player.character)<=CharacterType.values().indexOf(inTurnPlayer().character)
    }


    enum class GameStatus{
        ChooseCharacter {
            override fun start(gameEngine: GameEngine) {
                
                gameEngine.apply {


                    if (lastRound){
                        winner=players.maxBy {getScore(it) }
                        isGameRunning=false
                        sync()
                        return
                    }

                    players.forEach{
                        it.turnReset()
                    }
                    gameStatus = ChooseCharacter

                    disposeCharacterMap.clear()

                    currentPlayerIndex = players.indexOfFirst { it.haveCrown }
                    remainingCharacterList=CharacterType.values().drop(1).toMutableList()
                     remainingCharacterList.shuffle()
                    val rule = getNumberOfCardsSetAside(players.size)
                    Log.d("GameEngin","rule: $rule")
                    for (i in 0 until rule.first) {
                        disposeCharacterMap[remainingCharacterList.removeFirst().name] =
                            true
                    }
                    for (i in 0 until rule.second) {
                        disposeCharacterMap[remainingCharacterList.removeFirst().name] =
                            false
                    }

                }
                
                
                
                
            }
        },
        TakeAction {
            override fun start(gameEngine: GameEngine) {
                gameEngine.apply {
                    gameStatus=TakeAction

                    currentPlayerIndex=players.indexOf(players.minByOrNull { CharacterType.values().indexOf(it.character) })
                    Log.d("GameEngine","currentPlayerIndex=$currentPlayerIndex")

                }
            }
        };
        abstract fun start(gameEngine: GameEngine)
    }






}