package citadels.project

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import citadels.project.game.GameEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

import kotlinx.coroutines.Job

class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        val rm=RoomManager()
        val um= UserManager()



        setContent {
            MyScreen(rm, um,this)
        }
    }
}



@Composable
fun MyScreen(rm:RoomManager,um:UserManager,context: Context) {
    var isLoading by remember { mutableStateOf(true) }

    var user by remember { mutableStateOf(User()) }


    // 模拟一个异步操作，2秒后加载完成
    LaunchedEffect(key1 = Unit) {
        user=um.login(context)
        Log.d("MainActivity", "ID ${user.id}, Name ->${user.name}")
        isLoading = false

    }

    LoadingContent(isLoading = isLoading) {
        // 这里放置你的主界面内容
        Log.d("MainActivity", "ID ${user.id}, Name ${user.name}")
        if (user.id.isNotEmpty()){
            MyEditableNameScreen(user=user, userManager = um, onLoading = { isLoading = it },rm, context)
        }

    }
}

@Composable
fun LoadingContent(isLoading: Boolean, content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(),
        ) {
        content()
        if (isLoading) {
            // 覆盖整个内容的灰色背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray.copy(alpha = 0.8f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                // 显示一个加载旋转框
                CircularProgressIndicator()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MyEditableNameScreen(user: User,userManager: UserManager,onLoading: (Boolean) -> Unit,roomManager: RoomManager,context: Context) {

    var isEditing by remember { mutableStateOf(false) }
    val name = remember { mutableStateOf(TextFieldValue(text = user.name)) }
    val coroutineScope = rememberCoroutineScope()
    val roomName = remember { mutableStateOf(TextFieldValue()) }
    var isRoomNameOccupied by remember { mutableStateOf(false) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    val scrollState = rememberScrollState()
    val  imeState= rememberImeState()

    LaunchedEffect(key1 = imeState.value){
        scrollState.scrollTo(100)
        Log.d("WTF","")
    }
    

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)

                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            TextField(
                value = name.value,
                onValueChange = { newName ->
                    name.value = newName
                },
                readOnly = !isEditing,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = MaterialTheme.typography.displayMedium.fontSize
                ),

                placeholder = { Text("Enter your name") },
                label = { Text(text = "Your Name:", style = TextStyle(fontSize = 20.sp)) }

            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(

                onClick = {
                    if (isEditing) {
                        // Handle the submission of the edited name here

                        onLoading(true)
                        coroutineScope.launch {
                            userManager.setName(user.id, name.value.text, context)
                            onLoading(false)
                        }

                    }
                    isEditing = !isEditing
                },
                enabled = isEditing.not() || name.value.text.isNotBlank()
            ) {
                Text(if (isEditing) "Submit Your Name" else "Edit Your Name")
            }

            //Join Room field
            TextField(
                
                value = roomName.value,
                onValueChange = { newName ->
                    roomName.value = newName
                    isRoomNameOccupied = false // 当用户输入时，禁用按钮
                    debounceJob?.cancel() // 取消之前的协程
                    debounceJob = coroutineScope.launch {
                        delay(200) // 等待用户停止输入1秒
                        isRoomNameOccupied = RoomManager.isUserNameOccupied(roomName.value.text)
                    }
                },

                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = MaterialTheme.typography.displayMedium.fontSize
                ),


                label = {
                    Text(
                        text = "Enter Room Name To Join Room:",
                        style = TextStyle(fontSize = 20.sp)
                    )
                },


            )
            Spacer(modifier = Modifier.height(8.dp))
            val context = LocalContext.current
            Button(

                onClick = {

                    // Handle the submission of the edited name here

                    onLoading(true)
                    coroutineScope.launch {
                        RoomManager.joinRoomByRoomName(roomName.value.text, user)
                        val intent = Intent(context, RoomActivity::class.java).apply {
                            putExtra("roomName", roomName.value.text)
                            putExtra("user", user)

                        }
                        context.startActivity(intent)

                        onLoading(false)
                    }



                },
                enabled = isEditing.not() && name.value.text.isNotBlank() && isRoomNameOccupied && roomName.value.text.isNotBlank()
            ) {
                Text(if (isRoomNameOccupied) "Join Room" else "Cannot Find Room")
            }

            CreateRoomView(
                coroutineScope,
                onLoading,
                isEditing.not() && name.value.text.isNotBlank(),
                user
            )

        }
    



}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomView(
    coroutineScope: CoroutineScope,
    onLoading: (Boolean) -> Unit,
    enable: Boolean,
    user: User
){
    val roomName = remember { mutableStateOf(TextFieldValue()) }
    var isRoomNameOccupied by remember { mutableStateOf(true) }
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    val context= LocalContext.current


    Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
    ) {


            TextField(
                value = roomName.value,
                onValueChange = { newName ->
                    roomName.value = newName
                    isRoomNameOccupied = true // 当用户输入时，禁用按钮
                    debounceJob?.cancel() // 取消之前的协程
                    debounceJob = coroutineScope.launch {
                        delay(200) // 等待用户停止输入1秒
                        isRoomNameOccupied = RoomManager.isUserNameOccupied(roomName.value.text)
                    }
                },

                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = MaterialTheme.typography.displayMedium.fontSize
                ),


                label = {
                    Text(
                        text = "Enter Room Name To Create Room:",
                        style = TextStyle(fontSize = 20.sp)
                    )
                }

            )
            Spacer(modifier = Modifier.height(8.dp))



            Button(

                onClick = {

                    // Handle the submission of the edited name here

                    onLoading(true)
                    coroutineScope.launch {

                        RoomManager.createRoomByUser(user, roomName.value.text, GameEngine())
                        val intent = Intent(context, RoomActivity::class.java).apply {
                            putExtra("roomName", roomName.value.text)
                            putExtra("user",user)
                        }
                        context.startActivity(intent)
                        onLoading(false)
                    }



                },
                enabled = enable && !isRoomNameOccupied
            ) {
                Text(if (isRoomNameOccupied) "Name is invalid" else "Create New Room")
            }

            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { val webpage: Uri = Uri.parse("https://play-citadels.online/rules")
                val intent = Intent(Intent.ACTION_VIEW, webpage)
                context.startActivity(intent)
                 }) {
                Text("See Rules")
            }


    }







}



