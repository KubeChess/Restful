package model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class UserModel(
    
    @field:BsonId 
    val id: ObjectId? = null,
    
    val username: String,
    val email:    String,
    val tenant:   String,
    val status:   UserStatus,
)