package citadels.project

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.model.DocumentCollections
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserManager {

    private var usersCollection:CollectionReference


    init {
        val db = FirebaseFirestore.getInstance()
        usersCollection=db.collection("users")


    }

    private fun createUser(id: String, name: String): User {
        return User(id, name)
    }

    private fun saveUser(user: User) {
        usersCollection.document(user.id).set(user)
            .addOnSuccessListener { Log.d(TAG, "User successfully saved!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error saving user", e) }
    }

    suspend fun login(context: Context): User = withContext(Dispatchers.IO) {
        var self = User()

        val sharedPref = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        if (sharedPref.contains("userId")&&sharedPref.contains("userName")){
            self.id= sharedPref.getString("userId","Name Not found").toString()
            self.name=sharedPref.getString("userName","ID Not found").toString()
            Log.d(TAG,"Get user from sharedPreferences")
            return@withContext self
        }
        try {
            val result = FirebaseAuth.getInstance().signInAnonymously().await()
            val sessionId = result.user?.uid

            if (sessionId != null) {
                if (idIsExist(sessionId)) {

                    Log.d(TAG, "Give the user object")
                    self = getUserById(sessionId) ?: User()
                } else {

                    Log.d(TAG, "Create new user")
                    self = createUser(sessionId, "")

                    saveUser(self)
                }
                saveUserInSharedPreferences(sharedPref,self)

            } else {
                Log.w(TAG, "signInAnonymously:failure - User ID is null")
            }
        } catch (e: Exception) {
            Log.w(TAG, "signInAnonymously:failure", e)
        }

        Log.d(TAG, "login user :\nid:${self.id}\nname:${self.name}")
        return@withContext self
    }

    private fun saveUserInSharedPreferences(sharedPreferences: SharedPreferences, user: User){
        val editor = sharedPreferences.edit()
        editor.putString("userId",user.id)
        editor.putString("userName",user.name)
        editor.apply()
    }


    private suspend fun idIsExist(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val document = usersCollection.document(id).get().await()
            document.exists()
        } catch (e: Exception) {
            Log.d(TAG, "Failed with: ", e)
            false
        }
    }


    private suspend fun getUserById(userId: String): User? {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            Log.d(TAG,"getUserById:\nid:${documentSnapshot.toObject(User::class.java)?.id}\nname:${documentSnapshot.toObject(User::class.java)?.name}")
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun setName(userId: String,newName:String,context: Context){
        try {
            usersCollection.document(userId).update(mapOf("name" to newName)).await()
            saveUserInSharedPreferences(context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE),User(userId,newName))
            Log.d(TAG, "ID $userId, Name ->$newName")
        }catch(e: Exception) {
            Log.w(TAG, "Error changing name", e)
        }




    }






    companion object {
        private const val TAG = "UserManager"
    }
}