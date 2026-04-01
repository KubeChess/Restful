package core

import com.mongodb.client.MongoCollection
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.MongoClient
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.client.model.FindOneAndUpdateOptions
import com.mongodb.client.model.ReturnDocument
import jakarta.inject.Singleton
import jakarta.inject.Inject
import jakarta.ws.rs.ClientErrorException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.InternalServerErrorException
import jakarta.ws.rs.ForbiddenException
import org.bson.types.ObjectId

import model.OtpModel
import model.UserModel

@Singleton
class RegistrationOtpService : GenericOtpService {
    @Inject constructor(client: MongoClient) 
        : super(client, "registration-otps") {}
}

@Singleton
class PasswordResetOtpService : GenericOtpService {
    @Inject constructor(client: MongoClient) 
        : super(client, "password-reset-otps") {}
}

@Singleton
class AccountDeletionOtpService : GenericOtpService {
    @Inject constructor(client: MongoClient) 
        : super(client, "account-deletion-otps") {}
}

open class GenericOtpService {

    private val collection: MongoCollection<OtpModel>
    private val secureRandom = java.security.SecureRandom()

    constructor(client: MongoClient, collectionName: String) {
        this.collection = client.getDatabase("microchess")
            .getCollection(collectionName, OtpModel::class.java)
    }

    fun createOrRefresh(userId: ObjectId): OtpModel {
        val filter = Filters.eq("userId", userId)
        val updates = Updates.combine(
            Updates.set("otp", generateOtp()),
            Updates.inc("iteration", 1),
            Updates.set("timestamp", System.currentTimeMillis()),
            Updates.set("attempts", 0)
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

    fun verifyOtp(otpText: String, otpModel: OtpModel) {
        if (otpText != otpModel.otp || otpModel.attempts >= 5) {
            val filter = Filters.eq("_id", otpModel.id)
            val updates = Updates.combine(
                Updates.inc("attempts", 1),
            )
            collection.updateOne(filter, updates)
            throw ForbiddenException("Invalid OTP code")
        }
    }
}