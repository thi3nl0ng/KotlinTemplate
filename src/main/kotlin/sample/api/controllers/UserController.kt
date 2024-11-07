package sample.api.controllers

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*
import sample.api.models.User
import sample.api.models.UserRequest
import sample.api.services.UserService
import io.ktor.server.auth.*
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI


fun Route.userController(userService: UserService) {     
    route("/users") {
        get {
            call.respond(userService.getAllUsers())
        }

        post () {     
            // Receive the User object from the request body
            val user = call.receive<UserRequest>()
            var addedUser = userService.addUser(user)
            // Respond with the created user (echoing back)           
            call.respond(HttpStatusCode.OK, addedUser) // Respond with the updated user if found
        }
       
        get("/{id}",{
            // description of the route
            description = "Get user by id"
            // information about the request
            request {
                // information about the query-parameter "name" of type "string"
                queryParameter<String>("id") {
                    description = "the user id"
                }
            }

             response {
                // information about a "200 OK" response
                code(HttpStatusCode.OK) {
                    // a description of the response
                    description = "successful request - always returns the user object"
                    body<User> {
                        description = "the user object."
                        mediaTypes(ContentType.Application.Json)
                        required = true
                    }
                }
            }           
        }) {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val user = userService.getUserById(id)
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
            }
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@put
            }

            // Receive the User object from the request body
            val user = call.receive<UserRequest>()
            var updatedUser = userService.updateUser(id, user)

            if (updatedUser != null) {
                call.respond(HttpStatusCode.OK, updatedUser) // Respond with the updated user if found
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found") // Respond with 404 if the user wasn't found
            }
        }

        delete("/{id}") {  
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@delete
            }

            val wasDeleted = userService.deleteUser(id)
            if (wasDeleted) {
                call.respond(HttpStatusCode.OK, "User deleted successfully")
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found")
            }
        }
        
    }
}
