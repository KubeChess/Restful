package rest

import com.mongodb.client.MongoClient
import jakarta.ws.rs.core.*
import jakarta.ws.rs.*
import jakarta.inject.*
import java.net.http.*
import org.json.*
import java.net.*
import model.*
import core.*

@Path("/v1/auth/account-delete/verify")
class AccountDeleteVerify {

    private val users: UserService
    private val passwords: PasswordService
    private val tokens: TokenService
    private val otpCodes: AccountDeletionOtpService
    private val client: MongoClient

    @Inject constructor(
        mongoClient: MongoClient,
        users: UserService,
        passwords: PasswordService, 
        otpCodes: AccountDeletionOtpService,
        tokens: TokenService
    ) {
        this.client = mongoClient
        this.users = users
        this.passwords = passwords
        this.tokens = tokens
        this.otpCodes = otpCodes
    }

    @POST @Produces(MediaType.APPLICATION_JSON)
    fun delete(
        @QueryParam("jwt-token")      queryParam:   String?,
        @HeaderParam("X-Jwt-Token")   customHeader: String?,
        @HeaderParam("Authorization") authHeader:   String?,
        @QueryParam("X-Otp-Code")     otpCode:      String?
    ): Response {
        val token = tokens.extractAuthenticationToken(queryParam, customHeader, authHeader)
        val model = tokens.parseJsonWebToken(token)
        val userId = model.id!!
        users.ensureAccountStillExists(model)
        client.startSession().use { 
            session -> session.withTransaction {
                users.deleteByUserId(userId)
                passwords.deleteByUserId(userId)
            }
        }
        return Response.ok().build()
    }
}