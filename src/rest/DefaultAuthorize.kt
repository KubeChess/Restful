package rest

import jakarta.ws.rs.core.*
import jakarta.ws.rs.*
import jakarta.inject.*
import java.net.http.*
import org.json.*
import java.net.*
import model.*
import core.*

@Path("/v1/auth/authorize/native")
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
        users.ensureAccountStillExists(model)
        return tokens.emitAuthorizedResponse(model)
    }
}