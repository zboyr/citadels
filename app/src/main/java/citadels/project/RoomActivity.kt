package citadels.project


import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box



import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import citadels.project.game.GameEngine
import citadels.project.game.Player
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.lifecycleScope
import citadels.project.game.CharacterType
import citadels.project.game.shiftList
import kotlinx.coroutines.launch

class RoomActivity : ComponentActivity() {
    lateinit var roomName: String
    lateinit var user: User
    lateinit var player: Player
    val paddingValues = PaddingValues(10.dp)





    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        roomName = intent.getStringExtra("roomName").toString()
        user = intent.getParcelableExtra("user", User::class.java)
            ?: throw IllegalArgumentException("User not provided")





        setContent {
            RoomScreen()
        }


    }


    @Composable
    @Preview
    fun RoomScreen() {

        val context = LocalContext.current
        // 模拟不同人数的房间
        //val users = remember { mutableStateListOf<User>() }

        val room = remember {
            mutableStateOf<Room?>(null)
        }



        LaunchedEffect(roomName) {
            val onError = { exception: Exception? ->
                //Log.d("RoomActivity", exception.toString())
                // Handle any errors appropriately in your app
            }

            val onResult = { resultRoom: Room ->
                //users.clear()
//
//                // 移除特定ID的用户
//                val filteredUsers = updatedUsers.filter { it.id != user.id}
//                // 添加剩余的用户到users列表
//                users.addAll(filteredUsers)
                room.value = resultRoom
                //users.addAll(room.value!!.users)
                //Log.d("RoomActivity", "Room change detact!")
                player= resultRoom.gameEngine.players.find { it.user.id==user.id }!!

                Unit
            }

            // Listen for changes in the document
            val subscription = RoomManager.listenToRoomUpdates(roomName, onResult, onError)

            // When the composable leaves the composition, stop listening to changes

        }






        Column {


            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                if (room.value != null) {
                    DynamicRoomLayout(room.value!!.gameEngine)
                }


            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Cyan)
            ) {
                if (room.value!=null){
                    BottomLayout(context, room.value!!.gameEngine)
                }





            }
        }
    }

    @Composable
    fun BottomLayout(context:Context,gameEngine: GameEngine) {
        val scrollState=rememberScrollState()
        val isYourTurn=gameEngine.isYourTurn(player)
        if (isYourTurn&&gameEngine.isGameRunning&&gameEngine.gameStatus==GameEngine.GameStatus.ChooseCharacter){
            gameEngine.chooseCharacter(player)
        }

        Box(modifier = Modifier.fillMaxSize()) {


            Box(modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(0.8f)

                ) {
                Row {


                        PlayerBox(player = player, gameIsRunning = gameEngine.isGameRunning,true,true,gameEngine.getScore(player),gameEngine.isYourTurn(player))




                    Column(modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)) {
                        player.handCards.forEach {
                            val buttonEnable=player.buildAble(it)&&isYourTurn
                            Button(onClick = { gameEngine.build(player,it)
                            }, enabled =buttonEnable,
                                colors = ButtonDefaults.buttonColors(containerColor = if (buttonEnable) it.type.color else it.type.color.copy(alpha = 0.5f)
                                )
                            ,){
                                Text(text = it.toShortSting(), color =   if (buttonEnable) Color.Black else it.type.color , style = TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, color =  Color.Black  ))
                            }
                        }
                    }
                }
            }



        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            Text(
                text = "${gameEngine.gameStatus} Room Name:$roomName User Name:${user.name}",
                color = Color.Blue,
                fontSize = 12.sp
            )
        }




        Column(modifier = Modifier.align(Alignment.TopEnd)) {
            Button(onClick = {
                RoomManager.leaveRoom(roomName, player)
                val intent = Intent(context, MainActivity::class.java)
                context.startActivities(arrayOf(intent))
            }) {
                Text(text = "Quit")
            }
            Button(onClick = {

                //player.character?.abilityEffect?.let { it(gameEngine,player) }



                    //Log.d("RoomActivity", "lifecycleScope")
                    //player.character?.abilityEffect?.let { it(gameEngine,player) }
                    gameEngine.useSkill(player)



            }, enabled = isYourTurn&&!player.usedSkill&&gameEngine.gameStatus==GameEngine.GameStatus.TakeAction) {
                Text(text = "Skill")
            }
            Button(onClick = {

                    gameEngine.action(player)


            }, enabled = !player.tookAction&&isYourTurn&&gameEngine.gameStatus==GameEngine.GameStatus.TakeAction) {
                Text(text = "Action")
            }

            Button(onClick = {

                //player.character?.abilityEffect?.let { it(gameEngine,player) }
                gameEngine.nextPlayerToTakeAction()


            },enabled = isYourTurn&&gameEngine.gameStatus==GameEngine.GameStatus.TakeAction) {
                Text(text = "End")
            }

        }



    }
    }


    @Composable
    fun DynamicRoomLayout(gameEngine: GameEngine) {
        gameEngine.characterTypeDialogFactory.NumberPickerDialog()
        gameEngine.actionDialogFactory.NumberPickerDialog()
        gameEngine.districtDialogFactory.NumberPickerDialog()
        gameEngine.playerDialogFactory.NumberPickerDialog()
        var otherPlayer = gameEngine.players.filterNot { player ->
//            Log.d("RoomActivity","user=$user")
//            Log.d("RoomActivity","player.user=${player.user}")
            player.user.id == user.id
        }

        otherPlayer=shiftList(otherPlayer,-gameEngine.getIndexInPlayersList(player))


//        Log.d("RM","Before: ${gameEngine.players}")
//        Log.d("RM","After: $otherPlayer")
        Box(modifier = Modifier.fillMaxSize()) {

            Row (modifier = Modifier.align(Alignment.TopStart)){
                Button(onClick =  {gameEngine.cheat(player)}, ) {
                    Text(text = "+$")
                }
                Button(onClick = { gameEngine.reset() }) {
                    Text(text = "🛑")
                }
            }
            otherPlayer.forEachIndexed { index, person ->


                val alignment = when (otherPlayer.size) {
                    1 -> when (index) {
                        else -> Alignment.TopCenter
                    }

                    2 -> when (index) {
                        0 -> Alignment.TopStart
                        else -> {
                            Alignment.TopEnd
                        }
                    }

                    3 -> when (index) {
                        0 -> Alignment.CenterStart
                        1 -> Alignment.TopEnd
                        else -> Alignment.BottomEnd
                    }

                    4 -> when (index) {
                        0 -> Alignment.BottomStart
                        1 -> Alignment.TopStart
                        2 -> Alignment.TopEnd
                        else -> Alignment.BottomEnd
                    }

                    5 -> when (index) {
                        0 -> Alignment.BottomStart
                        1 -> Alignment.TopStart
                        2 -> Alignment.TopEnd
                        3 -> Alignment.CenterEnd
                        else -> Alignment.BottomEnd
                    }

                    6 -> when (index) {
                        0 -> Alignment.BottomStart
                        1 -> Alignment.CenterStart
                        2 -> Alignment.TopStart
                        3 -> Alignment.TopEnd
                        4 -> Alignment.CenterEnd
                        else -> Alignment.BottomEnd
                    }

                    7 -> when (index) {
                        0 -> Alignment.BottomStart
                        1 -> Alignment.CenterStart
                        2 -> Alignment.TopStart
                        3 -> Alignment.TopCenter
                        4 -> Alignment.TopEnd
                        5 -> Alignment.CenterEnd
                        else -> Alignment.BottomEnd
                    }

                    else -> when (index % 4) {
                        0 -> Alignment.TopStart
                        1 -> Alignment.TopEnd
                        2 -> Alignment.BottomStart
                        else -> Alignment.BottomEnd
                    }
                }


                    Box (modifier = Modifier.align(alignment)){
                        PlayerBox(person, gameEngine.isGameRunning,gameEngine.showCharacter(person),false,gameEngine.getScore(person),gameEngine.isYourTurn(person))
                    }









            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
                    .fillMaxWidth(0.32f)
                    .aspectRatio(0.5f)
                    .background(Color.LightGray),

                ) {
                Column(modifier = Modifier.align(Alignment.TopCenter)) {
                    if (!gameEngine.isGameRunning) {
                        if (gameEngine.winner!=Player()){
                            Text(text = "WINNER: ${gameEngine.winner.user.name}!!", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Red, textAlign = TextAlign.Center))
                            Text(text = "WINNER WINNER\nCHICKEN DINNER", style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Yellow, textAlign = TextAlign.Center))
                        }

                        Text(text = "Game not start\n", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center))
                        Button(onClick = { gameEngine.startGame() },modifier = Modifier.align(Alignment.CenterHorizontally)) {
                            Text(text = "Start!")
                        }
                    }else{
                        Spacer(modifier = Modifier.weight(0.1f))
                        Row {
                            Image(
                                painter = painterResource(id = R.drawable.cards_game_casino_svgrepo_com),
                                contentDescription = null,
                                modifier = Modifier.getDpByScreenWidth(0.15f)
                            )
                            Text(text = gameEngine.deck.drawPile.size.toString(), color = Color.White, fontSize = 40.sp)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row {
                            Image(
                                painter = painterResource(id = R.drawable.coins_money_svgrepo_com),
                                contentDescription = null,
                                modifier = Modifier.getDpByScreenWidth(0.15f)
                            )

                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Row {

                            gameEngine.disposeCharacterMap.forEach{ (type, show) ->
                                Image(
                                    painter = painterResource(id = if (show) CharacterType.values().find { it.name==type }!!.iconID else R.drawable.question_svgrepo_com),
                                    contentDescription = null,
                                    modifier = Modifier.getDpByScreenWidth(0.1f)
                                )


                            }



                        }



                    }
                }
                
            }

        }
    }




}

