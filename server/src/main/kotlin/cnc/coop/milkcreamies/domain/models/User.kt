package cnc.coop.milkcreamies.domain.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
    val lastLoginAt: LocalDateTime?
)

@Serializable
data class UserRegistration(
    val username: String,
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String
)

@Serializable
data class UserLogin(
    val username: String,
    val password: String
)

@Serializable
data class UserUpdate(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val isActive: Boolean?
)

@Serializable
data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
)

@Serializable
data class LoginResponse(
    val message: String,
    val user: UserResponse,
    val sessionId: String
)

@Serializable
data class UserDataEntry(
    val id: Int,
    val userId: Int,
    val dataKey: String,
    val dataValue: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?
)

@Serializable
data class UserDataCreate(
    val dataKey: String,
    val dataValue: String
)

@Serializable
data class UserDataUpdate(
    val dataValue: String
)

@Serializable
enum class UserRole {
    USER, ADMIN, SUPERUSER
}

@Serializable
data class ErrorResponse(
    val error: String,
    val message: String? = null
)
