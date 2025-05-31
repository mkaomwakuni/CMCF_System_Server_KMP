package cnc.coop.milkcreamies.data

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val firstName = varchar("first_name", 100)
    val lastName = varchar("last_name", 100)
    val role = varchar("role", 20).default("user") // "user",
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()
    val lastLoginAt = datetime("last_login_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object UserData : Table("user_data") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id)
    val dataKey = varchar("data_key", 100)
    val dataValue = text("data_value")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at").nullable()

    override val primaryKey = PrimaryKey(id)
}