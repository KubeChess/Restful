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

import model.ResetRequest
import model.UserModel
import model.UserStatus

@Path("/v1/security/account/password/reset/init")
class PasswordResetInit {

    private val users: UserService
    private val passwords: PasswordService
    private val otpCodes: PasswordResetOtpService
    private val mailing: EmailService

    @Inject constructor(
        users: UserService,
        passwords: PasswordService, 
        otpCodes: PasswordResetOtpService,
        mailing: EmailService
    ) {
        this.users = users
        this.passwords = passwords
        this.otpCodes = otpCodes
        this.mailing = mailing
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun reset(request: ResetRequest): Response  {
        val account = users.findOrPanic(request.identity)
        if (account.tenant != "microchess") {
            throw ForbiddenException("Account registered as passwordless")
        }
        val otpData = otpCodes.createOrRefresh(account.id!!)
        mailing.sendPasswordResetEmail(request.email, otpData.otp)
        return Response.ok().build()
    }
}