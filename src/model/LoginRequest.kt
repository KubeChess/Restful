package model

data class LoginRequest(
    val identity: String,
    val password: String,
)