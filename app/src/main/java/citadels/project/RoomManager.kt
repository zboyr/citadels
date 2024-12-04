package citadels.project

import android.util.Log
import citadels.project.game.GameEngine
import citadels.project.game.Player
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.tasks.await

class RoomManager() {
    companion object {
        private var roomsCollection: CollectionReference
        const val TAG = "RoomManager"
        init {

            val db: FirebaseFirestore = FirebaseFirestore.getInstance()
            roomsCollection = db.collection("rooms")
            Log.d(TAG, "RoomManager init successfully")

        }

        suspend fun createRoom(roomId: String, gameEngine: GameEngine): Room {
            Log.d(TAG, "createRoom is called with roomId: $roomId")
            val room = Room(roomId,roomId,gameEngine)
            try {
                roomsCollection.document(roomId).set(room).await()
                Log.d(TAG, "Room successfully saved!")

            } catch (e: Exception) {
                Log.d(TAG, "Error saving room", e)
            }



            return room
        }

        suspend fun syncData(gameEngine: GameEngine){
            Log.d(TAG,"Try to sync: $gameEngine")
            try {

                val docRef=roomsCollection.document(gameEngine.roomID)
                docRef.update("gameEngine",gameEngine).await()
                Log.d(TAG, "Room successfully saved!")

            } catch (e: Exception) {
                Log.d(TAG, "Error saving room", e)
            }
        }

        suspend fun createRoomByUser(user: User, roomName: String, gameEngine: GameEngine) {
            //Take the ID of the one create room for room ID
            gameEngine.roomID=roomName
            val room = createRoom(roomName, gameEngine)
            joinRoom(room.gameEngine.roomID, user)
        }

        suspend fun joinRoom(roomId: String, user: User) {
            try {
                // 获取房间文档引用
                val roomRef = roomsCollection.document(roomId)
                // 异步获取房间文档
                val roomSnapshot = roomRef.get().await()

                // 检查房间中是否已经有该用户
                val players = roomSnapshot.toObject(Room::class.java)?.gameEngine?.players
                val userExists = players?.any { it.user== user }!!

                // 如果用户不存在，将其添加到房间
                if (!userExists) {
                    roomRef.update("gameEngine.players", FieldValue.arrayUnion(Player(user = user)))
                        .await()
                    Log.d(TAG, "Join room successfully")
                } else {
                    Log.d(TAG, "User already in room")
                }
            } catch (e: Exception) {
                Log.d(TAG, "Join room failed with $e")
            }

        }


        suspend fun getEngine(roomName: String): Room {
            val docRef = getQuerySnapshotByRoomName(roomName)
            return docRef.documents.firstOrNull()?.toObject(Room::class.java)!!
        }

        fun leaveRoom(roomId: String, player: Player) {
            val userRef = roomsCollection.document(roomId)


            // Remove the user from the 'users' array field
            userRef.update("gameEngine.players", FieldValue.arrayRemove(player))
                .addOnSuccessListener {
                    Log.d(TAG, "User removed from room successfully")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error removing user from room", e)
                }
        }


        fun saveRoom(room: Room) {
            roomsCollection.document(room.gameEngine.roomID).set(room)
                .addOnSuccessListener { Log.d(TAG, "Room successfully saved!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error saving room", e) }
        }

        suspend fun isUserNameOccupied(roomName: String): Boolean {
            val querySnapshot = roomsCollection.whereEqualTo("name", roomName).get().await()
            return querySnapshot.documents.isNotEmpty()
        }

        suspend fun getQuerySnapshotByRoomName(roomName: String): QuerySnapshot {
            return roomsCollection.whereEqualTo("name", roomName).limit(1).get().await()
        }


        suspend fun joinRoomByRoomName(roomName: String, user: User) {
            Log.d(TAG, "Joining room : $roomName")
            try {
                // 查找与提供的房间名匹配的房间
                val querySnapshot = getQuerySnapshotByRoomName(roomName)
                if (querySnapshot.documents.isNotEmpty()) {
                    // 获取第一个匹配的房间的ID
                    val roomId = querySnapshot.documents.first().id
                    // 使用获取到的房间ID加入房间
                    joinRoom(roomId, user)

                } else {
                    Log.d(TAG, "No room found with name: $roomName")
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error joining room by name: $e")
            }
        }

        fun listenToRoomUpdates(
            roomName: String,
            onResult: (Room) -> Unit,
            onError: (Exception?) -> Unit
        ) {
            roomsCollection.whereEqualTo("name", roomName).limit(1)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        onError(e)
                        return@addSnapshotListener
                    }

                    val room = snapshot?.documents?.firstOrNull()?.toObject(Room::class.java)
                    if (room != null) {
                        onResult(room)
                    } else {
                        onError(NullPointerException("Room not found"))
                    }
                }
        }








    }
}