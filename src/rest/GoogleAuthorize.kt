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

const val GOOGLE_USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v2/userinfo"

@Path("/v1/oauth/google/authorize")
class GoogleAuthorize {

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
        @QueryParam("oauth-token")    queryParam:   String?,
        @HeaderParam("X-OAuth-Token") customHeader: String?,
        @HeaderParam("Authorization") authHeader:   String?
    ): Response  {
        val token = controller.extractAuthenticationToken(queryParam, customHeader, authHeader)
        val userInfo = googleOAuthDecode(token)
        val email = JSONObject(userInfo).getString("email")
        val username = email.substringBefore("@")
        val model = UserModel(username = username, email = email, tenant = "google", status = "verified")
        val result = users.findOrCreate(model)
        return controller.emitAuthorizedResponse(result)
    }

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