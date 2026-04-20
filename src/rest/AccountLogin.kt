package rest

import jakarta.ws.rs.core.*
import jakarta.ws.rs.*
import jakarta.inject.*
import java.net.http.*
import org.json.*
import java.net.*
import model.*
import core.*

@Path("/v1/auth/login/native")
class AccountLogin {

    private val users: UserService
    private val passwords: PasswordService
    private val tokens: TokenService

    @Inject constructor(
        users: UserService,
        passwords: PasswordService, 
        tokens: TokenService
    ) {
        this.users = users
        this.passwords = passwords
        this.tokens = tokens
    }

    data class RequestBody(
        val identity: String,
        val password: String,
    )

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun login(request: RequestBody): Response  {
        val user = users.findOrPanic(request.identity)
        val password = passwords.findOrPanic(user.id!!)
        passwords.verifyPassword(request.password, password)
        return tokens.emitAuthorizedResponse(user)
    }
}