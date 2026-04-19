package core

import io.quarkus.mailer.Mail
import io.quarkus.mailer.Mailer
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject

@ApplicationScoped
class EmailService {

    lateinit var mailer: Mailer

    @Inject constructor(mailer: Mailer) {
        this.mailer = mailer
    }

    fun sendEmail(to: String, subject: String, body: String) {
        mailer.send(
            Mail.withText(to, subject, body)
        )
    }

    fun sendHtmlEmail(to: String, subject: String, htmlBody: String) {
        mailer.send(
            Mail.withHtml(to, subject, htmlBody)
        )
    }

    fun sendVerificationEmail(to: String, otp: String) {
        mailer.send(
            Mail.withHtml(to, "Verify Your Email", "<p>Your verification code is: $otp</p>")
        )
    }

    fun sendPasswordResetEmail(to: String, otp: String) {
        mailer.send(
            Mail.withHtml(to, "Reset Your Password", "<p>Your password reset code is: $otp</p>")
        )
    }

    fun sendAccountDeletetionVerificationEmail(to: String, otp: String) {
        mailer.send(
            Mail.withHtml(to, "Delete Your Account", "<p>Your account deletion code is: $otp</p>")
        )
    }
}