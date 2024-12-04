package citadels.project



import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext


class DialogFactory<T>(val defaultSelection : T,val discretion:String)  {
    var toSelectList:List<T> = listOf(defaultSelection)
    var _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()
    var theSelection=defaultSelection

    @Composable
    fun NumberPickerDialog(){
        val showDialog by showDialog.collectAsState()



        if (showDialog) {
            Dialog(onDismissRequest = { _showDialog.value= false }) {

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column {
                        Text(text = discretion, style = TextStyle(fontSize = 30.sp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1), // 可以根据需求调整每行的元素数量
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {

                            items(toSelectList) { character ->
                                Button(
                                    onClick = {
                                        // 处理数字选择
                                        theSelection = character
                                        _showDialog.value = false

                                    },
                                    modifier = Modifier
                                        .height(IntrinsicSize.Min)
                                ) {
                                    Text(
                                        text = character.toString(),
                                        style = TextStyle(fontSize = 20.sp)
                                    )
                                }
                            }


                        }
                    }
                }

            }
        }

    }

    fun setShowDialogValue(show: Boolean) {
        _showDialog.value = show
    }


//    init {
//        viewModelScope.launch {
//            showDialog.collect { isShown ->
//                if (!isShown) {
//
//                    // 当 showDialog 变为 false 时，执行操作
//                    Log.d("Dialog", "$selection")
//                }
//            }
//        }
//    }


     suspend fun selectNumber(list:List<T>):T{
        //Log.d("Dialog","??")
         theSelection=defaultSelection
         toSelectList=list

        _showDialog.value=true
        Log.d("Dialog", showDialog.value.toString())

        // 等待 showDialog 变为 false
         withContext(Dispatchers.Main) {
             showDialog
                 .filter { !it } // 等待 showDialog 变为 false
                 .first() // 取第一个符合条件的值
         }

         return theSelection // 返回选择的数字


    }
}