package sample.api.models
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int, 
    var name: String, 
    var email: String
)
