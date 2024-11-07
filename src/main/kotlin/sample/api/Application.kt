package sample.api
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import sample.api.controllers.userController
import sample.api.services.UserService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.server.sessions.*
import io.ktor.server.plugins.swagger.*
import kotlinx.serialization.Serializable
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.github.smiley4.ktorswaggerui.data.*
import io.github.smiley4.ktorswaggerui.dsl.config.PluginConfigDsl

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)    
}

fun Application.module() {   
    val redirects = mutableMapOf<String, String>()  

    install(ContentNegotiation) {
        json() 
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowHeader(HttpHeaders.ContentType)
    }    

    install(Sessions) {
        cookie<UserSession>("user_session")
    }    

    install(SwaggerUI){
        // configure basic information about the api
        info {
            title = "Sample API"
            description = "Sample V2.0 Swagger-UI Functionality."
        }
        
        // configure the servers from where the api is being served
        server {
            url = "http://localhost:8080"
            description = "Development Server"
        }
        server {
            url = "https://www.ediel.no"
            description = "Production Server"
        }
        security {
            // configure a basic-auth security scheme
            securityScheme("MySecurityScheme") {
                name = "Bearer"
                type = AuthType.HTTP
                scheme = AuthScheme.BEARER
                bearerFormat = "JWT"
            }

            // if no other security scheme is specified for a route, the one with this name is used instead
            defaultSecuritySchemeNames("MySecurityScheme")
            // if no other response is documented for "401 Unauthorized", this information is used instead
            defaultUnauthorizedResponse {
                description = "Unauthorized"
            }
        }

        tags {
            tagGenerator = { url -> listOf(url.firstOrNull()) }
            tag("users") {
                description = "routes to manage users"
                externalDocUrl = "example.com"
                externalDocDescription = "Users documentation"
            }            
        }
    }    

    val oauthConfig = environment.config.config("ktor.oauth2.provider.authentication")
    val ojwtConfig = environment.config.config("ktor.oauth2.provider.jwt")

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(ojwtConfig.property("algorithmSecret").getString()))  // Replace with actual secret or public key
                    .withIssuer(ojwtConfig.property("issuer").getString())            // Replace with actual issuer
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(ojwtConfig.property("audience").getString())) { // Validate audience
                    JWTPrincipal(credential.payload)
                } else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or expired")
            }
        }   

        oauth("auth-oauth") {
            urlProvider = { oauthConfig.property("redirectUrl").getString() }  // Adjust the callback URL
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "oauth",
                    authorizeUrl = oauthConfig.property("authorizeUrl").getString(),
                    accessTokenUrl = oauthConfig.property("accessTokenUrl").getString(),
                    clientId =  oauthConfig.property("clientId").getString(),
                    clientSecret = oauthConfig.property("clientSecret").getString(),
                    requestMethod = HttpMethod.Post,
                    defaultScopes = listOf("openid", "profile", "email"),
                    onStateCreated = { 
                        call, state ->
                        //saves new state with redirect url value
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    }
                )
            }
            client = HttpClient(Apache)       
        } 
    }

    val userService = UserService()
  
    routing { 
     
        route("swagger-ui") {
            swaggerUI("/api.json")
        }
        route("api.json") {
            openApiSpec()
        }
    
        authenticate("auth-oauth") {
            // Endpoint to initiate OAuth
            get("/login") {
                // Redirects to the Google OAuth flow
                call.respondRedirect("/callback")
            }

            // OAuth callback endpoint
            get("/callback") {
                val currentPrincipal: OAuthAccessTokenResponse.OAuth2? = call.principal()

                val accessToken = currentPrincipal?.accessToken
                // redirects home if the url is not found before authorization

                if (accessToken != null)// && validateAccessToken(accessToken)) 
                {
                    currentPrincipal?.let { principal ->
                        principal.state?.let { state ->
                            call.sessions.set(UserSession(state, principal.accessToken))
                            redirects[state]?.let { redirect ->
                                call.respondRedirect(redirect)
                                return@get
                            }
                        }
                    }

                    call.respondRedirect("/")
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid access token.")
                }            
            }
        }

        authenticate("auth-jwt") {
            userController(userService)   
        }

        get("/",{
            // description of the route
            description = "A Hello-World Kotlin API"
           
            // information about possible responses
            response {
                // information about a "200 OK" response
                code(HttpStatusCode.OK) {
                    // a description of the response
                    description = "successful request - always returns 'Welcome to Sample API v2.0'"
                }
            }
        }
            ) {
           call.respondText("Welcome to Sample API v2.0", status = HttpStatusCode.Created)
        } 
    }
} 

