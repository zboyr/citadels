package citadels.project

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box



import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import citadels.project.game.DistrictType
import citadels.project.game.Player




@Composable
fun PlayerBox(player: Player, gameIsRunning: Boolean,showCharacter:Boolean,isSelf:Boolean,score:Int,inTurn:Boolean) {



    Column(modifier = Modifier
        .padding(10.dp)
        // 假设这是自定义的修饰符
        .shadow(8.dp, RoundedCornerShape(10.dp))  // 首先应用阴影
        .background(Color.Blue, RoundedCornerShape(10.dp))  // 然后应用背景
        .border(5.dp, if (inTurn) Color.Red else Color.Gray, RoundedCornerShape(10.dp))  // 最后应用边框
        .getDpByScreenWidth(0.32f)
        .wrapContentHeight()

    )

        {

        if (gameIsRunning) {

            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Text(
                    text = player.user.name,
                    style = TextStyle(fontSize = 25.sp, fontWeight = FontWeight.Bold,color = Color.White),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Cyan)
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.castle_dracula_halloween_svgrepo_com),
                        contentDescription = null,
                        modifier = Modifier.getDpByScreenWidth(0.07f)
                    )
                    Text(
                        text = player.districts.size.toString(),
                        color = Color.Gray,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    AnimatedVisibility(visible = player.haveCrown) {
                        Image(
                            painter = painterResource(id = R.drawable.crown_svgrepo_com),
                            contentDescription = null,
                            modifier = Modifier.getDpByScreenWidth(0.07f)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(id = R.drawable.board_game_score_svgrepo_com),
                        contentDescription = null,
                        modifier = Modifier.getDpByScreenWidth(0.07f)
                    )
                    Text(text = score.toString(), color = Color.Gray, fontSize = 20.sp)


                    //R.drawable.question_svgrepo_com

                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(color = Color.Gray)
                        .fillMaxWidth()
                ) {
                    SpacerAdaptedScreenWidth(fraction = 0.02f)
                    BuildingView(
                        color = Color.Yellow,
                        number = player.getDistrictAmount(DistrictType.NOBLE).toString()
                    )
                    SpacerAdaptedScreenWidth(fraction = 0.02f)
                    BuildingView(
                        color = Color.Green,
                        number = player.getDistrictAmount(DistrictType.TRADE).toString()
                    )
                    SpacerAdaptedScreenWidth(fraction = 0.02f)
                    BuildingView(
                        color = Color.Blue,
                        number = player.getDistrictAmount(DistrictType.RELIGIOUS).toString()
                    )
                    SpacerAdaptedScreenWidth(fraction = 0.02f)
                    BuildingView(
                        color = Color.Red,
                        number = player.getDistrictAmount(DistrictType.MILITARY).toString()
                    )

                }


                if (isSelf) {

                        MoneyAndCardAmountLayout(player = player, true)


                } else {
                    if (showCharacter) {

                            MoneyAndCardAmountLayout(player = player, true)

                    } else {
                        MoneyAndCardAmountLayout(player = player, false)
                    }
                }


            }
        } else {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(text = player.user.name, style = TextStyle(fontSize = 30.sp, color = Color.White))
            }

        }
    }
}


@Composable
fun DynamicFontSizeText(text: String) {
    val fontSize = calculateFontSize(text)
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        fontSize = fontSize,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = Color.White
    )
}



@Composable
fun BuildingView(color: Color, number: String) {
    Box(
        modifier = Modifier
            .size((LocalConfiguration.current.screenWidthDp / 30).dp) // 设置圆点的大小，可以根据需要调整
            .background(color = color, shape = CircleShape) // 设置背景色为黄色，并且是圆形
    )
    Text(text = number, color = Color.White, fontSize = 16.sp)
}

@Composable
fun MoneyAndCardAmountLayout(player: Player, showCharacter: Boolean){
    Row (modifier = Modifier

        .fillMaxWidth()
        .wrapContentHeight()){


        Column(
            modifier = Modifier
                .wrapContentSize()
                
        ) {
            Image(
                painter = painterResource(id = R.drawable.cards_game_casino_svgrepo_com),
                contentDescription = null,
                modifier = Modifier.getDpByScreenWidth(0.08f)
            )
            Spacer(modifier = Modifier.height((LocalConfiguration.current.screenWidthDp * 0.03f).dp))

            Image(
                painter = painterResource(id = R.drawable.coins_money_svgrepo_com),
                contentDescription = null,
                modifier = Modifier.getDpByScreenWidth(0.08f)
            )



        }

        val paintID=if (showCharacter) {
            if (player.assassinated){
                R.drawable.christianity_dead_svgrepo_com
            } else if (player.stolen){
                R.drawable.theft_crime_steal_thief_svgrepo_com
            }else{
                player.character.iconID
            }
        }else {
        R.drawable.question_svgrepo_com}
        Box(modifier = Modifier.wrapContentSize()){
            Text(text = player.handCards.size.toString(), color = Color.White, fontSize = 20.sp, modifier = Modifier.align(Alignment.TopStart))
            Image(
                painter = painterResource(id = paintID),
                contentDescription = null,
                modifier = Modifier
                    .getDpByScreenWidth(0.3f)
                    .align(Alignment.Center)
            )
            Text(text = player.gold.toString(), color = Color.White, fontSize = 20.sp, modifier = Modifier.align(Alignment.BottomStart))
        }











    }
}


private fun calculateFontSize(text: String): TextUnit {
    // 根据文本长度动态计算字体大小
    // 这里的逻辑可以根据需要进行调整
    val calculatedSize = 20 - text.length * 0.2
    return maxOf(minOf(calculatedSize, 20.0), 8.0).sp
}




@Composable
fun SpacerAdaptedScreenWidth(fraction: Float) {
    Spacer(Modifier.size((LocalConfiguration.current.screenWidthDp * fraction).dp))
}


fun Modifier.getDpByScreenWidth(fraction: Float): Modifier = composed {
    this.width((LocalConfiguration.current.screenWidthDp * fraction).dp)
}