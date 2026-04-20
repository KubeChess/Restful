package rest

import jakarta.ws.rs.core.*
import jakarta.ws.rs.*
import jakarta.inject.*
import java.net.http.*
import org.json.*
import java.net.*
import model.*
import core.*

@Path("/v1/auth/password-reset/verify")
class PasswordResetVerify {

    private val users: UserService
    private val passwords: PasswordService
    private val tokens: TokenService
    private val otpCodes: PasswordResetOtpService

    @Inject constructor(
        users: UserService,
        passwords: PasswordService, 
        otpCodes: PasswordResetOtpService,
        tokens: TokenService
    ) {
        this.users = users
        this.passwords = passwords
        this.tokens = tokens
        this.otpCodes = otpCodes
    }

    data class RequestBody(
        val identity:    String,
        val otpCode:     String,
        val newPassword: String,
    )

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun verify(request: RequestBody): Response  {
        val account = users.findOrPanic(request.identity)
        val password = passwords.findOrPanic(account.id!!)
        val reference = otpCodes.findOrPanic(account.id)
        otpCodes.verifyOtp(request.otpCode, reference)
        passwords.createOrUpdate(request.newPassword, account.id)
        return tokens.emitAuthorizedResponse(account)
    }
}