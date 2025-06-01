package cnc.coop.milkcreamies.routes

import cnc.coop.milkcreamies.domain.models.ErrorResponse
import cnc.coop.milkcreamies.domain.models.LoginResponse
import cnc.coop.milkcreamies.domain.models.UserDataCreate
import cnc.coop.milkcreamies.domain.models.UserDataUpdate
import cnc.coop.milkcreamies.domain.models.UserLogin
import cnc.coop.milkcreamies.domain.models.UserRegistration
import cnc.coop.milkcreamies.domain.models.UserResponse
import cnc.coop.milkcreamies.domain.models.UserRole
import cnc.coop.milkcreamies.domain.models.UserUpdate
import cnc.coop.milkcreamies.repositoryImpl.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

private val userRepository = UserRepository()

fun Route.userRoutes() {
    route("/auth") {
        // User Registration
        post("/signup") {
            try {
                val registration = call.receive<UserRegistration>()
                
                // Validate input
                if (registration.username.isBlank() || registration.email.isBlank() || 
                    registration.password.isBlank() || registration.firstName.isBlank() || 
                    registration.lastName.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid input", "All fields are required")
                    )
                    return@post
                }
                
                // Check if user already exists
                if (userRepository.getUserByUsername(registration.username) != null) {
                    call.respond(
                        HttpStatusCode.Conflict, 
                        ErrorResponse("User exists", "Username already taken")
                    )
                    return@post
                }
                
                val user = userRepository.createUser(registration)
                if (user != null) {
                    val userResponse = UserResponse(
                        id = user.id,
                        username = user.username,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        role = user.role,
                        isActive = user.isActive,
                        createdAt = user.createdAt,
                        lastLoginAt = user.lastLoginAt
                    )
                    call.respond(HttpStatusCode.Created, userResponse)
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError, 
                        ErrorResponse("Registration failed", "Unable to create user account")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
        
        // User Login
        post("/signin") {
            try {
                val login = call.receive<UserLogin>()
                
                if (login.username.isBlank() || login.password.isBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid input", "Username and password are required")
                    )
                    return@post
                }
                
                val user = userRepository.authenticateUser(login.username, login.password)
                if (user != null && user.isActive) {
                    val userResponse = UserResponse(
                        id = user.id,
                        username = user.username,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        role = user.role,
                        isActive = user.isActive,
                        createdAt = user.createdAt,
                        lastLoginAt = user.lastLoginAt
                    )
                    // Simple response - in production you'd return a JWT token
                    call.respond(
                        HttpStatusCode.OK, LoginResponse(
                            message = "Login successful",
                            user = userResponse,
                            sessionId = "session_${user.id}_${System.currentTimeMillis()}"
                    ))
                } else {
                    call.respond(
                        HttpStatusCode.Unauthorized, 
                        ErrorResponse("Login failed", "Invalid credentials or account disabled")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
    }

    route("/users") {
        // Get all users (Admin/Superuser only)
        get {
            try {
                val users = userRepository.getAllUsers()
                val userResponses = users.map { user ->
                    UserResponse(
                        id = user.id,
                        username = user.username,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        role = user.role,
                        isActive = user.isActive,
                        createdAt = user.createdAt,
                        lastLoginAt = user.lastLoginAt
                    )
                }
                call.respond(HttpStatusCode.OK, userResponses)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError, 
                    ErrorResponse("Server error", e.message)
                )
            }
        }
        
        // Get user by ID
        get("/{id}") {
            try {
                val userId = call.parameters["id"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid ID", "User ID must be a number")
                    )
                    return@get
                }
                
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    val userResponse = UserResponse(
                        id = user.id,
                        username = user.username,
                        email = user.email,
                        firstName = user.firstName,
                        lastName = user.lastName,
                        role = user.role,
                        isActive = user.isActive,
                        createdAt = user.createdAt,
                        lastLoginAt = user.lastLoginAt
                    )
                    call.respond(HttpStatusCode.OK, userResponse)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound, 
                        ErrorResponse("User not found", "No user found with ID $userId")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
        
        // Update user
        put("/{id}") {
            try {
                val userId = call.parameters["id"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid ID", "User ID must be a number")
                    )
                    return@put
                }
                
                val userUpdate = call.receive<UserUpdate>()
                val updatedUser = userRepository.updateUser(userId, userUpdate)
                if (updatedUser != null) {
                    val userResponse = UserResponse(
                        id = updatedUser.id,
                        username = updatedUser.username,
                        email = updatedUser.email,
                        firstName = updatedUser.firstName,
                        lastName = updatedUser.lastName,
                        role = updatedUser.role,
                        isActive = updatedUser.isActive,
                        createdAt = updatedUser.createdAt,
                        lastLoginAt = updatedUser.lastLoginAt
                    )
                    call.respond(HttpStatusCode.OK, userResponse)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound, 
                        ErrorResponse("User not found", "No user found with ID $userId")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
        
        // Delete user (Superuser only)
        delete("/{id}") {
            try {
                val userId = call.parameters["id"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid ID", "User ID must be a number")
                    )
                    return@delete
                }
                
                val deleted = userRepository.deleteUser(userId)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User deleted successfully"))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound, 
                        ErrorResponse("User not found", "No user found with ID $userId")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
        
        // Update user role (Superuser only)
        put("/{id}/role") {
            try {
                val userId = call.parameters["id"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid ID", "User ID must be a number")
                    )
                    return@put
                }
                
                val roleRequest = call.receive<Map<String, String>>()
                val roleString = roleRequest["role"]
                if (roleString == null) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid input", "Role is required")
                    )
                    return@put
                }
                
                val role = try {
                    UserRole.valueOf(roleString.uppercase())
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid role", "Valid roles: USER, ADMIN, SUPERUSER")
                    )
                    return@put
                }
                
                val updatedUser = userRepository.updateUserRole(userId, role)
                if (updatedUser != null) {
                    val userResponse = UserResponse(
                        id = updatedUser.id,
                        username = updatedUser.username,
                        email = updatedUser.email,
                        firstName = updatedUser.firstName,
                        lastName = updatedUser.lastName,
                        role = updatedUser.role,
                        isActive = updatedUser.isActive,
                        createdAt = updatedUser.createdAt,
                        lastLoginAt = updatedUser.lastLoginAt
                    )
                    call.respond(HttpStatusCode.OK, userResponse)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound, 
                        ErrorResponse("User not found", "No user found with ID $userId")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
    }
    
    route("/user-data") {
        // Get user data
        get("/{userId}") {
            try {
                val userId = call.parameters["userId"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid ID", "User ID must be a number")
                    )
                    return@get
                }
                
                val userData = userRepository.getUserData(userId)
                call.respond(HttpStatusCode.OK, userData)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
        
        // Create user data
        post("/{userId}") {
            try {
                val userId = call.parameters["userId"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid ID", "User ID must be a number")
                    )
                    return@post
                }
                
                val userDataCreate = call.receive<UserDataCreate>()
                val createdData = userRepository.createUserData(userId, userDataCreate)
                if (createdData != null) {
                    call.respond(HttpStatusCode.Created, createdData)
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError, 
                        ErrorResponse("Creation failed", "Unable to create user data")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
        
        // Update user data
        put("/{userId}/{dataId}") {
            try {
                val userId = call.parameters["userId"]?.toIntOrNull()
                val dataId = call.parameters["dataId"]?.toIntOrNull()
                if (userId == null || dataId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid ID", "User ID and Data ID must be numbers")
                    )
                    return@put
                }
                
                val userDataUpdate = call.receive<UserDataUpdate>()
                val updatedData = userRepository.updateUserData(dataId, userId, userDataUpdate)
                if (updatedData != null) {
                    call.respond(HttpStatusCode.OK, updatedData)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound, 
                        ErrorResponse("Data not found", "No data found with the specified IDs")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
        
        // Delete user data
        delete("/{userId}/{dataId}") {
            try {
                val userId = call.parameters["userId"]?.toIntOrNull()
                val dataId = call.parameters["dataId"]?.toIntOrNull()
                if (userId == null || dataId == null) {
                    call.respond(
                        HttpStatusCode.BadRequest, 
                        ErrorResponse("Invalid ID", "User ID and Data ID must be numbers")
                    )
                    return@delete
                }
                
                val deleted = userRepository.deleteUserData(dataId, userId)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User data deleted successfully"))
                } else {
                    call.respond(
                        HttpStatusCode.NotFound, 
                        ErrorResponse("Data not found", "No data found with the specified IDs")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.BadRequest, 
                    ErrorResponse("Invalid request", e.message)
                )
            }
        }
    }
}
