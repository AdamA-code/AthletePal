package services
import java.security.MessageDigest // Provides hashing algorithms
import java.security.SecureRandom // Used for generating random salt
import java.util.Base64 // Converts binary data into readable strings

class AuthService {

    fun validateLogin(username: String, password: String): Boolean {
        val user = findUser(email) // Load user from databsed

        if (user != null && verifyPassword(inputPassword, user.key, user.passwordHash)) {
            return True
        }
        return False
    }


    fun generateSalt() String {

        val random = SecureRandom() // Create secure random number generator
        val saltBytes = ByteArray(16) // Create array to hold random data
        random.nextBytes(saltBytes) // Fill array with random bytes
        
        return Base64.getEncoder().encodeToString(saltBytes) // Convert binary to text
    }

    fun passwordHash(password: String, salt: String) String {
        
        val digest = MessageDigest.getInstance("SHA-256") // Create a SHA-256 hashing object

        val combined = password + salt
        val hashBytes = digest.digest(combined.toByteArray()) // convert combined string to bytes and hash
        val encoded = Base64.getEncoder().encodeToString(hashBytes) // convert binary hash to readable text

        return encoded
    }

    fun verifyPassword(password: String, key: String, hash: String): Boolean {

        val newHash = passwordHash(password, key) // Recreate hash using the same password and key
        return newHash == storedHash // Return if it is equal to what is stored
    }

}