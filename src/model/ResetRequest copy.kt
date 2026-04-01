package model

data class PasswordResetRequest(
    val newPassword: String,
    val identity: String,
)