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
import model.OtpModel
import model.UserModel

@ApplicationScoped
class OtpService {

    private val collection: MongoCollection<OtpModel>
    private val secureRandom = java.security.SecureRandom()

    @Inject constructor(client: MongoClient) {
        this.collection = client.getDatabase("microchess")
            .getCollection("otps", OtpModel::class.java)
    }

    fun createOrUpdate(userId: ObjectId): OtpModel {
        val filter = Filters.eq("userId", userId)
        val updates = Updates.combine(
            Updates.set("otp", generateOtp())
        )
        val options = FindOneAndUpdateOptions()
            .returnDocument(ReturnDocument.AFTER)
            .upsert(true)
        return collection.findOneAndUpdate(filter, updates, options)
            ?: throw InternalServerErrorException("Failed to create or update otp")
    }

    fun generateOtp(): String {
        val max = 1000000
        val value = secureRandom.nextInt(max)
        return value.toString().padStart(6, '0')
    }

    fun findOrPanic(userId: ObjectId): OtpModel {
        val filter = Filters.eq("userId", userId)
        val existing = collection.find(filter).first()
        return existing ?: throw NotFoundException("Otp not found")
    }

    fun verifyOtp(otpText: String, otpModel: OtpModel): Boolean {
        return otpText == otpModel.otp
    }
}