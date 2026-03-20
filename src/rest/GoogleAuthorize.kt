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
import model.UserStatus

const val GOOGLE_USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v2/userinfo"

@Path("/v1/oauth/authorize/google")
class GoogleAuthorize {

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
        @QueryParam("oauth-token")    queryParam:   String?,
        @HeaderParam("X-OAuth-Token") customHeader: String?,
        @HeaderParam("Authorization") authHeader:   String?
    ): Response  {
        val token = tokens.extractAuthenticationToken(queryParam, customHeader, authHeader)
        val userInfo = googleOAuthDecode(token)
        val email = JSONObject(userInfo).getString("email")
        val model = defaultUserModel(email)
        val result = users.findOrCreate(model)
        ensureAccountIsActive(result)
        return tokens.emitAuthorizedResponse(result)
    }

    fun ensureAccountIsActive(candidate: UserModel) {
        if (candidate.status != UserStatus.ACTIVE) {
            throw ForbiddenException("User account is not active")
        }
    }

    fun defaultUserModel(email: String) = UserModel(
        username = email.substringBefore("@"),
        email = email,
        tenant = "microchess",
        status = UserStatus.ACTIVE
    )

    fun googleOAuthDecode(token: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(GOOGLE_USERINFO_ENDPOINT))
            .header("Authorization", "Bearer $token")
            .GET()
            .build()
        
        val client = HttpClient.newHttpClient()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        
        return when (response.statusCode()) {
            200 -> response.body()
            else -> throw ForbiddenException(response.body())
        }
    }
}