import model.OtpModel
import model.UserModel
import model.UserStatus
import com.mongodb.client.MongoClient
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import jakarta.ws.rs.ForbiddenException
import org.junit.jupiter.api.Assertions.assertEquals
import jakarta.inject.Inject
import core.GenericOtpService
import org.bson.types.ObjectId

@QuarkusTest
class GenericOtpServiceTest {

    @Inject lateinit var client: MongoClient
    var service: GenericOtpService = GenericOtpService(client, "test-otps")

    @Test
    fun `createOrRefresh actually creates a new otp`() {
        val targetUserId: ObjectId = ObjectId()
        service.createOrRefresh(targetUserId)
        val stored = service.findOrPanic(targetUserId)
        assertEquals(targetUserId, stored.userId)
        assertEquals(1, stored.iteration)
    }

    @Test
    fun `createOrRefresh actually refreshes an existing otp`() {
        val targetUserId: ObjectId = ObjectId()
        service.createOrRefresh(targetUserId)
        service.createOrRefresh(targetUserId)
        val stored = service.findOrPanic(targetUserId)
        assertEquals(targetUserId, stored.userId)
        assertEquals(2, stored.iteration)
    }

    @Test
    fun `verifyOtp succeeds with valid otp`() {
        val targetUserId: ObjectId = ObjectId()
        val stored = service.createOrRefresh(targetUserId)
        service.verifyOtp(stored.otp, stored)
    }

    @Test
    fun `verifyOtp fails with invalid otp`() {
        val targetUserId: ObjectId = ObjectId()
        val stored = service.createOrRefresh(targetUserId)
        assertThrows<ForbiddenException> {
            service.verifyOtp("invalid", stored)
            val updated = service.findOrPanic(stored.id!!)
            assertEquals(1, updated.attempts)
        }
    }

    @Test
    fun `verifyOtp fails on saturates attempts`() {
        val targetUserId: ObjectId = ObjectId()
        var otpCode = service.createOrRefresh(targetUserId)
        for (attempt in 1..5) {
            assertThrows<ForbiddenException> {
                otpCode = service.findOrPanic(targetUserId)
                service.verifyOtp("invalid", otpCode)
                assertEquals(attempt, otpCode.attempts)
            }
        }
        assertThrows<ForbiddenException> {
            otpCode = service.findOrPanic(targetUserId)
            service.verifyOtp(otpCode.otp, otpCode)
        }
    }
}