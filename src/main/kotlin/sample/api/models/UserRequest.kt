package sample.api.models
import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(var name: String, var email: String)