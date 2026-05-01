// Formatting assisted by ktlint
package services
import kotlinx.serialization.Serializable // Helps routes
import java.sql.Connection // Interface for database connection
import java.sql.DriverManager // To connect to the DB
import java.sql.PreparedStatement // Precompiled SQL statement
import java.sql.ResultSet // Represents results of SQL queries
import java.sql.Statement
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID // Univversal unique identifier for reset tokens

// Imports for sending reset emails to API for sending to user(s)
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Provides all database operations for the application.
 *
 * This class acts as the main service layer between the backend logic
 * and the SQL database, handling CRUD operations, authentication,
 * and relational queries.
 * 
 * It is responsible for maintaining data integrity, managing transactions,
 * and ensuring consistent communication between the application and database.
 */
class DatabaseService {

    // Data classes used to store inputs and outputs for database operations

    /**
     * Represents a registered user stored in the database.
     *
     * @property userID Unique identifier for the user.
     * @property name User's display name.
     * @property email User's login email address.
     * @property passwordHash Secure SHA-256 hash of the password.
     * @property passwordKey Random salt used during hashing.
     * @property gender Optional gender value.
     * @property age Optional age value.
     * @property height Optional height in cm.
     * @property weight Optional weight in kg.
     * @property accountType True if personal trainer, false if standard user.
     */
    @Serializable
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
        val accountType: Boolean,
    )

    /**
     * Represents an exercise stored in the database.
     *
     * @property exerciseID Unique identifier for the exercise, defaulting to 0.
     * @property name Exercise's display name.
     * @property category The category of exercise (e.g. weights, running, etc).
     * @property description A description of the exercise.
     */
    @Serializable
    data class Exercise(
        val exerciseID: Int = 0,
        val name: String,
        val category: String,
        val description: String?,
    )

    /**
     * Represents a workout stored in the database.
     *
     * @property workoutID Unique identifier for the workout, defaulting to 0.
     * @property createdBy The userID of the user who created the workout.
     * @property assignedTo The userID of the user who will be attempting the workout.
     * @property date The date the workout is scheduled for.
     * @property duration Optional duration of the workout.
     * @property workoutType Optional type of the workout such as weights or swimming.
     * @property notes Optional notes added to the workout.
     * @property routeID Optional routeID to link cardio workouts to a route on a map.
     * @property isRoutine True if the workout is a regular routine, false if not.
     */
    @Serializable
    data class Workout(
        val workoutID: Int = 0,
        val createdBy: Int,
        val assignedTo: Int,
        val date: String,
        val duration: Int? = null,
        val workoutType: String? = null,
        val notes: String? = null,
        val routeID: Int? = null,
        val isRoutine: Boolean = false,
    )

    /**
     * Represents a many-to-many relationship between workouts and exercises.
     *
     * @property workoutExercisesID Unique identifier for the exercise in the workout, defaulting to 0.
     * @property workoutID The workoutID of the workout the exercise belongs to.
     * @property exerciseID The exerciseID of the exercise in the workout.
     * @property sets Optional number of sets for the exercise.
     * @property reps Optional number of reps for the exercise.
     * @property weight Optional weight of the exercise.
     */
    @Serializable
    data class WorkoutExercise(
        val workoutExercisesID: Int = 0,
        val workoutID: Int = 0, // Still a required field for adding workout exercise to workout
        val exerciseID: Int,
        val sets: Int? = 1,
        val reps: Int? = 1,
        val weight: Double? = 1.0
    )

    /**
     * Represents a user statistic stored in the database.
     *
     * @property statisticID Unique identifier for the statistic, defaulting to 0.
     * @property userID The userID of the user the statistic belongs to.
     * @property exerciseID The exerciseID of the exercise the statistic is based off.
     * @property scoreValue The score the user achieved (e.g. 70kg for bench press, 240 minutes for a marathon).
     * @property date The date the user achieved this statistic.
     */
    @Serializable
    data class Statistic(
        val statisticID: Int = 0,
        val userID: Int,
        val exerciseID: Int,
        val scoreValue: Double,
        val date: String,
    )

    /**
     * Represents competition stored in a the database.
     *
     * @property competitionID Unique identifier for the competition in the database, defaulting to 0.
     * @property userID The userID of the user the competition belongs to.
     * @property name The name of the competition.
     * @property date The date the competition is scheduled for.
     * @property rank Optional placement of the user in the competition (e.g. first, third, etc).
     * @property result Optional score the user achieved in the competition.
     */
    @Serializable
    data class Competition(
        val competitionID: Int = 0,
        val userID: Int,
        val name: String,
        val date: String,
        val rank: Int? = null,
        val result: Int? = null,
    )

    /**
     * Represents a many-to-many relationship between competitions and exercises.
     *
     * @property competitionExerciseID Unique identifier for the exercise in the competition, defaulting to 0.
     * @property competitionID Unique identifier for the competition in the database.
     * @property exerciseID The exerciseID of the exercise in the competition.
     * @property performance Optional score of the user exercise (e.g. 70kg or 40 minutes).
     */
    @Serializable
    data class CompetitionExercise(
        val competitionExerciseID: Int = 0,
        val competitionID: Int,
        val exerciseID: Int,
        val performance: Double?,
    )

    /**
     * Represents a route associated with a cardio workout in the database.
     *
     * @property routeID Unique identifier for the route in the workout, defaulting to 0.
     * @property userID The userID of the user who owns the route.
     * @property name The name of the route.
     * @property distance The distance of the route.
     * @property estimatedTime Optional estimated time the route to be completed.
     * @property date The date the route is scheduled for.
     * @property isPublic True if the route is available to the public, false if not.
     * @property routeData The data that represents where the route is on the map.
     */
    @Serializable
    data class Route(
        val routeID: Int = 0,
        val userID: Int,
        val name: String,
        val distance: Double,
        val estimatedTime: Double? = null,
        val date: String,
        val isPublic: Boolean,
        val routeData: String,
    )

    /**
     * Represents a training group users can join.
     *
     * @property groupID Unique identifier for the training group users can join, defaulting to 0.
     * @property createdBy The userID of the user who created the workout.
     * @property name The name of the training group.
     * @property description Optional description describing the training group.
     * @property goal Optional overall goal users in the group are working towards.
     */
    @Serializable
    data class TrainingGroup(
        val groupID: Int = 0,
        val createdBy: Int,
        val name: String,
        val description: String? = null,
        val goal: String? = null
    )

    /**
     * Represents a many-to-many relationship between training groups and users.
     *
     * @property memberID Unique identifier for the users in a training group, defaulting to 0.
     * @property groupID Unique identifier for the group the user is in.
     * @property userID Unique identifier for user
     */
    @Serializable
    data class GroupMember(
        val memberID: Int = 0,
        val groupID: Int,
        val userID: Int,
    )

    /**
     * Represents a many-to-many relationship between training groups and workouts.
     *
     * @property sessionID Unique identifier for the workout session the group will be completing together, defaulting to 0.
     * @property groupID Unique identifier for the group the user is in.
     * @property workoutID Unique identifier for the workout.
     * @property scheduledDate The date the group session is scheduled to be held on.
     */
    @Serializable
    data class GroupSession(
        val sessionID: Int = 0,
        val groupID: Int,
        val workoutID: Int,
        val scheduledDate: String,
    )

    /**
     * Manages the application's connection to the SQLite database.
     *
     * This class creates a JDBC connection to the users.db database file located in the project's resources directory.
     *
     * When an instance is created, foreign key constraints are enabled using PRAGMA foreign_keys = ON 
     * so relational integrity rules such as cascading deletes are enabled.
     *
     * The connection can be reused by database service functions until close() is called.
     */
    class DatabaseConnection {
        // File path used to connect to the database
        val connectionLocation = "jdbc:sqlite:${System.getProperty("user.dir")}/src/main/resources/users.db"

        // Open connection used to communicate with database
        val connection: Connection = DriverManager.getConnection(connectionLocation)

        // Runs automatically on creation - enables foreign key contstraint enforcement
        init {
            connection.createStatement().execute("PRAGMA foreign_keys = ON") // Enable deletion cascading
        }

        // Closes the database connectiion if its currently open
        fun close() {
            if (!connection.isClosed) connection.close()
        }
    }

    // Shared connection used by all service functions
    val db = DatabaseConnection()

    /**
     * Stores a new user in the database.
     *
     * The function first checks whether the given email address is already associated with an existing account.
     * If the email is unique, the user record is inserted into the users table.
     *
     * If successful, the generated userID is returned. If the email already exists or the insert fails,
     * -1 is returned.
     *
     * Prepared statements are used to safely insert values.
     *
     * @param user The user data to store.
     * @return The created userID if successful, -1 if not.
     */
    fun createUser(user: User): Int {
        if (findUser(user.email) == null) {
            val stmt: PreparedStatement =
                db.connection.prepareStatement(
                    """
                INSERT INTO users(name, email, passwordHash, passwordKey, gender, age, height, weight, accountType)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """
                        .trimIndent(),
                    Statement.RETURN_GENERATED_KEYS, // Needed to return created ID
                )

            stmt.setString(1, user.name)
            stmt.setString(2, user.email)
            stmt.setString(3, user.passwordHash)
            stmt.setString(4, user.passwordKey)
            stmt.setString(5, user.gender)
            stmt.setObject(6, user.age)
            stmt.setObject(7, user.height)
            stmt.setObject(8, user.weight)
            stmt.setBoolean(9, user.accountType)

            val rowsInserted = stmt.executeUpdate()

            if (rowsInserted == 0) {
                stmt.close()
                return -1
            }

            val rs = stmt.generatedKeys
            val id = if (rs.next()) rs.getInt(1) else -1

            rs.close()
            stmt.close()

            return id
        }
        return -1
    }

    /**
     * Retrieves a user from the database using their email address.
     *
     * @param email The email address of the user to find.
     * @return The matching User object if found, null if no user exists.
     */
    fun findUser(email: String): User? {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM users WHERE email = ?",
            )

        stmt.setString(1, email)

        val rs: ResultSet = stmt.executeQuery()

        // If query returns a row, create a User object
        if (rs.next()) {
            val user =
                User(
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    email = rs.getString("email"),
                    passwordHash = rs.getString("passwordHash"),
                    passwordKey = rs.getString("passwordKey"),
                    gender = rs.getObject("gender") as String?,
                    age = rs.getObject("age") as Int?,
                    height = rs.getObject("height") as Double?,
                    weight = rs.getObject("weight") as Double?,
                    accountType = rs.getBoolean("accountType"),
                )
            rs.close()
            stmt.close()
            return user
        }
        return null
    }

    /**
     * Updates optional profile details for a user account.
     *
     * The function validates the provided email, ensuring it's in a valid format before attempting the update.
     * Age is also validated to ensure it falls in a realistic range.
     *
     * The update modifies only non-authentication profile fields such as gender, age, height, and weight.
     * The user is identified using their email address.
     *
     * If the update is successful, the database is modified and the function returns true. If no matching user
     * is found or validation fails, false is returned.
     *
     * @param email The email address of the user to update.
     * @param gender Optional gender value.
     * @param age Optional age value.
     * @param height Optional height in cm.
     * @param weight Optional weight in kg.
     * @return True if the update was successful, false if not.
     */
    fun updateAccount(
        email: String,
        gender: String?,
        age: Int?,
        height: Double?,
        weight: Double?,
    ): Boolean {

        //  Validation checks
        if (email.isBlank() || !email.contains("@") || !email.contains(".")) return false
        if (age != null && (age < 0 || age > 120)) return false

        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                UPDATE users
                SET gender = ?, age = ?, height = ?, weight = ?
                WHERE email = ?
                """.trimIndent(),
            )

        stmt.setString(1, gender)
        stmt.setObject(2, age)
        stmt.setObject(3, height)
        stmt.setObject(4, weight)
        stmt.setString(5, email)

        val rowsUpdated = stmt.executeUpdate()
        stmt.close()

        return rowsUpdated > 0
    }

    /**
     * Updates a user's password after verifying their current password.
     *
     * At first, the function validates that both passwords meet minimum security requirements and
     * ensures the user exists in the database.
     *
     * It then verifies the given old password against the stored hash using the user's stored salt.
     * If authentication succeeds, a new salt and hash are generated for the new password.
     *
     * The password hash and salt are then updated in the database.
     *
     * This function is used for voluntary password changes, not password recovery.
     *
     * @param email The email address of the user.
     * @param oldPassword The user's current password.
     * @param newPassword The new password to be set.
     * @return True if the password was successfully updated, false if not.
     */
    fun updatePassword(
        email: String,
        oldPassword: String,
        newPassword: String,
    ): Boolean {
        // Validation checks
        if (oldPassword.isBlank() || newPassword.isBlank() || newPassword.length < 8) return false
        val user = findUser(email) ?: return false

        val authService = AuthService()

        // Verify old password is correct via reverse
        if (!authService.verifyPassword(oldPassword, user.passwordKey, user.passwordHash)) {
            return false
        }

        // Generate new salt and hash
        val newSalt = authService.generateSalt()
        val newHash = authService.hashPassword(newPassword, newSalt)

        val stmt =
            db.connection.prepareStatement(
                """
                UPDATE users
                SET passwordHash = ?, passwordKey = ?
                WHERE email = ?
                """.trimIndent(),
            )
        stmt.setString(1, newHash)
        stmt.setString(2, newSalt)
        stmt.setString(3, email)

        val rowsUpdated = stmt.executeUpdate()
        stmt.close()

        return rowsUpdated > 0
    }

    /**
     * Generates a password reset token for a user account.
     *
     * The function validates the email and ensures the user exists in the database.
     * If valid, a unique token is generated and stored in the password_reset_tokens table. 
     * An expiry time is set to 1 hour from creation to add security.
     *
     * The token is returned so it can be sent to the user's email for password recovery.
     *
     * @param email The email address of the account requesting a password reset.
     * @return The generated reset token if successful, or null if validation fails or the user doesn't exit.
     */
    fun generatePasswordResetToken(email: String): String? {
        // Validation checks
        if (!email.contains("@") || !email.contains(".")) return null

        val user = findUser(email) ?: return null

        // Generate a random unique token
        val token = UUID.randomUUID().toString()

        // Set expiry to 1 hour from now
        val expiry = LocalDateTime.now().plusHours(1).toString()

        // Store token in DB
        val stmt =
            db.connection.prepareStatement(
                """
                INSERT INTO password_reset_tokens (userID, token, expiry, used)
                VALUES (?, ?, ?, 0)
                """.trimIndent(),
            )
        stmt.setInt(1, user.userID)
        stmt.setString(2, token)
        stmt.setString(3, expiry)

        val rows = stmt.executeUpdate()
        stmt.close()

        return if (rows > 0) {
            token
        } else {
            null
        }
    }

    /**
     * Resets a user's password using a valid password reset token.
     *
     * The function checks the token has not passed expiry or has already been used. If it has,
     * the function returns false. If valid, the user's password is updated using a newly generated salt
     * and hash based off the given input.
     * 
     *
     * The reset is performed inside a transaction using a try-catch block to ensure atomicity.
     * Both the password update and token invalidation must succeed or all changes are undone using rollback().
     *
     * @param token The password reset token provided to the user.
     * @param newPassword The new password.
     * @return True if the password was successfully reset, false if not.
     */
    fun resetPasswordWithToken(
        token: String,
        newPassword: String,
    ): Boolean {
        // Validation checks
        if (token.isBlank() || newPassword.length < 8) return false

        // Turn off autocommit for transactions
        db.connection.autoCommit = false
        try {
            // FInd unused token
            val stmt =
                db.connection.prepareStatement(
                    """
                    SELECT * FROM password_reset_tokens
                    WHERE token = ? AND used = 0
                    """.trimIndent(),
                )
            stmt.setString(1, token)

            val rs = stmt.executeQuery()

            // If no matching token found return false
            if (!rs.next()) {
                rs.close()
                stmt.close()
                db.connection.rollback()
                return false
            }

            // Get expiry and userID
            val expiry = LocalDateTime.parse(rs.getString("expiry")) // Set to parse object
            val userID = rs.getInt("userID")

            rs.close()
            stmt.close()

            // expiry check
            if (LocalDateTime.now().isAfter(expiry)) {
                db.connection.rollback()
                return false
            }

            // Generate new salt and hash for new password
            val authService = AuthService()
            val newKey = authService.generateSalt()
            val newHash = authService.hashPassword(newPassword, newKey)

            // Update password in db
            val updateStmt =
                db.connection.prepareStatement(
                    """
                    UPDATE users
                    SET passwordHash = ?, passwordKey = ?
                    WHERE userID = ?
                    """.trimIndent(),
                )
            updateStmt.setString(1, newHash)
            updateStmt.setString(2, newKey)
            updateStmt.setInt(3, userID)

            val updatedPassword = updateStmt.executeUpdate()
            updateStmt.close()

            // check on password updating
            if (updatedPassword == 0) {
                db.connection.rollback()
                return false
            }

            // Mark token as used (cant be used again)
            val usedTokenStmt =
                db.connection.prepareStatement(
                    """
                    UPDATE password_reset_tokens
                    SET used = 1 WHERE token = ?
                    """.trimIndent(),
                )
            usedTokenStmt.setString(1, token)

            val tokenUsed = usedTokenStmt.executeUpdate()
            usedTokenStmt.close()
            // check failure at token being set to used
            if (tokenUsed == 0) {
                db.connection.rollback()
                return false
            }

            db.connection.commit()
            return true
            // Find any unexpected errors and rollback if so, set connection back to autocommit either way
        } catch (error: Exception) {
            db.connection.rollback()
            return false
        } finally {
            db.connection.autoCommit = true
        }
    }

    fun sendResetEmail(email: String): Boolean {
        try {
            val token: String? = generatePasswordResetToken(email)

            if (token == null) { // If token couldn't be generated return false
                return false
            }

            val client = OkHttpClient() // create HTTP client instance
            val postmarkAPIToken = "28ac09e0-0c09-462f-9438-ccabc99a56e2"

            val json = """
                {
                    "From": "dwbt7640@leeds.ac.uk",
                    "To": "${email}",
                    "Subject": "Password Reset Code",
                    "TextBody": "Your password reset code is: ${token}",
                    "MessageStream": "outbound"
                }
            """.trimIndent() // Defines body of API email request

            val request = Request.Builder() // Begin building HTTP request for API
                .url("https://api.postmarkapp.com/email") // Send to API (Postmark)
                .post(json.toRequestBody("application/json".toMediaType())) // Attach JSON body defined above as POST request
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-Postmark-Server-Token", postmarkAPIToken) // Add Postmark Server API token for authentication
                .build()

            client.newCall(request).execute().use { response ->
                return response.isSuccessful // Returns true if confirmation recieved from API that email was accepted for sending
            }
        }
        catch(e: Exception) { // If an exception is hit at any point, return false
            return false
        }
    }

    /**
     * Deletes a user account from the database using their email address.
     * 
     * The function executes a DELETE operation on the users table if a user is found linking
     * to the given email address and true is returned. Otherwise, false is returned.
     * 
     * @param email The email address of the user to delete.
     * @return True if the user was deleted, false if not.
     */
    fun deleteUser(email: String): Boolean {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                DELETE FROM users
                WHERE email = ?
                """.trimIndent(),
            )

        stmt.setString(1, email)

        val rowsDeleted = stmt.executeUpdate()
        stmt.close()

        return rowsDeleted > 0
    }

    /**
     * Creates a new exercise and stores it in the database.
     * 
     * The function checks the exercise name isn't blank and ensures the category is one of the
     * predefined types: ("weights", "running", "cycling", "swimming", "walking").
     * 
     * If validation is passed, the exercise is inserted into the database and the exerciseID is returned.
     * 
     * @param exercise The exercise object containing name, category, and description.
     * @return The generated exerciseID if successful, -1  if not.
     */
    fun createExercise(exercise: Exercise): Int {
        
        // Validation Checks
        if (exercise.name.isBlank()) return -1
        val validCategories = listOf("weights", "running", "cycling", "swimming", "walking")
        if (exercise.category !in validCategories) return -1
    
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                INSERT INTO exercises(name, category, description)
                VALUES (?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS,
            )

        stmt.setString(1, exercise.name)
        stmt.setString(2, exercise.category)
        stmt.setString(3, exercise.description)

        val rowsInserted = stmt.executeUpdate()

        if (rowsInserted == 0) {
            stmt.close()
            return -1
        }

        val rs = stmt.generatedKeys
        val id = if (rs.next()) rs.getInt(1) else -1

        rs.close()
        stmt.close()

        return id
    }

    /**
     * Retrieves an exercise from the database using its unique ID.
     * 
     * If an exercise with the given ID exists, it is returned in an Exercise object.
     * If no matching record is found, null is returned instead.
     * 
     * @param exerciseID The unique identifier of the exercise.
     * @return The corresponding Exercise object, or null if no record is found.
     */
    fun findExercise(exerciseID: Int): Exercise? {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM exercises WHERE exerciseID = ?",
            )

        stmt.setInt(1, exerciseID)

        val rs: ResultSet = stmt.executeQuery()

        val exercise =
            if (rs.next()) {
                Exercise(
                    exerciseID = rs.getInt("exerciseID"),
                    name = rs.getString("name"),
                    category = rs.getString("category"),
                    description = rs.getString("description"),
                )
            } else {
                null
            }

        rs.close()
        stmt.close()

        return exercise
    }

    /**
     * Retrieves all exercises from the database.
     * 
     * Each record in the table is mapped to an Exercise object and returned in a list.
     * If the table is empty, an empty list is returned.
     * 
     * @return The list of Exercise objects in the database.
     */
    fun findAllExercises(): List<Exercise> {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM exercises",
            )

        val rs: ResultSet = stmt.executeQuery()
        val exercises = mutableListOf<Exercise>()
        while (rs.next()) {
            val exercise =
                Exercise(
                    exerciseID = rs.getInt("exerciseID"),
                    name = rs.getString("name"),
                    category = rs.getString("category"),
                    description = rs.getString("description"),
                )
            exercises.add(exercise)
        }

        rs.close()
        stmt.close()

        return exercises
    }

    /**
     * Updates an exercise in the database.
     *
     * The function validates the provided name, ensuring it's not blank before attempting the update.
     * category is also validated to ensure it falls the list of predefined acceptable values.
     *
     * The function only updates the category or optional description, identified by the exercise's name.
     *
     * If the update is successful, the database is modified and the function returns true. If no matching exercise
     * is found or validation fails, false is returned.
     * 
     *
     * @param name The name of the exercise used to identify the record.
     * @param category The new category value of the exercise.
     * @param description Optional updated description of the exercise.
     * @return True if the update was successful, false if not.
     */
    fun updateExercise(
        name: String,
        category: String,
        description: String?,
    ): Boolean {
        // Validation Checks
        if (name.isBlank()) return false
        val validCategories = listOf("weights", "running", "cycling", "swimming", "walking")
        if (category !in validCategories) return false
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                UPDATE exercises
                SET category = ?, description = ?
                WHERE name = ?
                """.trimIndent(),
            )

        stmt.setString(1, category)
        stmt.setString(2, description)
        stmt.setString(3, name)

        val rowsUpdated = stmt.executeUpdate()
        stmt.close()

        return rowsUpdated > 0
    }

    /**
     * Deletes an exercise from the database using the provided exerciseID.
     * 
     * The function executes a DELETE operation on the exercises table if an exercise is found linking
     * to the given exerciseID and true is returned. Otherwise, false is returned.
     * 
     * @param exerciseID The exerciseID of the row to delete.
     * @return True if the exercise was deleted, false if not.
     */
    fun deleteExercise(exerciseID: Int): Boolean {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "DELETE FROM exercises WHERE exerciseID = ?",
            )

        stmt.setInt(1, exerciseID)

        val rowsDeleted = stmt.executeUpdate()
        stmt.close()

        return rowsDeleted > 0
    }

    /**
     * Stores a new workout in the database.
     *
     * The function first checks whether the given date is not blank and in the correct format.
     * The function then checks that if the duration is not null then it is longer than 0, and that the given
     * workoutType of the workout is in the valid types list, if it iss not null.
     *
     * The function then inserts the workout into the database.
     * 
     * If successful, the generated workoutID is returned. If unsuccessful, -1 is returned.
     *
     * Prepared statements are used to safely insert values.
     *
     * @param workout The workout data to store.
     * @return The created workoutID if successful, -1 if not.
     */
    fun createWorkout(workout: Workout): Int {
        // Validation Checks
        if (workout.date.isBlank() || !workout.date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return -1
        if (workout.duration != null && workout.duration <= 0) return -1
        val validTypes = listOf("cardio", "weights", "running", "cycling", "swimming")
        if (workout.workoutType != null && workout.workoutType !in validTypes) return -1

        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                INSERT INTO workouts(createdBy, assignedTo, date, duration, workoutType, notes, routeID, isRoutine)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS,
            )

        stmt.setInt(1, workout.createdBy)
        stmt.setObject(2, workout.assignedTo)
        stmt.setString(3, workout.date)
        stmt.setObject(4, workout.duration)
        stmt.setObject(5, workout.workoutType)
        stmt.setObject(6, workout.notes)
        stmt.setObject(7, workout.routeID)
        stmt.setBoolean(8, workout.isRoutine)

        val rowsInserted = stmt.executeUpdate()

        if (rowsInserted == 0) {
            stmt.close()
            return -1
        }

        val rs = stmt.generatedKeys
        val id = if (rs.next()) rs.getInt(1) else -1

        rs.close()
        stmt.close()

        return id
    }

    /**
     * Retrieves all workouts for a specific user from the database.
     * 
     * Each record in the table is mapped to a Workout object and returned in a list.
     * If no matching record is found, an empty list is returned.
     * 
     * @param userID The unique identifier of the user who's workouts are being returned.
     * @return The list of Workout objects in the database.
     */
    fun findAllWorkouts(userID: Int): List<Workout> {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM workouts WHERE assignedTo = ?",
            )

        stmt.setInt(1, userID)

        val rs: ResultSet = stmt.executeQuery()
        val workouts = mutableListOf<Workout>()

        while (rs.next()) {
            val workout =
                Workout(
                    workoutID = rs.getInt("workoutID"),
                    createdBy = rs.getInt("createdBy"),
                    assignedTo = rs.getInt("assignedTo"),
                    date = rs.getString("date"),
                    duration = rs.getObject("duration") as Int?,
                    workoutType = rs.getObject("workoutType") as String?,
                    notes = rs.getObject("notes") as String?,
                    routeID = rs.getObject("routeID") as Int?,
                    isRoutine = rs.getBoolean("isRoutine"),
                )
            workouts.add(workout)
        }

        rs.close()
        stmt.close()
        return workouts
    }

    /**
     * Updates a workout in the database.
     *
     * The function validates the provided workoutID, ensuring it's not impossible beforing executing the
     * statement. Date is also validated to ensure it is in the correct YYYY-MM-DD format. Duration is then
     * validated to check that if a value is given, that it is greater than 0.
     *
     * The function only updates the date, duration, workoutType, notes, routeID, and isRoutine associated
     * with the workout, identified by the workoutID.
     *
     * If the update is successful, the database is modified and the function returns true. If no matching workout
     * is found or validation fails, false is returned.
     * 
     *
     * @param workoutID The unique identifier of the workout to update.
     * @param date The new date value of the workout.
     * @param duration Optional updated duration of the workout.
     * @param workoutType Optional new type descriptor of workout.
     * @param notes Optional new notes about the workout
     * @param routeID Optional new routeID linked to the workout.
     * @param isRoutine New flag that's true if the workout is a regular routine, false if not.
     * @return True if the update was successful, false if not.
     */
    fun updateWorkout(
        workoutID: Int,
        date: String,
        duration: Int?,
        workoutType: String?,
        notes: String?,
        routeID: Int?,
        isRoutine: Boolean,
    ): Boolean {
        // Validation Checks
        if (workoutID <= 0) return false
        if (date.isBlank() || !date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return false
        if (duration != null && duration <= 0) return false

        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                UPDATE workouts
                SET date = ?, duration = ?, workoutType = ?, notes = ?, routeID = ?, isRoutine = ?
                WHERE workoutID = ?
                """.trimIndent(),
            )

        stmt.setString(1, date)
        stmt.setObject(2, duration)
        stmt.setObject(3, workoutType)
        stmt.setObject(4, notes)
        stmt.setObject(5, routeID)
        stmt.setBoolean(6, isRoutine)
        stmt.setInt(7, workoutID)

        val rowsUpdated = stmt.executeUpdate()
        stmt.close()

        return rowsUpdated > 0
    }

    /**
     * Deletes a workout from the database using the provided workoutID.
     * 
     * The function executes a DELETE operation on the workouts table if a workout is found linking
     * to the given workoutID and true is returned. Otherwise, false is returned.
     * 
     * @param workoutID The workoutID of the row to delete.
     * @return True if the workout was deleted, false if not.
     */
    fun deleteWorkout(workoutID: Int): Boolean {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "DELETE FROM workouts WHERE workoutID = ?",
            )

        stmt.setInt(1, workoutID)

        val rowsDeleted = stmt.executeUpdate()
        stmt.close()

        return rowsDeleted > 0
    }

    
    /**
     * Stores a new many-to-many record in the database between workouts and exercises.
     *
     * The function first checks that the given exercise and workout IDs are possible
     * The function then validates that if sets, reps, or weight are not equal to null, then they are
     * above 0.
     *
     * The function then inserts the join record into the database.
     * 
     * If successful, the generated workoutExercisesID is returned. If unsuccessful, -1 is returned.
     *
     * Prepared statements are used to safely insert values.
     *
     * @param workout_exercise The workout_exercise data to store.
     * @return The created workoutExercisesID if successful, -1 if not.
     */
    fun addExerciseToWorkout(workout_exercise: WorkoutExercise): Int {
        // Validation Checks
        if (workout_exercise.workoutID <= 0 || workout_exercise.exerciseID <= 0) return -1
        if (workout_exercise.sets != null && workout_exercise.sets <= 0) return -1
        if (workout_exercise.reps != null && workout_exercise.reps <= 0) return -1
        if (workout_exercise.weight != null && workout_exercise.weight <= 0.0) return -1

        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                INSERT INTO workout_exercises(workoutID, exerciseID, sets, reps, weight)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS,
            )

        stmt.setInt(1, workout_exercise.workoutID)
        stmt.setInt(2, workout_exercise.exerciseID)
        stmt.setObject(3, workout_exercise.sets)
        stmt.setObject(4, workout_exercise.reps)
        stmt.setObject(5, workout_exercise.weight)

        val rowsInserted = stmt.executeUpdate()

        if (rowsInserted == 0) {
            stmt.close()
            return -1
        }

        val rs = stmt.generatedKeys
        val id = if (rs.next()) rs.getInt(1) else -1

        rs.close()
        stmt.close()

        return id
    }

    /**
     * Retrieves all workout and exercises join records from the database.
     * 
     * Each record in the table is mapped to a WorkoutExercise object and returned in a list.
     * If no matching record is found, an empty list is returned.
     * 
     * @return The list of WorkoutExercise objects in the database.
     */
    fun findAllWorkoutExercises(workoutID: Int): List<WorkoutExercise> {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM workout_exercises WHERE workoutID = ?",
            )

        stmt.setInt(1, workoutID)

        val rs: ResultSet = stmt.executeQuery()
        val workoutExs = mutableListOf<WorkoutExercise>()
        while (rs.next()) {
            val we =
                WorkoutExercise(
                    workoutExercisesID = rs.getInt("workoutExercisesID"),
                    workoutID = rs.getInt("workoutID"),
                    exerciseID = rs.getInt("exerciseID"),
                    sets = rs.getObject("sets") as Int?,
                    reps = rs.getObject("reps") as Int?,
                    weight = rs.getObject("weight") as Double?,
                )
            workoutExs.add(we)
        }

        rs.close()
        stmt.close()
        return workoutExs
    }

    /**
     * Updates a workout and exercise join record in the database.
     *
     * The function validates the provided workoutExercisesID, ensuring it's not impossible beforing executing the
     * statement. Sets, reps, and weight are also validated to ensure that if a value is given, that it is greater than 0.
     *
     * The function only updates the sets, reps, and weight associated with the workout, identified by the workoutExercisesID.
     *
     * If the update is successful, the database is modified and the function returns true. If no matching workout
     * is found or validation fails, false is returned.
     * 
     *
     * @param workoutExercisesID The unique identifier of the join record to update.
     * @param sets Optional sets of an exercise in the workout to update.
     * @param reps Optional new reps of a set in an exercise in a workout.
     * @param weight Optional new weight of an exercise in a rep.
     * @return True if the update was successful, false if not.
     */
    fun updateWorkoutExercise(
        workoutExercisesID: Int,
        sets: Int?,
        reps: Int?,
        weight: Double?,
    ): Boolean {
        // Validation Checks
        if (workoutExercisesID <= 0) return false
        if (sets != null && sets <= 0) return false
        if (reps != null && reps <= 0) return false
        if (weight != null && weight <= 0.0) return false

        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                UPDATE workout_exercises
                SET sets = ?, reps = ?, weight = ?
                WHERE workoutExercisesID = ?
                """.trimIndent(),
            )

        stmt.setObject(1, sets)
        stmt.setObject(2, reps)
        stmt.setObject(3, weight)
        stmt.setInt(4, workoutExercisesID)

        val rowsUpdated = stmt.executeUpdate()
        stmt.close()

        return rowsUpdated > 0
    }

    /**
     * Deletes a workout and exercise join record from the database using the provided workoutExercisesID.
     * 
     * The function executes a DELETE operation on the workout_exercises table if a workout_exercise is found linking
     * to the given workoutExercisesID and true is returned. Otherwise, false is returned.
     * 
     * @param workoutExercisesID The workoutExercisesID of the row to delete.
     * @return True if the join record was deleted, false if not.
     */
    fun deleteWorkoutExercise(workoutExercisesID: Int): Boolean {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "DELETE FROM workout_exercises WHERE workoutExercisesID = ?",
            )

        stmt.setInt(1, workoutExercisesID)

        val rowsDeleted = stmt.executeUpdate()
        stmt.close()

        return rowsDeleted > 0
    }

    // Statistics DB
    fun createStatistic(stat: Statistic): Int {
        // Validation Checks
        if (stat.userID <= 0 || stat.exerciseID <= 0) return -1
        if (stat.scoreValue <= 0.0) return -1
        if (stat.date.isBlank() || !stat.date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return -1

        val stmt =
            db.connection.prepareStatement(
                """
                INSERT INTO statistics(userID, exerciseID, scoreValue, date)
                VALUES (?, ?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS,
            )

        stmt.setInt(1, stat.userID)
        stmt.setInt(2, stat.exerciseID)
        stmt.setDouble(3, stat.scoreValue)
        stmt.setString(4, stat.date)

        val rowsInserted = stmt.executeUpdate()

        if (rowsInserted == 0) {
            stmt.close()
            return -1
        }

        val rs = stmt.generatedKeys
        val id = if (rs.next()) rs.getInt(1) else -1

        rs.close()
        stmt.close()

        return id
    }

    fun findAllStatistics(
        userID: Int,
        exerciseID: Int,
    ): List<Statistic> {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM statistics WHERE userID = ? AND exerciseID = ?",
            )

        stmt.setInt(1, userID)
        stmt.setInt(2, exerciseID)

        val rs: ResultSet = stmt.executeQuery()
        val stats = mutableListOf<Statistic>()
        while (rs.next()) {
            val stat =
                Statistic(
                    statisticID = rs.getInt("statisticID"),
                    userID = rs.getInt("userID"),
                    exerciseID = rs.getInt("exerciseID"),
                    scoreValue = rs.getDouble("scoreValue"),
                    date = rs.getString("date"),
                )
            stats.add(stat)
        }

        rs.close()
        stmt.close()
        return stats
    }

    fun getBestStat(
        userID: Int,
        exerciseID: Int,
    ): Statistic? {
        val stmt =
            db.connection.prepareStatement(
                """
                SELECT * FROM statistics
                WHERE userID = ? AND exerciseID = ?
                ORDER BY scoreValue DESC
                LIMIT 1
                """.trimIndent(),
            )

        stmt.setInt(1, userID)
        stmt.setInt(2, exerciseID)

        val rs = stmt.executeQuery()

        val stat =
            if (rs.next()) {
                Statistic(
                    rs.getInt("statisticID"),
                    rs.getInt("userID"),
                    rs.getInt("exerciseID"),
                    rs.getDouble("scoreValue"),
                    rs.getString("date"),
                )
            } else {
                null
            }

        rs.close()
        stmt.close()

        return stat
    }

    fun updateStatistic(
        statisticID: Int,
        scoreValue: Double,
        date: String,
    ): Boolean {
        // Validation Checks
        if (statisticID <= 0) return false
        if (scoreValue <= 0.0) return false
        if (date.isBlank() || !date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return false
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                UPDATE statistics
                SET scoreValue = ?, date = ?
                WHERE statisticID = ?
                """.trimIndent(),
            )

        stmt.setDouble(1, scoreValue)
        stmt.setString(2, date)
        stmt.setInt(3, statisticID)

        val rowsUpdated = stmt.executeUpdate()
        stmt.close()

        return rowsUpdated > 0
    }

    fun deleteStatistic(statisticID: Int): Boolean {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "DELETE FROM statistics WHERE statisticID = ?",
            )

        stmt.setInt(1, statisticID)

        val rowsDeleted = stmt.executeUpdate()
        stmt.close()

        return rowsDeleted > 0
    }

    // Competitions DB
    fun createCompetition(comp: Competition): Int {
        // Validation Checks
        if (comp.name.isBlank()) return -1
        if (comp.date.isBlank() || !comp.date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return -1
        if (comp.rank != null && comp.rank <= 0) return -1
        if (comp.result != null && comp.result <= 0) return -1

        val stmt =
            db.connection.prepareStatement(
                """
                INSERT INTO competitions(userID, name, date, rank, result)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS,
            )

        stmt.setInt(1, comp.userID)
        stmt.setString(2, comp.name)
        stmt.setString(3, comp.date)
        stmt.setObject(4, comp.rank)
        stmt.setObject(5, comp.result)

        val rowsInserted = stmt.executeUpdate()

        if (rowsInserted == 0) {
            stmt.close()
            return -1
        }

        val rs = stmt.generatedKeys
        val id = if (rs.next()) rs.getInt(1) else -1

        rs.close()
        stmt.close()

        return id
    }

    fun findAllCompetitions(userID: Int): List<Competition> {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM competitions WHERE userID = ?",
            )

        stmt.setInt(1, userID)

        val rs: ResultSet = stmt.executeQuery()
        val comps = mutableListOf<Competition>()
        while (rs.next()) {
            val comp =
                Competition(
                    competitionID = rs.getInt("competitionID"),
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    date = rs.getString("date"),
                    rank = rs.getObject("rank") as Int?,
                    result = rs.getObject("result") as Int?,
                )
            comps.add(comp)
        }

        rs.close()
        stmt.close()
        return comps
    }

    fun updateCompetition(
        competitionID: Int,
        name: String,
        date: String,
        rank: Int? = null,
        result: Int? = null,
    ): Boolean {
        // Validation Checks
        if (competitionID <= 0) return false
        if (name.isBlank()) return false
        if (date.isBlank() || !date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return false
        if (rank != null && rank <= 0) return false
        if (result != null && result <= 0) return false
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                UPDATE competitions
                SET name = ?, date = ?, rank = ?, result = ?
                WHERE competitionID = ?
                """.trimIndent(),
            )

        stmt.setString(1, name)
        stmt.setString(2, date)
        stmt.setObject(3, rank)
        stmt.setObject(4, result)
        stmt.setInt(5, competitionID)

        val rowsUpdated = stmt.executeUpdate()
        stmt.close()

        return rowsUpdated > 0
    }

    fun deleteCompetition(competitionID: Int): Boolean {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "DELETE FROM competitions WHERE competitionID = ?",
            )

        stmt.setInt(1, competitionID)

        val rowsDeleted = stmt.executeUpdate()
        stmt.close()

        return rowsDeleted > 0
    }

    // CompetitionsAndExercise DB
    fun addExercisetoCompetition(compEx: CompetitionExercise): Int {
        val stmt =
            db.connection.prepareStatement(
                """
                INSERT INTO competition_exercises(competitionID, exerciseID, performance)
                VALUES (?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS,
            )

        stmt.setInt(1, compEx.competitionID)
        stmt.setInt(2, compEx.exerciseID)
        stmt.setObject(3, compEx.performance)

        val rowsInserted = stmt.executeUpdate()

        if (rowsInserted == 0) {
            stmt.close()
            return -1
        }

        val rs = stmt.generatedKeys
        val id = if (rs.next()) rs.getInt(1) else -1

        rs.close()
        stmt.close()

        return id
    }

    fun findAllCompetitionExercises(competitionID: Int): List<CompetitionExercise> {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM competition_exercises WHERE competitionID = ?",
            )

        stmt.setInt(1, competitionID)

        val rs: ResultSet = stmt.executeQuery()
        val compExs = mutableListOf<CompetitionExercise>()
        while (rs.next()) {
            val compEx =
                CompetitionExercise(
                    competitionExerciseID = rs.getInt("competitionExerciseID"),
                    competitionID = rs.getInt("competitionID"),
                    exerciseID = rs.getInt("exerciseID"),
                    performance = rs.getObject("performance") as Double?,
                )
            compExs.add(compEx)
        }

        rs.close()
        stmt.close()
        return compExs
    }

    fun updateCompetitionExercise(
        competitionExerciseID: Int,
        performance: Double?,
    ): Boolean {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                UPDATE competition_exercises
                SET performance = ?
                WHERE competitionExerciseID = ?
                """.trimIndent(),
            )

        stmt.setObject(1, performance)
        stmt.setInt(2, competitionExerciseID)

        val rowsUpdated = stmt.executeUpdate()
        stmt.close()

        return rowsUpdated > 0
    }

    fun deleteCompetitionExercise(competitionExerciseID: Int): Boolean {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "DELETE FROM competition_exercises WHERE competitionExerciseID = ?",
            )

        stmt.setInt(1, competitionExerciseID)

        val rowsDeleted = stmt.executeUpdate()
        stmt.close()

        return rowsDeleted > 0
    }

    // Routes DB

    fun createRoute(route: Route): Int {
        // Validation Checks
        if (route.name.isBlank()) return -1
        if (route.distance <= 0.0) return -1
        if (route.estimatedTime != null && route.estimatedTime <= 0.0) return -1
        if (route.date.isBlank() || !route.date.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return -1

        val stmt =
            db.connection.prepareStatement(
                """
                INSERT INTO routes(userID, name, distance, estimatedTime, date, isPublic, routeData)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS,
            )

        stmt.setInt(1, route.userID)
        stmt.setString(2, route.name)
        stmt.setDouble(3, route.distance)
        stmt.setObject(4, route.estimatedTime)
        stmt.setString(5, route.date)
        stmt.setBoolean(6, route.isPublic)
        stmt.setString(7, route.routeData)

        val rowsInserted = stmt.executeUpdate()

        if (rowsInserted == 0) {
            stmt.close()
            return -1
        }

        val rs = stmt.generatedKeys
        val id = if (rs.next()) rs.getInt(1) else -1

        rs.close()
        stmt.close()

        return id
    }

    fun findAllRoutes(userID: Int): List<Route> {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM routes WHERE userID = ? or isPublic = 1",
            )

        stmt.setInt(1, userID)

        val rs: ResultSet = stmt.executeQuery()
        val routes = mutableListOf<Route>()
        while (rs.next()) {
            val route =
                Route(
                    routeID = rs.getInt("routeID"),
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    distance = rs.getDouble("distance"),
                    estimatedTime = rs.getObject("estimatedTime") as Double?,
                    date = rs.getString("date"),
                    isPublic = rs.getBoolean("isPublic"),
                    routeData = rs.getString("routeData"),
                )
            routes.add(route)
        }

        rs.close()
        stmt.close()
        return routes
    }

    fun updateRoute(
        routeID: Int,
        name: String,
        distance: Double,
        estimatedTime: Double?,
        date: String?,
        isPublic: Boolean,
        routeData: String,
    ): Boolean {
        // Validation Checks
        if (name.isBlank()) return false
        if (distance <= 0.0) return false
        if (estimatedTime != null && estimatedTime <= 0.0) return false
        if (date != null && (date.isBlank() || !date.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))) return false
        if (routeID <= 0) return false

        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                """
                UPDATE routes
                SET name = ?, distance = ?, estimatedTime = ?, date = ?, isPublic = ?, routeData = ?
                WHERE routeID = ?
                """.trimIndent(),
            )

        stmt.setString(1, name)
        stmt.setDouble(2, distance)
        stmt.setObject(3, estimatedTime)
        stmt.setString(4, date)
        stmt.setBoolean(5, isPublic)
        stmt.setString(6, routeData)
        stmt.setInt(7, routeID)

        val rowsUpdated = stmt.executeUpdate()
        stmt.close()

        return rowsUpdated > 0
    }

    fun deleteRoute(routeID: Int): Boolean {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "DELETE FROM routes WHERE routeID = ?",
            )

        stmt.setInt(1, routeID)

        val rowsDeleted = stmt.executeUpdate()
        stmt.close()

        return rowsDeleted > 0
    }

    // Individual find functions

    fun findRoute(routeID: Int): Route? {
        val stmt =
            db.connection.prepareStatement(
                "SELECT * FROM routes WHERE routeID = ?",
            )

        stmt.setInt(1, routeID)

        val rs = stmt.executeQuery()

        val route =
            if (rs.next()) {
                Route(
                    routeID = rs.getInt("routeID"),
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    distance = rs.getDouble("distance"),
                    estimatedTime = rs.getObject("estimatedTime") as Double?,
                    date = rs.getString("date"),
                    isPublic = rs.getBoolean("isPublic"),
                    routeData = rs.getString("routeData"),
                )
            } else {
                null
            }

        rs.close()
        stmt.close()

        return route
    }

    fun findWorkout(workoutID: Int): Workout? {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM workouts WHERE workoutID = ?",
            )

        stmt.setInt(1, workoutID)

        val rs: ResultSet = stmt.executeQuery()

        if (rs.next()) {
            val workout =
                Workout(
                    workoutID = rs.getInt("workoutID"),
                    createdBy = rs.getInt("createdBy"),
                    assignedTo = rs.getInt("assignedTo"),
                    date = rs.getString("date"),
                    duration = rs.getObject("duration") as Int?,
                    workoutType = rs.getObject("workoutType") as String?,
                    notes = rs.getObject("notes") as String?,
                    routeID = rs.getObject("routeID") as Int?,
                    isRoutine = rs.getBoolean("isRoutine"),
                )
            rs.close()
            stmt.close()
            return workout
        }

        rs.close()
        stmt.close()
        return null
    }

    fun findWorkoutExercise(workoutExercisesID: Int): WorkoutExercise? {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM workout_exercises WHERE workoutExercisesID = ?",
            )

        stmt.setInt(1, workoutExercisesID)

        val rs: ResultSet = stmt.executeQuery()
        if (rs.next()) {
            val we =
                WorkoutExercise(
                    workoutExercisesID = rs.getInt("workoutExercisesID"),
                    workoutID = rs.getInt("workoutID"),
                    exerciseID = rs.getInt("exerciseID"),
                    sets = rs.getObject("sets") as Int?,
                    reps = rs.getObject("reps") as Int?,
                    weight = rs.getObject("weight") as Double?,
                )
            rs.close()
            stmt.close()
            return we
        }

        rs.close()
        stmt.close()
        return null
    }

    fun findStatistic(statisticID: Int): Statistic? {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM statistics WHERE statisticID = ?",
            )

        stmt.setInt(1, statisticID)

        val rs: ResultSet = stmt.executeQuery()

        if (rs.next()) {
            val stat =
                Statistic(
                    statisticID = rs.getInt("statisticID"),
                    userID = rs.getInt("userID"),
                    exerciseID = rs.getInt("exerciseID"),
                    scoreValue = rs.getDouble("scoreValue"),
                    date = rs.getString("date"),
                )
            rs.close()
            stmt.close()
            return stat
        }

        rs.close()
        stmt.close()
        return null
    }

    fun findCompetition(competitionID: Int): Competition? {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM competitions WHERE competitionID = ?",
            )

        stmt.setInt(1, competitionID)

        val rs: ResultSet = stmt.executeQuery()
        if (rs.next()) {
            val comp =
                Competition(
                    competitionID = rs.getInt("competitionID"),
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    date = rs.getString("date"),
                    rank = rs.getObject("rank") as Int?,
                    result = rs.getObject("result") as Int?,
                )
            rs.close()
            stmt.close()
            return comp
        }

        rs.close()
        stmt.close()
        return null
    }

    fun findCompetitionExercise(competitionExerciseID: Int): CompetitionExercise? {
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM competition_exercises WHERE competitionExerciseID = ?",
            )

        stmt.setInt(1, competitionExerciseID)

        val rs: ResultSet = stmt.executeQuery()
        if (rs.next()) {
            val compEx =
                CompetitionExercise(
                    competitionExerciseID = rs.getInt("competitionExerciseID"),
                    competitionID = rs.getInt("competitionID"),
                    exerciseID = rs.getInt("exerciseID"),
                    performance = rs.getObject("performance") as Double?,
                )
            rs.close()
            stmt.close()
            return compEx
        }

        rs.close()
        stmt.close()
        return null
    }

    // Training groups

    fun createGroup(group: TrainingGroup): Int {
        db.connection.autoCommit = false // stops db from saving each statting - keeps atomicty
        // Validation checks
        if (group.name.isBlank()) return -1
        if (group.description != null && group.description.length > 500) return -1
        if (group.goal != null && group.goal.length > 500) return -1

        try {
            val stmt =
                db.connection.prepareStatement(
                    """
                    INSERT INTO training_groups(createdBy, name, description, goal)
                    VALUES (?, ?, ?, ?)
                    """.trimIndent(),
                    Statement.RETURN_GENERATED_KEYS,
                )
            stmt.setInt(1, group.createdBy)
            stmt.setString(2, group.name)
            stmt.setObject(3, group.description)
            stmt.setObject(4, group.goal)

            val rowsInserted = stmt.executeUpdate()
            // If group creation failed undo everything
            if (rowsInserted == 0) {
                db.connection.rollback()
                stmt.close()
                return -1
            }

            val rs = stmt.generatedKeys
            // Get new group ID
            val groupID = if (rs.next()) rs.getInt(1) else -1

            rs.close()
            stmt.close()

            // If that failed, retract what happend for atomicty
            if (groupID == -1) {
                db.connection.rollback()
                return -1
            }

            // Add creator to group
            if (!joinGroup(groupID, group.createdBy)) {
                // If fails undo the group creation too
                db.connection.rollback()
                return -1
            }
            // Group and member all successful
            db.connection.commit()
            return groupID
        } catch (error: Exception) {
            // If unexpected error undo everything
            db.connection.rollback()
            return -1
        } finally {
            // Always restore autocommit if failur eor success
            db.connection.autoCommit = true
        }
    }

    fun deleteGroup(groupID: Int): Boolean {
        val stmt =
            db.connection.prepareStatement(
                "DELETE FROM training_groups WHERE groupID = ?",
            )
        stmt.setInt(1, groupID)

        val rows = stmt.executeUpdate()
        stmt.close()

        return rows > 0
    }

    fun findGroup(groupID: Int): TrainingGroup? {
        val stmt =
            db.connection.prepareStatement(
                "SELECT * FROM training_groups WHERE groupID = ?",
            )
        stmt.setInt(1, groupID)

        val rs = stmt.executeQuery()

        val group =
            if (rs.next()) {
                TrainingGroup(
                    groupID = rs.getInt("groupID"),
                    createdBy = rs.getInt("createdBy"),
                    name = rs.getString("name"),
                    description = rs.getObject("description") as String?,
                    goal = rs.getObject("goal") as String?,
                )
            } else {
                null
            }

        rs.close()
        stmt.close()

        return group
    }

    fun findAllGroups(userID: Int): List<TrainingGroup> {
        val stmt =
            db.connection.prepareStatement(
                """
                SELECT training_groups.* FROM training_groups
                JOIN group_members ON training_groups.groupID = group_members.groupID
                WHERE group_members.userID = ?
                """.trimIndent(),
            )
        stmt.setInt(1, userID)

        val rs = stmt.executeQuery()
        val groups = mutableListOf<TrainingGroup>()

        while (rs.next()) {
            groups.add(
                TrainingGroup(
                    groupID = rs.getInt("groupID"),
                    createdBy = rs.getInt("createdBy"),
                    name = rs.getString("name"),
                    description = rs.getObject("description") as String?,
                    goal = rs.getObject("goal") as String?,
                ),
            )
        }
        rs.close()
        stmt.close()

        return groups
    }

    fun updateGroup(
        groupID: Int,
        name: String,
        description: String?,
        goal: String?,
    ): Boolean {
        // Validation Checks
        if (name.isBlank()) return false
        if (description != null && description.length > 500) return false
        if (goal != null && goal.length > 500) return false
        if (groupID <= 0) return false
        val stmt =
            db.connection.prepareStatement(
                """
                UPDATE training_groups SET name = ?, description = ?, goal = ?
                WHERE groupID = ?
                """.trimIndent(),
            )
        stmt.setString(1, name)
        stmt.setString(2, description)
        stmt.setObject(3, goal)
        stmt.setInt(4, groupID)

        val rows = stmt.executeUpdate()
        stmt.close()

        return rows > 0
    }

    // Group members

    fun joinGroup(
        groupID: Int,
        userID: Int,
    ): Boolean {
        // Validation Checks
        if (groupID <= 0 || userID <= 0) return false
        val members = findGroupMembers(groupID)
        if (members.any { it.userID == userID }) return false

        val stmt =
            db.connection.prepareStatement(
                "INSERT INTO group_members(groupID, userID) VALUES (?, ?)",
            )

        stmt.setInt(1, groupID)
        stmt.setInt(2, userID)

        val rows = stmt.executeUpdate()
        stmt.close()

        return rows > 0
    }

    fun leaveGroup(
        groupID: Int,
        userID: Int,
    ): Boolean {
        val stmt =
            db.connection.prepareStatement(
                "DELETE FROM group_members WHERE groupID = ? AND userID = ?",
            )
        stmt.setInt(1, groupID)
        stmt.setInt(2, userID)

        val rows = stmt.executeUpdate()
        stmt.close()

        return rows > 0
    }

    fun findGroupMembers(groupID: Int): List<User> {
        val stmt =
            db.connection.prepareStatement(
                """
                SELECT users.* FROM users
                JOIN group_members ON users.userID = group_members.userID
                WHERE group_members.groupID = ?
                """.trimIndent(),
            )
        stmt.setInt(1, groupID)

        val rs = stmt.executeQuery()
        val members = mutableListOf<User>()

        while (rs.next()) {
            members.add(
                User(
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    email = rs.getString("email"),
                    passwordHash = rs.getString("passwordHash"),
                    passwordKey = rs.getString("passwordKey"),
                    gender = rs.getObject("gender") as String?,
                    age = rs.getObject("age") as Int?,
                    height = rs.getObject("height") as Double?,
                    weight = rs.getObject("weight") as Double?,
                    accountType = rs.getBoolean("accountType"),
                ),
            )
        }

        rs.close()
        stmt.close()

        return members
    }

    // Group training sessions

    fun addGroupSession(session: GroupSession): Int {
        // Validation Checks
        if (session.groupID <= 0 || session.workoutID <= 0) return -1
        if (session.scheduledDate.isBlank() || !session.scheduledDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return -1

        val stmt =
            db.connection.prepareStatement(
                """
                INSERT INTO group_sessions(groupID, workoutID, scheduledDate)
                VALUES (?, ?, ?)
                """.trimIndent(),
                Statement.RETURN_GENERATED_KEYS,
            )
        stmt.setInt(1, session.groupID)
        stmt.setInt(2, session.workoutID)
        stmt.setString(3, session.scheduledDate)

        val rowsInserted = stmt.executeUpdate()

        if (rowsInserted == 0) {
            stmt.close()
            return -1
        }

        val rs = stmt.generatedKeys
        val id =
            if (rs.next()) {
                rs.getInt(1)
            } else {
                -1
            }

        rs.close()
        stmt.close()

        return id
    }

    fun getUpcomingGroupSessions(groupID: Int): List<GroupSession> {
        val today = LocalDate.now().toString()
        val stmt =
            db.connection.prepareStatement(
                """
                SELECT * FROM group_sessions
                WHERE groupID = ? AND scheduledDate >= ?
                ORDER BY scheduledDate ASC
                """.trimIndent(),
            )

        stmt.setInt(1, groupID)
        stmt.setString(2, today)

        val rs = stmt.executeQuery()
        val sessions = mutableListOf<GroupSession>()

        while (rs.next()) {
            sessions.add(
                GroupSession(
                    sessionID = rs.getInt("sessionID"),
                    groupID = rs.getInt("groupID"),
                    workoutID = rs.getInt("workoutID"),
                    scheduledDate = rs.getString("scheduledDate"),
                ),
            )
        }
        rs.close()
        stmt.close()

        return sessions
    }

    fun getPastGroupSessions(groupID: Int): List<GroupSession> {
        val today = LocalDate.now().toString()
        val stmt =
            db.connection.prepareStatement(
                """
                SELECT * FROM group_sessions
                WHERE groupID = ? AND scheduledDate < ?
                ORDER BY scheduledDate DESC
                """.trimIndent(),
            )
        stmt.setInt(1, groupID)
        stmt.setString(2, today)

        val rs = stmt.executeQuery()
        val sessions = mutableListOf<GroupSession>()

        while (rs.next()) {
            sessions.add(
                GroupSession(
                    sessionID = rs.getInt("sessionID"),
                    groupID = rs.getInt("groupID"),
                    workoutID = rs.getInt("workoutID"),
                    scheduledDate = rs.getString("scheduledDate"),
                ),
            )
        }
        rs.close()
        stmt.close()

        return sessions
    }

    fun deleteGroupSession(sessionID: Int): Boolean {
        val stmt =
            db.connection.prepareStatement(
                "DELETE FROM group_sessions WHERE sessionID = ?",
            )
        stmt.setInt(1, sessionID)

        val rows = stmt.executeUpdate()
        stmt.close()

        return rows > 0
    }

    // Functions for the dashboard of the frontend

    fun getRecentWorkouts(email: String): List<Workout> {
        val user = findUser(email) ?: return emptyList()
        val results = mutableListOf<Workout>()

        val query = """
            SELECT * FROM workouts
            WHERE assignedTo = ?
            ORDER BY date DESC
            LIMIT 5
        """

        val stmt = db.connection.prepareStatement(query)
        stmt.setInt(1, user.userID)

        val rs = stmt.executeQuery()

        while (rs.next()) {
            results.add(
                Workout(
                    workoutID = rs.getInt("workoutID"),
                    createdBy = rs.getInt("createdBy"),
                    assignedTo = rs.getInt("assignedTo"),
                    date = rs.getString("date"),
                    duration = rs.getObject("duration") as Int?,
                    workoutType = rs.getObject("workoutType") as String?,
                    notes = rs.getObject("notes") as String?,
                    routeID = rs.getObject("routeID") as Int?,
                    isRoutine = rs.getBoolean("isRoutine"),
                ),
            )
        }
        rs.close()
        stmt.close()

        return results
    }

    // Frontend potentially want to split it into 3 most recent of each
    fun getRecentWorkoutsByType(userID: Int): Map<String, List<Workout>> {
        return findAllWorkouts(
            userID,
        ).filter { it.workoutType != null }.sortedByDescending { it.date }.groupBy { it.workoutType!! }.mapValues {
                (_, workouts) ->
            workouts.take(3)
        }
    }

    fun getTodaysWorkout(
        email: String,
        today: LocalDate = LocalDate.now(),
    ): List<Workout> {
        val user = findUser(email) ?: return emptyList()
        val results = mutableListOf<Workout>()

        val query = """
            SELECT * FROM workouts
            WHERE assignedTo = ? AND date = ?
        """

        val stmt = db.connection.prepareStatement(query)
        stmt.setInt(1, user.userID)
        stmt.setString(2, today.toString()) // Local date finds the dates and to string makes it able to be stored in YYYY-MM-DD

        val rs = stmt.executeQuery()

        while (rs.next()) {
            results.add(
                Workout(
                    workoutID = rs.getInt("workoutID"),
                    createdBy = rs.getInt("createdBy"),
                    assignedTo = rs.getInt("assignedTo"),
                    date = rs.getString("date"),
                    duration = rs.getObject("duration") as Int?,
                    workoutType = rs.getObject("workoutType") as String?,
                    notes = rs.getObject("notes") as String?,
                    routeID = rs.getObject("routeID") as Int?,
                    isRoutine = rs.getBoolean("isRoutine")
                ),
            )
        }
        rs.close()
        stmt.close()

        return results
    }

    fun getUpcomingCompetitions(email: String): List<Competition> {
        val user = findUser(email) ?: return emptyList()

        val results = mutableListOf<Competition>()

        val query = """
            SELECT * FROM competitions
            WHERE userID = ? AND date >= ?
            ORDER BY date ASC
        """

        val stmt = db.connection.prepareStatement(query)
        stmt.setInt(1, user.userID)
        stmt.setString(2, LocalDate.now().toString())

        val rs = stmt.executeQuery()

        while (rs.next()) {
            results.add(
                Competition(
                    competitionID = rs.getInt("competitionID"),
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    date = rs.getString("date"),
                    rank = rs.getObject("rank") as Int?,
                    result = rs.getObject("result") as Int?,
                ),
            )
        }
        rs.close()
        stmt.close()

        return results
    }

    fun getRecentRoutes(email: String): List<Route> {
        val user = findUser(email) ?: return emptyList()

        val results = mutableListOf<Route>()

        val query = """
            SELECT * FROM routes
            WHERE userID = ?
            ORDER BY date DESC
            LIMIT 5
        """

        val stmt = db.connection.prepareStatement(query)
        stmt.setInt(1, user.userID)

        val rs = stmt.executeQuery()

        while (rs.next()) {
            results.add(
                Route(
                    routeID = rs.getInt("routeID"),
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    distance = rs.getDouble("distance"),
                    estimatedTime = rs.getObject("estimatedTime") as Double?,
                    date = rs.getString("date"),
                    isPublic = rs.getBoolean("isPublic"),
                    routeData = rs.getString("routeData"),
                ),
            )
        }
        rs.close()
        stmt.close()

        return results
    }

    fun getRecentCompetitions(email: String): List<Competition> {
        val user = findUser(email) ?: return emptyList()

        val results = mutableListOf<Competition>()

        val query = """
            SELECT * FROM competitions
            WHERE userID = ?
            ORDER BY date DESC
            LIMIT 5
        """

        val stmt = db.connection.prepareStatement(query)
        stmt.setInt(1, user.userID)

        val rs = stmt.executeQuery()

        while (rs.next()) {
            results.add(
                Competition(
                    competitionID = rs.getInt("competitionID"),
                    userID = rs.getInt("userID"),
                    name = rs.getString("name"),
                    date = rs.getString("date"),
                    rank = rs.getObject("rank") as Int?,
                    result = rs.getObject("result") as Int?,
                ),
            )
        }
        rs.close()
        stmt.close()

        return results
    }

    /**
     * Retrieves a user's completed workouts grouped by workout type.
     *
     * The function first retrieves all workouts assigned to the supplied userID.
     * It then excludes workouts with null workoutType and filters out any workouts with a date
     * scheduled for today or in the future, so only completed typed workouts are included.
     *
     * The remaining workouts are sorted in ascending date order and grouped by workout type, such as weights,
     *  running, swimming, or cycling.
     *
     * This is used to display progression statistics over time for different categories of training.
     *
     * @param userID The unique identifier of the user.
     * @return A map where the key is the workout type and the value is a list of completed workouts of that type,
     * ordered by date from oldest to newest.
     */
    fun getWorkoutProgression(userID: Int): Map<String, List<Workout>> {
        val today = LocalDate.now().toString()
        return findAllWorkouts(
            userID,
        ).filter { it.workoutType != null && it.date < today }.sortedBy { it.date }.groupBy { it.workoutType!! }
    }

    /**
     * Retrieves the total number of workouts completed by the user.
     *
     * The function first retrieves all workouts assigned to the supplied userID.
     * It then counts workouts the number of workouts completed by filtering out any workouts
     * with a date set today or after.
     *
     * This is used to display summary statistics such as the total number of training sessions completed.
     *
     * @param userID The unique identifier of the user.
     * @return The number of completed workouts.
     */
    fun getTotalWorkoutsCompleted(userID: Int): Int {
        val today = LocalDate.now().toString()
        return findAllWorkouts(userID).count { it.date < today }
    }

    /**
     * Retrieves the total number of hours the user has trained.
     *
     * The function first retrieves all workouts assigned to the supplied userID.
     * It then filters out any workouts with future dates so only completed training sessions are included.
     * It also filters out any workouts where no duration has been recorded as these workouts
     * cannot affect the total hours trained.
     *
     * The duration values are summed in minutes and converted into hours.
     *
     * This is used to display cumulative training volume statistics such as
     * total hours exercised over time.
     *
     * @param userID The unique identifier of the user.
     * @return The total completed training time in hours.
     */
    fun getHoursTrained(userID: Int): Double {
        return findAllWorkouts(
            userID,
        ).filter { it.date < LocalDate.now().toString() && it.duration != null }.sumOf { it.duration ?: 0 }.div(60.0)
    }

    /**
     * Retrieves the user's current training streak in days.
     *
     * The function first retrieves all workouts assigned to the supplied userID
     * and filters out any workouts with future dates so only completed workouts
     * are included.
     *
     * The completed workout dates are converted into LocalDate values and sorted with
     * duplicate values being removed. The list is then ordered from most revent the oldest.
     *
     * A streak begins from yesterday, as today's workout may not yet be fully
     * completed. The function then checks whether the user completed a workout on each
     * consecutive previous day.
     *
     * The streak increases by one for each uninterrupted day with at least one
     * completed workout. If a day is missed, the streak is set back to 0.
     *
     * Multiple workouts completed on the same day only count as one streak day.
     *
     * This is used to display consistency statistics and motivate regular training.
     *
     * @param userID The unique identifier of the user.
     * @return The number of consecutive days the user has completed workouts.
     */
    fun getCurrentStreak(userID: Int): Int {
        val today = LocalDate.now()
        val completed =
            findAllWorkouts(
                userID,
            ).filter { it.date < today.toString() }.map { LocalDate.parse(it.date) }.toSortedSet().reversed()

        var streak = 0
        var current = today.minusDays(1)

        for (date in completed) {
            if (date == current) {
                streak++
                current = current.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    /**
     * Retrieves a weekly summary of the user's completed training history.
     * 
     * The function first retrieves all workouts assigned to the supplied userID.
     * It then filters out any workouts not yet completed, or workouts where no duration
     * has been recorded.
     * 
     * The remaining workouts are grouped by calendar week, starting from monday
     * and ending at sunday.
     * 
     * For each week, the function calculates:
     *  - the number of completed workouts
     *  - the total training duration in hours
     * 
     * The duration is calculated by summing the duration stored in minutes and converting
     * the result into hours.
     * 
     * The returned map is sorted by week start date in ascending order.
     * 
     * This is used to display statistics such as weekly consistency and total training time
     * for each week.
     * @param userID The unique identifier for the user.
     * @return A map where the key is the week's starting date, and the value is another map containing:
     *  - "sessions": the number of workouts completed that week
     *  - "totalDuration": the total hours trained during that week
     */
    fun getWeeklyTrainingHistory(userID: Int): Map<String, Map<String, Any>> {
        val today = LocalDate.now()
        return findAllWorkouts(userID).filter { LocalDate.parse(it.date).isBefore(today) && it.duration != null }
            .groupBy { workout ->
                val date = LocalDate.parse(workout.date)
                val weekStart = date.with(DayOfWeek.MONDAY).toString()
                weekStart
            }
            .mapValues { (_, workouts) ->
                mapOf(
                    "sessions" to workouts.size,
                    "totalDuration" to workouts.sumOf { it.duration ?: 0 }.div(60.0),
                )
            }
            .toSortedMap()
    }

    /**
     * Retrieves a breakdown of exercise categories completed by a user.
     * 
     * The function first retrieves all workouts assigned to the supplied userID
     * and filters out any workouts with future dates so only completed workouts
     * are included.
     * 
     * For each workout, the workout_exercises and exercises tables are joined
     * to identify the category of each exercise. The number of occurrences of each
     * category is then counted.
     * 
     * After all categories have been counted, the function returns both the number
     * of completed sessions for each category, and the percentage share of the total
     * completed sessions each category has.
     * 
     * This is used to display statistics such as how much of a user's training consists of weights,
     * running, swimming, and other categories.
     * 
     * If the user has no completed workouts, an empty map is returned.
     * 
     * @param userID The unique identifier of the user.
     * @return A map where the key is the exercise category and the value is another map containing:
     *   - "sessions": the number of times that category appears
     *   - "percentage": the percentage share of total exercises completed
     */
    fun getExerciseBreakdown(userID: Int): Map<String, Map<String, Any>> {
        val workouts = findAllWorkouts(userID).filter { it.date < LocalDate.now().toString() }
        val workoutIDs: List<Int> = workouts.mapNotNull { it.workoutID }

        if (workoutIDs.isEmpty()) return emptyMap()

        val categoryCounts = mutableMapOf<String, Int>()

        for (workoutID in workoutIDs) {
            val stmt =
                db.connection.prepareStatement(
                    """
                SELECT exercises.category FROM workout_exercises
                JOIN exercises ON workout_exercises.exerciseID = exercises.exerciseID
                WHERE workout_exercises.workoutID = ?
            """,
                )

            // Joins tables (above)
            stmt.setInt(1, workoutID)
            val rs = stmt.executeQuery()
            // Determines how many categories there are and which fall into which category
            while (rs.next()) {
                val category = rs.getString("category")
                categoryCounts[category] = (categoryCounts[category] ?: 0) + 1
            }
            rs.close()
            stmt.close()
        }

        // Total number
        val total = categoryCounts.values.sum().toDouble()

        // Returns counts and percentage for each category
        return categoryCounts.mapValues { (_, count) ->
            mapOf(
                "sessions" to count,
                "percentage" to ((count / total) * 100).toInt(),
            )
        }
    }

    /**
     * Retrieves the highest score for each exercise completed by a user.
     * 
     * The function first retrieves all exercises stored in the database.
     * It then checks each exercise individually by calling getBestStat()
     * and returns the highest recorded statistic for the supplied user
     * where one exists.
     * 
     * This is used to display personal bests such as the heaviest weight
     * lifted, fastest time achieved, or highest score recorded, depending
     * on the exercise.
     * 
     * Exercises with no recorded statistics for the user are excluded using
     * mapNotNull().
     * 
     * @param userID The unique identifier of the user.
     * @return A list of Statistic objects containing the user's best
     * recorded result for each exercise.
     */
    fun getPersonalBests(userID: Int): List<Statistic> {
        return findAllExercises().mapNotNull { exercise -> getBestStat(userID, exercise.exerciseID) }
    }

    /**
     * Adds a workout and its related exercises to the database.
     *
     * The autoCommit is set to false so all operations can be committed or rolled back as a single transaction.
     * 
     * Calls createWorkout() store the given workout
     * in the database, then iterates through every exercise in the given list of
     * exercises and links them to the created workout by adding them to the join table in
     * the database.
     * 
     * If there is any error or failure during the function, the previous actions are undone
     * using rollback() so there is no partial data stored and -1 is returned.
     * 
     * If all operations succeed, commit() is called and the new workoutID
     * is returned.
     * 
     * Regardless of success or failure, autoCommit is set back to true so later database operations remain unaffected.
     *
     * @param workout An instance of the workout data class.
     * @param exercises A list of the WorkoutExercise objects.
     * @return The created workoutID if successful, -1 if the transaction fails.
     */
    fun logWorkout(
        workout: Workout,
        exercises: List<WorkoutExercise>,
    ): Int {
        db.connection.autoCommit = false // Allows grouping of statements (transactions) so atomic
        try {
            // Create workout, if fails undo everything
            val workoutID = createWorkout(workout)
            if (workoutID == -1) {
                db.connection.rollback()
                return -1
            }
            // Link each exercise to the workout, if fails undo everything
            for (exercise in exercises) {
                val we =
                    WorkoutExercise(
                        workoutExercisesID = 0,
                        workoutID = workoutID,
                        exerciseID = exercise.exerciseID,
                        sets = exercise.sets,
                        reps = exercise.reps,
                        weight = exercise.weight,
                    )
                if (addExerciseToWorkout(we) == -1) {
                    db.connection.rollback()
                    return -1
                }
            }
            // Commit all this if success
            db.connection.commit()
            return workoutID
        } catch (error: Exception) {
            // If any error undo everything
            db.connection.rollback()
            return -1
        } finally {
            db.connection.autoCommit = true // Restore autocommit
        }
    }

    fun deleteAllExercisesInWorkout(workoutID: Int): Boolean {
        val stmt: PreparedStatement = db.connection.prepareStatement(
            "DELETE FROM workout_exercises WHERE workoutID = ?"
        )

        stmt.setInt(1, workoutID)

        val rowsDeleted = stmt.executeUpdate()
        stmt.close()

        return rowsDeleted > 0
    }

    /**
     * Updates an existing workout and replaces its linked exercises.
     *
     * The autoCommit is set to false so all operations can be committed or rolled
     *  back as a single transaction.
     *
     * The function first updates the workout record in the workouts table.
     * It then removes any existing exercise links for that workout from the
     * workout_exercises join table before inserting the new list of exercises.
     *
     * Replacing all linked exercises ensures outdated exercise records are
     * removed and the workout accurately reflects the user's latest changes.
     *
     * If any part of the update fails, rollback() is called so no partial
     * changes are saved, and false is returned.
     *
     * If all operations succeed, commit() is called and true is returned.
     *
     * Regardless of success or failure, autoCommit is set back to true so
     * later database operations remain unaffected.
     *
     * @param workout The updated workout data to store.
     * @param exercises The revised list of WorkoutExercise objects to link to the workout.
     * @return True if the workout was updated successfully, false otherwise.
     */
    fun updateLoggedWorkout(
        workout: Workout,
        exercises: List<WorkoutExercise>,
    ): Boolean {
        // As usual, turn off autocommit as multiple statement function
        db.connection.autoCommit = false
        try {
            val updated =
                updateWorkout(workout.workoutID, workout.date, workout.duration, workout.workoutType, workout.notes, workout.routeID, workout.isRoutine)
            // Update workout failed so undo everything
            if (!updated) {
                db.connection.rollback()
                return false
            }
            // Delete all exercise links
            deleteAllExercisesInWorkout(workout.workoutID)

            // Add new exercise list
            for (exercise in exercises) {
                val result = addExerciseToWorkout(exercise.copy(workoutID = workout.workoutID))
                if (result == -1) {
                    // If link fails  undo everything
                    db.connection.rollback()
                    return false
                }
            }
            db.connection.commit()
            return true
        } catch (error: Exception) {
            db.connection.rollback()
            return false
        } finally {
            db.connection.autoCommit = true
        }
    }
}
