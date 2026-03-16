package services
import java.sql.Connection // Interface for database connection
import java.sql.DriverManager // To connect to the DB
import java.sql.PreparedStatement // Precompiled SQL statement
import java.sql.ResultSet // Represents results of SQL queries

data class User(
    val userID: Int,
    val name: String,
    val email: String,
    val passwordHash: String,
    val passwordKey: String,
    val gender: String?,
    val age: Int?,
    val height: Double?,
    val weight: Double?,
    val accountType: Boolean
)

// Connect to DB
private val connection: Connection = DriverManager.getConnection("jdbc:sqlite:${System.getProperty("user.dir")}/src/main/resources/users.db")

fun createUser(user: User): Boolean {
    if (findUser(user.email) == null) {
        // SQL INSERT statement with "?"" as a placeholder
        val stmt: PreparedStatement = connection.prepareStatement(
            """
            INSERT INTO users(name, email, passwordHash, passwordKey, gender, age, height, weight, accountType)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        )

        // Set the values for each placeholder in the SQL statement
        stmt.setString(1, user.name)
        stmt.setString(2, user.email)
        stmt.setString(3, user.passwordHash)
        stmt.setString(4, user.passwordKey)
        stmt.setString(5, user.gender)
        stmt.setObject(6, user.age)
        stmt.setObject(7, user.height)
        stmt.setObject(8, user.weight)
        stmt.setBoolean(9, user.accountType)

        stmt.executeUpdate() // Execute the INSERT command
        stmt.close() // Close the statement
        return true
    } else {
        return false
    }

}

fun findUser(email: String): User? {
    val stmt: PreparedStatement = connection.prepareStatement(
        "SELECT * FROM users WHERE email = ?"
    )

    stmt.setString(1, email) // Put email value in placeholder (?)

    val rs: ResultSet = stmt.executeQuery()

    // If query returns a row, create a User object
    if (rs.next()) {
        val user = User(
            userID = rs.getInt("userID"),
            name = rs.getString("name"),
            email = rs.getString("email"),
            passwordHash = rs.getString("passwordHash"),
            passwordKey = rs.getString("passwordKey"),
            gender = rs.getString("gender"),
            age = rs.getObject("age") as Int?,
            height = rs.getObject("height") as Double?,
            weight = rs.getObject("weight") as Double?,
            accountType = rs.getBoolean("accountType")
            )
            rs.close()
            stmt.close()
            return user
    }
    rs.close()
    stmt.close()
    return null
    }