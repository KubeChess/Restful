package core

import at.favre.lib.crypto.bcrypt.BCrypt
import com.mongodb.client.MongoCollection
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.MongoClient
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.ws.rs.ClientErrorException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.InternalServerErrorException
import org.bson.types.ObjectId
import model.PasswordModel
import model.UserModel

@ApplicationScoped
class PasswordService {

    private val collection: MongoCollection<PasswordModel>
    private val secureRandom = java.security.SecureRandom()

    @Inject constructor(client: MongoClient) {
        this.collection = client.getDatabase("microchess")
            .getCollection("passwords", PasswordModel::class.java)
    }

    fun createOrUpdate(candidate: PasswordModel): PasswordModel {
        val filter = Filters.eq("userId", candidate.userId)
        val updates = Updates.combine(
            Updates.set("hash", candidate.hash),
            Updates.set("salt", candidate.salt)
        )
        val options = FindOneAndUpdateOptions()
            .returnDocument(ReturnDocument.AFTER)
            .upsert(true)
        return collection.findOneAndUpdate(filter, updates, options)
            ?: throw InternalServerErrorException("Failed to create or update password")
    }

    fun findOrPanic(userId: ObjectId): PasswordModel {
        val filter = Filters.eq("userId", userId)
        val existing = collection.find(filter).first()
        return existing ?: throw NotFoundException("Password not found")
    }

    fun generateSalt(): String {
        val max = 1000000
        val value = secureRandom.nextInt(max)
        return value.toString().padStart(6, '0')
    }

    fun hashPassword(plainPassword: String, salt: String, user: UserModel): PasswordModel {
        val salted = plainPassword + salt
        val hash = BCrypt.withDefaults().hashToString(12, salted.toCharArray())
        val userId = user.id ?: throw IllegalArgumentException("User ID cannot be null")
        return PasswordModel(
            userId = userId,
            hash = hash,
            salt = salt
        )
    }

    fun verifyPassword(plainPassword: String, password: PasswordModel): Boolean {
        val salted = plainPassword + password.salt
        return BCrypt.verifyer().verify(salted.toCharArray(), password.hash).verified
    }
}