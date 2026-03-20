package model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

data class OtpModel @BsonCreator constructor(

    @param:BsonId 
    @param:BsonProperty("_id")
    val id: ObjectId? = null,

    @param:BsonProperty("userId") 
    val userId:      ObjectId,

    @param:BsonProperty("otp") 
    val otp:         String,

    @param:BsonProperty("timestamp") 
    val timestamp:   Long,

    @param:BsonProperty("iteration") 
    val iteration:   Int,

    @param:BsonProperty("attempts") 
    val attempts:    Int,
)
