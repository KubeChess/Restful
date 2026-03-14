package core

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

import model.UserModel

@ApplicationScoped
class UserService {

    private val collection: MongoCollection<UserModel>

    @Inject constructor(client: MongoClient) {
        this.collection = client.getDatabase("microchess")
            .getCollection("users", UserModel::class.java)
    }

    fun findOrCreate(candidate: UserModel): UserModel {
        val update = Updates.combine(
            Updates.setOnInsert("username", candidate.username),
            Updates.setOnInsert("email", candidate.email),
            Updates.setOnInsert("tenant", candidate.tenant),
            Updates.setOnInsert("status", candidate.status)
        )
        
        val options = FindOneAndUpdateOptions()
            .upsert(true)
            .returnDocument(ReturnDocument.AFTER)
        
        val filter = getMongoFilters(candidate)
        val error = InternalServerErrorException("Failed to create or find user")
        val user = collection.findOneAndUpdate(filter, update, options) ?: throw error
    
        return when (user.username) {
            candidate.username -> user
            else -> throw ClientErrorException("Username already taken", 409)
        }
    }

    fun createOrPanic(candidate: UserModel): InsertOneResult {
        val filter = getMongoFilters(candidate)
        val existing = collection.find(filter).first()
        return when (existing) {
            null -> throw ClientErrorException("User already exists", 409)
            else -> collection.insertOne(candidate)
        }
    }

    fun assertIsPresent(candidate: UserModel): UserModel {
        val filter = getMongoFilters(candidate)
        val existing = collection.find(filter).first()
        if (existing == null || existing != candidate) {
            throw NotFoundException("User not found")
        }
        return existing
    }

    fun findOrPanic(usernameOrEmail: String): UserModel {
        val filter = getMongoFilters(usernameOrEmail)
        val existing = collection.find(filter).first()
        return existing ?: throw NotFoundException("User not found")
    }

    fun getMongoFilters(usernameOrEmail: String) = Filters.or(
        Filters.eq("username", usernameOrEmail),
        Filters.eq("email", usernameOrEmail)
    )
    
    fun getMongoFilters(candidate: UserModel) = Filters.or(
        Filters.eq("username", candidate.username),
        Filters.eq("email", candidate.email),
        Filters.eq("id", candidate.id)
    )
}