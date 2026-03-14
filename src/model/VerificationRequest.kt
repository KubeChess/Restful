package model

data class VerificationRequest(
    val identity: String,
    val otpCode:  String,
)