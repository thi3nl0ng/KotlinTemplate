package sample.api.config
import kotlinx.serialization.Serializable

@Serializable
data class ErrorModel(
    var code: String,
    var description: String    
)
