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
import core.RegistrationOtpService

import model.UserModel
import model.UserStatus

@Path("/v1/security/account/registration/retry")
class RegistrationRetry {

    private val users: UserService
    private val passwords: PasswordService
    private val tokens: TokenService
    private val otpCodes: RegistrationOtpService
    private val mailing: EmailService

    @Inject constructor(
        users: UserService,
        passwords: PasswordService, 
        otpCodes: RegistrationOtpService,
        mailing: EmailService,
        tokens: TokenService
    ) {
        this.users = users
        this.passwords = passwords
        this.tokens = tokens
        this.otpCodes = otpCodes
        this.mailing = mailing
    }

    data class RequestBody(
        val email: String
    )

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun retry(request: RequestBody): Response  {
        val account = users.findOrPanic(request.email)
        when (account.status) {
            UserStatus.PENDING -> {}
            UserStatus.ACTIVE -> return Response.notModified().build()
            else -> throw ForbiddenException("Account is not pending verification")
        }
        val otpData = otpCodes.createOrRefresh(account.id!!)
        mailing.sendVerificationEmail(request.email, otpData.otp)
        return Response.ok().build()
    }
}