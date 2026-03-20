package model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class UserModel @BsonCreator constructor(

    @param:BsonId 
    @param:BsonProperty("_id")
    val id: ObjectId? = null,

    @param:BsonProperty("username")
    val username: String,

    @param:BsonProperty("email")
    val email: String,

    @param:BsonProperty("tenant")
    val tenant: String,

    @param:BsonProperty("status")
    val status: UserStatus,
)