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
import model.VerificationRequest
import model.UserModel

@Path("/v1/security/account/registration/verify")
class AccountVerify {

    private val users: UserService
    private val passwords: PasswordService
    private val controller: TokenService

    @Inject constructor(
        users: UserService,
        passwords: PasswordService, 
        controller: TokenService
    ) {
        this.users = users
        this.passwords = passwords
        this.controller = controller
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun verify(request: VerificationRequest): Response  {
        throw NotImplementedError("Not implemented yet")
    }
}