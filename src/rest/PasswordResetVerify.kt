package rest

import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.HeaderParam
import jakarta.ws.rs.core.MediaType
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpClient
import java.net.http.HttpResponse
import jakarta.ws.rs.ForbiddenException
import jakarta.inject.Inject
import org.json.JSONObject

import core.TokenService
import core.UserService
import core.PasswordService
import core.EmailService
import core.PasswordResetOtpService

import model.PasswordResetVerificationRequest
import model.UserModel
import model.UserStatus

@Path("/v1/security/account/password/reset/verify")
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun verify(request: PasswordResetVerificationRequest): Response  {
        val account = users.findOrPanic(request.identity)
        val password = passwords.findOrPanic(account.id!!)
        val reference = otpCodes.findOrPanic(account.id)
        otpCodes.verifyOtp(request.otpCode, reference)
        passwords.createOrUpdate(request.newPassword, account.id)
        return tokens.emitAuthorizedResponse(account)
    }
}