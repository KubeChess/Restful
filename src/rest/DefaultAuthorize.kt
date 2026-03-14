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
    private val controller: TokenService

    @Inject constructor(
        users: UserService,
        controller: TokenService
    ) {
        this.users = users
        this.controller = controller
    }

    @POST @Produces(MediaType.APPLICATION_JSON)
    fun authorize(
        @QueryParam("jwt-token")      queryParam:   String?,
        @HeaderParam("X-Jwt-Token")   customHeader: String?,
        @HeaderParam("Authorization") authHeader:   String?
    ): Response  {
        val token = controller.extractAuthenticationToken(queryParam, customHeader, authHeader)
        val model = controller.parseJsonWebToken(token)
        users.assertIsPresent(model)
        return controller.emitAuthorizedResponse(model)
    }
}