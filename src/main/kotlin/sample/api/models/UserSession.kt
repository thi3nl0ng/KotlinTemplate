package sample.api
import kotlinx.serialization.Serializable

@Serializable
data class UserSession(val state: String, val token: String)