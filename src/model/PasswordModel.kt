package model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class PasswordModel @BsonCreator constructor(

    @param:BsonId 
    @param:BsonProperty("_id")
    val id: ObjectId? = null,

    @param:BsonProperty("userId") 
    val userId: ObjectId,

    @param:BsonProperty("hash") 
    val hash: String,

    @param:BsonProperty("timestamp") 
    val timestamp: Long,
)