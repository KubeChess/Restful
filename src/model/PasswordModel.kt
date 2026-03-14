package model

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class PasswordModel(

    @field:BsonId 
    val id: ObjectId? = null,

    val userId: ObjectId,
    val hash:   String,
    val salt:   String
)