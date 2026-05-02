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

// Data classes to finitely store inputs (and potentially outputs) to database

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
     * @property exerciseID Unique identifier for the exercise.
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
     * @property workoutID Unique identifier for the workout.
     * @property createdBy The userID of the user who created the workout.
     * @property assignedTo The userID of the user who will be attempting the workout.
     * @property date The date the workout is scheduled for.
     * @property duration Optional duration of the workout.
     * @property workoutType Optional type of the workout such as weights or swimming.
     * @property notes Optional notes added to the workout.
     * @property routeID Optional routeID to link cardio workouts to a route on a map.
     * @property isRoutine True if the workout is aa regular routine, false if not.
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
     * @property workoutExercisesID Unique identifier for the exercise in the workout.
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

    @Serializable
    data class Statistic(
        val statisticID: Int? = 0,
        val userID: Int,
        val exerciseID: Int,
        val scoreValue: Double,
        val date: String,
    )

    @Serializable
    data class Competition(
        val competitionID: Int? = 0,
        val userID: Int,
        val name: String,
        val date: String,
        val rank: Int? = null,
        val result: Int? = null,
    )

    @Serializable
    data class CompetitionExercise(
        val competitionExerciseID: Int? = 0,
        val competitionID: Int,
        val exerciseID: Int,
        val performance: Double?,
    )

    @Serializable
    data class Route(
        val routeID: Int? = 0,
        val userID: Int,
        val name: String,
        val distance: Double,
        val estimatedTime: Double? = null,
        val date: String,
        val isPublic: Boolean,
        val routeData: String,
    )

    @Serializable
    data class TrainingGroup(
        val groupID: Int? = 0,
        val createdBy: Int,
        val name: String,
        val description: String? = null,
        val goal: String? = null
    )

    @Serializable
    data class GroupMember(
        val memberID: Int? = 0,
        val groupID: Int,
        val userID: Int,
    )

    @Serializable
    data class GroupSession(
        val sessionID: Int? = 0,
        val groupID: Int,
        val workoutID: Int,
        val scheduledDate: String,
    )

    // Connect to DB
    class DatabaseConnection {
        // open connection to database
        val connectionLocation = "jdbc:sqlite:${System.getProperty("user.dir")}/src/main/resources/users.db"
        val connection: Connection = DriverManager.getConnection(connectionLocation)

        // Runs automatically on creation
        init {
            connection.createStatement().execute("PRAGMA foreign_keys = ON") // Enable deletion cascading
        }

        fun close() {
            if (!connection.isClosed) connection.close()
        }
    }

    val db = DatabaseConnection()
    // User db

    fun createUser(user: User): Int {
        if (findUser(user.email) == null) {
            // Connect to db, pepare query struct, use object that allows updating
            // and prevent SQL injection, store to set values and exec
            // SQL INSERT statement with "?"" as a placeholder
            val stmt: PreparedStatement =
                db.connection.prepareStatement(
                    """
                INSERT INTO users(name, email, passwordHash, passwordKey, gender, age, height, weight, accountType)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """
                        .trimIndent(),
                    Statement.RETURN_GENERATED_KEYS,
                ) // Needed to return created ID

            // Set placeholder values based on given data
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

    fun findUser(email: String): User? {
        // Connect to db, pepare query struct, use object that allows updating
        // and prevent SQL injection, store to set values and exec
        val stmt: PreparedStatement =
            db.connection.prepareStatement(
                "SELECT * FROM users WHERE email = ?",
            )

        stmt.setString(1, email) // Put email value in placeholder (?)

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

    fun updateAccount(
        email: String,
        gender: String?,
        age: Int?,
        height: Double?,
        weight: Double?,
    ): Boolean {
        // Connect to db, pepare query struct, use object that allows updating
        // and prevent SQL injection, store to set values and exec

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

        // Set placeholder values based on given data
        stmt.setString(1, gender)
        stmt.setObject(2, age)
        stmt.setObject(3, height)
        stmt.setObject(4, weight)
        stmt.setString(5, email)

        val rowsUpdated = stmt.executeUpdate() // smt.executeUpdate returns 0 if nothing updated and 1 if updated
        stmt.close()

        return rowsUpdated > 0 // Returns True is rowsUpdated == 1
    }

    // Voluntary change, not forgot password
    fun updatePassword(
        email: String,
        oldPassword: String,
        newPassword: String,
    ): Boolean {
        // Validation checks
        if (oldPassword.isBlank() || newPassword.isBlank() || newPassword.length < 8) return false
        val user = findUser(email) ?: return false

        val authService = AuthService()

        // Verify old password is correct via reverse of func
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

    // Forgot password (email token)
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
        } catch (erro: Exception) {
            db.connection.rollback()
            return false
        } finally {
            db.connection.autoCommit = true
        }
    }

    /*
    This function is responsible for making the API call to the Postmark API, allowing for the email containing the token
    to be sent to the email entered by the user - thereby verifying they have access to the account email. As we do not own a
    private domain, and public email providers are not permitted as senders on Postmark, we are having to use a university
    email. This is a limitation which would not exist in a real-world implamentation, but serves as a proof of concept.

    We expect the target email which we are to send to the API (to then send an email to) to be passed as a parameter to
    the function (as a String data type).

    The function returns:
        - true, if we generate the token and a successful API request containing the email is made to Postmark (and we recieve confirmation)
        - false, if we cannot generate the token OR Postmark does not send confirmation of email recieved OR we hit a logical exception
    */
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

    // Exercise db

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

    // Workout to DB
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

    // Workout_exercise join table

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

    // Functions for the stats page
    fun getWorkoutProgression(userID: Int): Map<String, List<Workout>> {
        val today = LocalDate.now().toString()
        return findAllWorkouts(
            userID,
        ).filter { it.workoutType != null && it.date < today }.sortedBy { it.date }.groupBy { it.workoutType!! }
    }

    fun getTotalWorkoutsCompleted(userID: Int): Int {
        val today = LocalDate.now().toString()
        return findAllWorkouts(userID).count { it.date < today }
    }

    // Hours trained
    fun getHoursTrained(userID: Int): Double {
        return findAllWorkouts(
            userID,
        ).filter { it.date < LocalDate.now().toString() && it.duration != null }.sumOf { it.duration ?: 0 }.div(60.0)
    }

    // Workout Streak
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

    // Weekly training history
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

    // Exercise by category
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

    // For personal bests
    fun getPersonalBests(userID: Int): List<Statistic> {
        return findAllExercises().mapNotNull { exercise -> getBestStat(userID, exercise.exerciseID) }
    }

    // Log workout for workout logs page
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

    // Update a logged workout (in case of a changed exercise or another detail)
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
