package model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class OtpModel(
    
    @field:BsonId 
    val id: ObjectId? = null,

    val userId:      ObjectId,
    val otp:         String,
    val timestamp:   Long,
    val iteration:   Int,
    val attempts:    Int,
)
