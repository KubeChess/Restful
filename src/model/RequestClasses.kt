package model

/*---------------- LOGIN AND REGISTRATION ----------------*/

data class LoginRequest(
    val identity: String,
    val password: String,
)

data class RegistrationRequest(
    val username: String,
    val email:    String,
    val password: String,
)

data class RegistrationVerificationRequest(
    val identity: String,
    val otpCode:  String,
)

data class RegistrationRetryRequest(
    val email: String
)

/*------------------ PASSWORD RESET ------------------*/

data class PasswordResetRequest(
    val identity: String,
)

data class PasswordResetVerificationRequest(
    val identity:    String,
    val otpCode:     String,
    val newPassword: String,
)
