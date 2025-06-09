package cnc.coop.milkcreamies.repositoryImpl

import cnc.coop.milkcreamies.data.UserData
import cnc.coop.milkcreamies.data.Users
import cnc.coop.milkcreamies.domain.models.User
import cnc.coop.milkcreamies.domain.models.UserDataCreate
import cnc.coop.milkcreamies.domain.models.UserDataEntry
import cnc.coop.milkcreamies.domain.models.UserDataUpdate
import cnc.coop.milkcreamies.domain.models.UserRegistration
import cnc.coop.milkcreamies.domain.models.UserRole
import cnc.coop.milkcreamies.domain.models.UserUpdate
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class UserRepository {

    fun createUser(userRegistration: UserRegistration): User? {
        return transaction {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                val hashedPassword = hashPassword(userRegistration.password)

                Users.insert {
                    it[username] = userRegistration.username
                    it[email] = userRegistration.email
                    it[passwordHash] = hashedPassword
                    it[firstName] = userRegistration.firstName
                    it[lastName] = userRegistration.lastName
                    it[role] = UserRole.USER.name.lowercase()
                    it[isActive] = true
                    it[createdAt] = now
                }

                getUserByUsername(userRegistration.username)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun authenticateUser(username: String, password: String): User? {
        return transaction {
            val userRow = Users.select { Users.username eq username }.singleOrNull()
            userRow?.let { row ->
                val storedHash = row[Users.passwordHash]
                if (verifyPassword(password, storedHash)) {
                    // Update last login
                    val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                    Users.update({ Users.id eq row[Users.id] }) {
                        it[lastLoginAt] = now
                    }
                    mapRowToUser(row)
                } else null
            }
        }
    }

    fun getUserById(id: Int): User? {
        return transaction {
            Users.select { Users.id eq id }.singleOrNull()?.let { mapRowToUser(it) }
        }
    }

    fun getUserByUsername(username: String): User? {
        return transaction {
            Users.select { Users.username eq username }.singleOrNull()?.let { mapRowToUser(it) }
        }
    }

    fun getAllUsers(): List<User> {
        return transaction {
            Users.selectAll().map { mapRowToUser(it) }
        }
    }

    fun updateUser(id: Int, userUpdate: UserUpdate): User? {
        return transaction {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val updated = Users.update({ Users.id eq id }) {
                userUpdate.firstName?.let { firstName -> it[Users.firstName] = firstName }
                userUpdate.lastName?.let { lastName -> it[Users.lastName] = lastName }
                userUpdate.email?.let { email -> it[Users.email] = email }
                userUpdate.isActive?.let { isActive -> it[Users.isActive] = isActive }
                it[updatedAt] = now
            }
            if (updated > 0) getUserById(id) else null
        }
    }

    fun deleteUser(id: Int): Boolean {
        return transaction {
            // First delete user data
            UserData.deleteWhere { UserData.userId eq id }
            // Then delete user
            Users.deleteWhere { Users.id eq id } > 0
        }
    }

    fun updateUserRole(id: Int, role: UserRole): User? {
        return transaction {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val updated = Users.update({ Users.id eq id }) {
                it[Users.role] = role.name.lowercase()
                it[updatedAt] = now
            }
            if (updated > 0) getUserById(id) else null
        }
    }

    // User Data methods
    fun createUserData(userId: Int, userDataCreate: UserDataCreate): UserDataEntry? {
        return transaction {
            try {
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                UserData.insert {
                    it[UserData.userId] = userId
                    it[dataKey] = userDataCreate.dataKey
                    it[dataValue] = userDataCreate.dataValue
                    it[createdAt] = now
                }
                // Get the inserted data by querying the most recent entry for this user and key
                UserData.select {
                    (UserData.userId eq userId) and (UserData.dataKey eq userDataCreate.dataKey)
                }.orderBy(UserData.createdAt to SortOrder.DESC)
                    .limit(1)
                    .singleOrNull()?.let { mapRowToUserData(it) }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getUserData(userId: Int): List<UserDataEntry> {
        return transaction {
            UserData.select { UserData.userId eq userId }.map { mapRowToUserData(it) }
        }
    }

    fun getUserDataById(id: Int): UserDataEntry? {
        return transaction {
            UserData.select { UserData.id eq id }.singleOrNull()?.let { mapRowToUserData(it) }
        }
    }

    fun updateUserData(id: Int, userId: Int, userDataUpdate: UserDataUpdate): UserDataEntry? {
        return transaction {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val updated = UserData.update({
                (UserData.id eq id) and (UserData.userId eq userId)
            }) {
                it[dataValue] = userDataUpdate.dataValue
                it[updatedAt] = now
            }
            if (updated > 0) getUserDataById(id) else null
        }
    }

    fun deleteUserData(id: Int, userId: Int): Boolean {
        return transaction {
            UserData.deleteWhere {
                (UserData.id eq id) and (UserData.userId eq userId)
            } > 0
        }
    }

    private fun hashPassword(password: String): String {
        // Simple hash for now - in production use proper bcrypt
        return password + "_hashed"
    }

    private fun verifyPassword(password: String, hash: String): Boolean {
        // Simple verification - in production use proper bcrypt
        return hash == password + "_hashed"
    }

    private fun mapRowToUser(row: ResultRow): User {
        return User(
            id = row[Users.id],
            username = row[Users.username],
            email = row[Users.email],
            firstName = row[Users.firstName],
            lastName = row[Users.lastName],
            role = UserRole.valueOf(row[Users.role].uppercase()),
            isActive = row[Users.isActive],
            createdAt = row[Users.createdAt],
            updatedAt = row[Users.updatedAt],
            lastLoginAt = row[Users.lastLoginAt]
        )
    }

    private fun mapRowToUserData(row: ResultRow): UserDataEntry {
        return UserDataEntry(
            id = row[UserData.id],
            userId = row[UserData.userId],
            dataKey = row[UserData.dataKey],
            dataValue = row[UserData.dataValue],
            createdAt = row[UserData.createdAt],
            updatedAt = row[UserData.updatedAt]
        )
    }
}
