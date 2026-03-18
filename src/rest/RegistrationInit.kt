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
import core.OtpService

import model.RegistrationRequest
import model.UserModel
import model.UserStatus

@Path("/v1/security/account/registration/init")
class RegistrationInit {

    private val users: UserService
    private val passwords: PasswordService
    private val tokens: TokenService
    private val otpCodes: OtpService
    private val mailing: EmailService

    @Inject constructor(
        users: UserService,
        passwords: PasswordService, 
        otpCodes: OtpService,
        mailing: EmailService,
        tokens: TokenService
    ) {
        this.users = users
        this.passwords = passwords
        this.tokens = tokens
        this.otpCodes = otpCodes
        this.mailing = mailing
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun register(request: RegistrationRequest): Response  {
        val user = UserModel(
            username = request.username,
            email = request.email,
            tenant = "microchess",
            status = UserStatus.PENDING
        )
        val created = users.createOrPanic(user)
        val salt = passwords.generateSalt()
        val password = passwords.hashPassword(request.password, salt, created)
        passwords.createOrUpdate(password)
        val otpData = otpCodes.createOrRefresh(created)
        mailing.sendVerificationEmail(request.email, otpData.otp)
        return Response.ok().build()
    }
}