package rest

import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
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
import model.UserModel

@Path("/v1/security/jwt/authorize")
class DefaultAuthorize {

    private val users: UserService
    private val tokens: TokenService

    @Inject constructor(
        users: UserService,
        tokens: TokenService
    ) {
        this.users = users
        this.tokens = tokens
    }

    @POST @Produces(MediaType.APPLICATION_JSON)
    fun authorize(
        @QueryParam("jwt-token")      queryParam:   String?,
        @HeaderParam("X-Jwt-Token")   customHeader: String?,
        @HeaderParam("Authorization") authHeader:   String?
    ): Response  {
        val token = tokens.extractAuthenticationToken(queryParam, customHeader, authHeader)
        val model = tokens.parseJsonWebToken(token)
        ensureAccountStillExists(model)
        return tokens.emitAuthorizedResponse(model)
    }

    fun ensureAccountStillExists(candidate: UserModel) {
        val user = users.findOrPanic(candidate.username)
        if (user != candidate) {
            throw ForbiddenException("User account no longer exists or has changed")
        }
        if (user.status != model.UserStatus.ACTIVE) {
            throw ForbiddenException("User account is not active")
        }
    }
}