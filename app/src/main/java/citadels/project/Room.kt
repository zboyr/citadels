package citadels.project

import citadels.project.game.GameEngine
import citadels.project.game.Player
import com.google.firebase.firestore.Exclude

data class Room(
    val id: String = "",
    val name: String = "",

    val gameEngine: GameEngine=GameEngine()
)
