package services
import java.security.MessageDigest // Provides hashing algorithms
import java.security.SecureRandom // Used for generating random salt
import java.util.Base64 // Converts binary data into readable strings

class AuthService {
    val databaseService = DatabaseService()

    fun validateLogin(
        email: String,
        password: String,
    ): Boolean {
        val user = databaseService.findUser(email) // Load user from databsed

        if (user != null && verifyPassword(password, user.passwordKey, user.passwordHash)) {
            return true
        }
        return false
    }

    fun generateSalt(): String {
        val random = SecureRandom() // Create secure random number generator
        val saltBytes = ByteArray(16) // Create array to hold random data
        random.nextBytes(saltBytes) // Fill array with random bytes

        return Base64.getEncoder().encodeToString(saltBytes) // Convert binary to text
    }

    fun hashPassword(
        password: String,
        salt: String,
    ): String {
        val digest = MessageDigest.getInstance("SHA-256") // Create a SHA-256 hashing object

        val combined = password + salt
        val hashBytes = digest.digest(combined.toByteArray()) // convert combined string to bytes and hash
        val encoded = Base64.getEncoder().encodeToString(hashBytes) // convert binary hash to readable text

        return encoded
    }

    fun verifyPassword(
        password: String,
        passwordKey: String,
        passwordHash: String,
    ): Boolean {
        val newHash = hashPassword(password, passwordKey) // Recreate hash using the same password and key
        return newHash == passwordHash // Return if it is equal to what is stored
    }

    fun registerUser(
        name: String,
        email: String,
        password: String,
        accountType: Boolean,
    ): Int {
        val key = generateSalt()
        val hash = hashPassword(password, key)

        val user =
            DatabaseService.User(
                0,
                name,
                email,
                hash,
                key,
                null,
                null,
                null,
                null,
                accountType,
            )

        return databaseService.createUser(user)
    }
}
