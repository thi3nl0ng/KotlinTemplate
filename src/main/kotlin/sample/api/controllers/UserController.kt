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
import io.github.smiley4.ktorswaggerui.dsl.routing.delete
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respond
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import sample.api.config.ErrorModel

fun Route.userController(userService: UserService) {     
    route("/users") {
        get ({
            description = "Get all users"
            summary = "Get all users"})
        {
            call.respond(userService.getAllUsers())
        }

        post ({
                description = "Create a new user"
                summary = "Create a new user"
                request {
                    body<UserRequest> {
                        description = "add new user"
                        required = true
                        example("Add new user 1") {
                            value = UserRequest( 
                                name = "John Doe",
                                email = "john@example.com"
                            )
                        }

                        example("Add new user 2") {
                            value = UserRequest(
                                name = "Jane Doe",
                                email = "jane@example.com"
                            )
                        }
                    }
                }
                response {
                    code(HttpStatusCode.OK) {
                        description = "successful request - always returns the user object"
                        body<User> {
                            description = "the user object."
                            mediaTypes(ContentType.Application.Json)
                            required = true
                        }
                    }
                } 
            }) { 

            val user = call.receive<UserRequest>()
            var addedUser = userService.addUser(user)
            call.respond(HttpStatusCode.OK, addedUser) 
        }
       
        get("/{id}",{
            description = "Get user by id"
            summary = "Get user by id"
            request {
                queryParameter<String>("id") {
                    description = "the user id"
                }
            }

             response {
                code(HttpStatusCode.OK) {
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

        post("/{id}",{
            description = "Update user by id"
            summary = "Update user"
            request {
                queryParameter<String>("id") {
                    description = "The user id"
                }
                body<UserRequest> {
                    description = "Add new user"
                    required = true
                    example("Add new user 1") {
                        value = UserRequest( 
                            name = "John Doe",
                            email = "john@example.com"
                        )
                    }

                    example("Add new user 2") {
                        value = UserRequest(
                            name = "Jane Doe",
                            email = "jane@example.com"
                        )
                    }
                }
            }

            response {
                code(HttpStatusCode.OK) {
                    description = "Successful request - always returns the user object"
                    body<User> {
                        description = "The user object."
                        mediaTypes(ContentType.Application.Json)
                        required = true
                    }
                }
            }           
        }
        ) {
         
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Invalid user ID")
                return@post
            }

            val user = call.receive<UserRequest>()
            var updatedUser = userService.updateUser(id, user)

            if (updatedUser != null) {
                call.respond(HttpStatusCode.OK, updatedUser) 
            } else {
                call.respond(HttpStatusCode.NotFound, "User not found") 
            }
        }

        delete("/{id}",{
            // description of the route
            description = "Delete user by id"
            summary = "Delete user"
            // information about the request
            request {
                // information about the query-parameter "name" of type "string"
                queryParameter<String>("id") {
                    description = "The user id"
                }
            }

             response {
                // information about a "200 OK" response
                code(HttpStatusCode.OK) {
                    // a description of the response
                    description = "Successful request - always returns the return message"
                }
            }           
        }) {  
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

        post("/testpost", {
            description = "Creates a new user"
            summary = "Just a test endpoint"
            request {
                body<UserRequest> {
                    description = "Test add new user"
                    required = true
                    example("New User 1") {
                        value = UserRequest( 
                            name = "John Doe",
                            email = "john@example.com"
                        )
                    }
                    example("New User 2") {
                        value = UserRequest(
                            name = "Jane Doe",
                            email = "jane@example.com"
                        )
                    }
                }
            }
            response {
                code(HttpStatusCode.OK) {
                    body<User> {
                        description = "The created user"
                        example("John") {
                            value = User(
                                id = 1,
                                name = "John",
                                email = "john@example.com"
                            )
                        }
                        example("Jane") {
                            value = User(
                                id = 2,
                                name = "Jane",
                                email = "jane@example.com"
                            )
                        }
                    }
                }
                
            }
        }) {
            call.respond(HttpStatusCode.NotImplemented, Unit)
        }        
    }
}
