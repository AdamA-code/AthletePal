import java.sql.Connection // Interface for database connection
import java.sql.DriverManager // To connect to the DB
import java.sql.PreparedStatement // Precompiled SQL statement
import java.sql.ResultSet // Represents results of SQL queries

data class User(
    val userID: Int,
    val name: String,
    val emai: String,
    val passwordHash: String,
    val passwordKey: String,
    val gender: String?,
    val age: Int?,
    val height: Double?,
    val weight: Double?,
    val accountType: String?
)

// Connect to DB
val connection: Connection = DriverManager.getConnection("")

fun createUser(user: User) {
    // SQL INSERT statement with "?"" as a placeholder
    val stmt: PreparedStatement = connection.prepareStatement(
        """
        INSERT INTO users(name, email, passwordHash, key, gender, age, height, weight, accountType)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
    )

    // Set the values for each placeholder in the SQL statement
    stmt.setString(1, user.name)
    stmt.setString(2, user.email)
    stmt.setString(3, user.passwordHash)
    stmt.setString(4, user.key)
    stmt.setString(5, user.gender)
    stmt.setObject(6, user.age)
    stmt.setObject(7, user.height)
    stmt.setObject(8, user.weight)
    stmt.setString(9, user.accountType)

    stmt.executeUpdate() // Execute the INSERT command
    stmt.close() // Close the statement
}

fun findUser(email: String): User? {
    val stmt: PreparedStatement = connection.prepareStatement(
        "SELECT * FROM users WHERE email = ?"
    )

    stmt.setString(1, email) // Put email value in placeholder (?)

    val rs: ResultSet = stmt.executeQuery()

    // If query returns a row, create a User object
    if (rs.next()) {
        val user = User{
            rs.getInt("userID")
            rs.getString("name")
            rs.getString("email")
            rs.getString("passwordHash")
            rs.getString("key")
            rs.getObject("age") as Int?
            rs.getObject("height") as Double?
            rs.getObject("weight") as Double?
            rs.getString("accountType")
        }
    } else null

    rs.close()
    stmt.close()
    return user
}