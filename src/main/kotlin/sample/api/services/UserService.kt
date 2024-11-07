package sample.api.services
import sample.api.models.User
import sample.api.models.UserRequest
class UserService {
    private val users = mutableListOf(
        User(1, "John Doe", "john@example.com"),
        User(2, "Jane Doe", "jane@example.com")
    )

    fun getAllUsers(): List<User> = users
    fun getUserById(id: Int): User? = users.find { it.id == id }
    fun addUser(user: UserRequest) { 
        val maxId = users.maxOfOrNull { it.id }
        user.id = maxId ?: 1
        users.add( User(id = user.id, name = user.name, email = user.email) ) 
    }
    fun deleteUser(id: Int): Boolean {
        return users.removeIf { it.id == id }
    }
    fun updateUser(id: Int, updateduser: UserRequest): User? {
        val user = users.find { it.id == id }
        user?.let {
            it.name = updateduser.name
            it.email = updateduser.email
        }
        return user  // Returns the updated user if found, otherwise null
    }
}
