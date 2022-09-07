package com.projet.sluca.smallbrother.libs

import android.os.Environment
import android.util.Log
import java.security.Security
import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

//TODO this part will have to change if we use Signal api
class Sender(private val user: String, private val password: String) : Authenticator() {
    private val mailhost = "smtp.gmail.com"
    private val session: Session

    companion object {
        init {
            Security.addProvider(JSSEProvider())
        }
    }

    override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(user, password)
    }

    @Synchronized
    @Throws(Exception::class)
    fun sendMail(subject: String?, body: String, sender: String?, recipients: String) {
        try {
            val message = MimeMessage(session)
            val handler = DataHandler(ByteArrayDataSource(body.toByteArray(), "text/plain"))
            message.sender = InternetAddress(sender)
            message.subject = subject
            message.dataHandler = handler


            // Nom du fichier à joindre.
            val nomFichier = "situation_partenaire.zip"
            // Chemin vers lui.
            val urlFichier =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    .toString() + "/SmallBrother/" +
                        nomFichier
            // Inclusion du corps du message (texte).
            val messageBodyPart: BodyPart = MimeBodyPart()
            messageBodyPart.setText(body)
            messageBodyPart.setContent(body, "text/html")
            val multipart: Multipart = MimeMultipart()
            multipart.addBodyPart(messageBodyPart)

            // Inclusion du fichier joint.
            val fichierBodyPart: BodyPart = MimeBodyPart()
            val source: DataSource = FileDataSource(urlFichier) // <- chemin du fichier
            fichierBodyPart.dataHandler = DataHandler(source)
            fichierBodyPart.fileName = nomFichier // <- nom du fichier
            multipart.addBodyPart(fichierBodyPart)
            message.setContent(multipart) // Construction du mail.
            if (recipients.indexOf(',') > 0) message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(recipients)
            ) else message.setRecipient(
                Message.RecipientType.TO, InternetAddress(recipients)
            )
            Transport.send(message)
            Log.e("SEND ", "SEND")
        } catch (e: Exception) {
            Log.e("ERROR ", "ERROR")
            e.printStackTrace()
        }
    }

    init {
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        props.setProperty("mail.host", mailhost)
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"
        props["mail.smtp.socketFactory.port"] = "465"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.fallback"] = "false"
        props.setProperty("mail.smtp.quitwait", "false")
        session = Session.getDefaultInstance(props, this)
    }
}