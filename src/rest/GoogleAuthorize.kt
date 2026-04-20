package rest

import jakarta.ws.rs.core.*
import jakarta.ws.rs.*
import jakarta.inject.*
import java.net.http.*
import org.json.*
import java.net.*
import model.*
import core.*

const val GOOGLE_USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v2/userinfo"

@Path("/v1/auth/authorize/google")
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