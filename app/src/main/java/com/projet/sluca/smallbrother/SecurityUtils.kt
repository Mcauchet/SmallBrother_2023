package com.projet.sluca.smallbrother

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import io.ktor.client.*
import io.ktor.client.request.*
import java.security.*
import java.security.KeyStore.PrivateKeyEntry
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal
import kotlin.math.sign


/**
 * manages the public/private keys
 *
 * @author https://github.com/hanmajid
 *
 * Source: https://yggr.medium.com/how-to-generate-public-private-key-in-android-7f3e244c0fd8
 */
object SecurityUtils {

    private const val KEYSTORE_ALIAS_RSA =
        "ksa.test6"

    private const val KEYSTORE_ALIAS_AES =
        "ksa.aes"


    fun getKeyPair() {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val aliases: Enumeration<String> = ks.aliases()

        /**
         * Check whether the keypair with the alias [KEYSTORE_ALIAS_RSA] exists.
         */
        if (aliases.toList().firstOrNull { it == KEYSTORE_ALIAS_RSA } == null) {
            // If it doesn't exist, generate new keypair
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
                "RSA"
            )

            kpg.initialize(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS_RSA,
                    KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
                ).setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
                ).setKeySize(
                    2048
                ).setCertificateSubject(X500Principal("CN=test"))
                    .build()
            )

            kpg.generateKeyPair()
        } else {
            // If it exists, load the existing keypair
            val entry = ks.getEntry(KEYSTORE_ALIAS_RSA, null) as? PrivateKeyEntry
            KeyPair(entry?.certificate?.publicKey, entry?.privateKey)
        }
    }

    /**
     * deletes the AES Key entry
     */
    fun deleteAESKeyEntry() {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        ks.deleteEntry(KEYSTORE_ALIAS_AES)
    }

    /**
     * Returns the public key with alias [KEYSTORE_ALIAS_RSA].
     *
     * @return the public key as a String
     */
    fun getPublicKey(): String {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val entry = ks.getEntry(KEYSTORE_ALIAS_RSA, null) as PrivateKeyEntry
        val pubKey = entry.certificate.publicKey
        return String(Base64.encode(pubKey.encoded, Base64.DEFAULT))
    }

    /**
     * Returns the private key with alias [KEYSTORE_ALIAS_RSA].
     *
     * @return the reference to the private key
     */
    private fun getPrivateKey(): PrivateKey {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val entry = ks.getEntry(KEYSTORE_ALIAS_RSA, null) as PrivateKeyEntry
        return entry.privateKey
    }

    /**
     * fun to get the AES key
     *
     * @return the AES key as a SecretKey
     */
    fun getAESKey(): SecretKey {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val aliases: Enumeration<String> = ks.aliases()
        return if (aliases.toList().firstOrNull { it == KEYSTORE_ALIAS_AES } == null) {
            val generator: KeyGenerator = KeyGenerator.getInstance("AES")
            generator.init(192)
            val aesKey = generator.generateKey()
            aesKey
        } else {
            ks.getEntry(KEYSTORE_ALIAS_AES, null) as SecretKey
        }
    }

    /**
     * fun to encrypt the AES key with the public RSA key
     *
     * @param publicKey the public key of the aidant
     * @param aesKey the key to encrypt
     * @return the encrypted aesKey as a ByteArray
     */
    fun encryptAESKey(publicKey: PublicKey, aesKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.PUBLIC_KEY, publicKey)
        return cipher.doFinal(aesKey.encoded)
    }

    /**
     * fun to decrypt the key with the private key
     *
     * @param encKey the AES key used to encrypt the data by the aide
     * @return the decrypted AESKey as a ByteArray
     */
    private fun decryptAESKey(encKey: ByteArray): ByteArray {
        val aesCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        aesCipher.init(Cipher.PRIVATE_KEY, getPrivateKey())
        return aesCipher.doFinal(encKey)
    }

    /**
     * fun to encrypt the zip file
     *
     * @param data the file's ByteArray
     * @param aesKey the key to encrypt the data
     * @return the encrypted file as a ByteArray
     */
    fun encryptDataAes(data:ByteArray, aesKey: SecretKey): ByteArray {
        val aesCipher = Cipher.getInstance("AES")
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey)
        return aesCipher.doFinal(data)
    }

    /**
     * fun to decrypt the data (previously encrypted with AES key)
     *
     * @param data the encrypted data
     * @param encKey the encrypted AES key
     * @return the decrypted file data as a ByteArray
     */
    fun decryptDataAes(data:ByteArray, encKey: ByteArray):ByteArray {
        val decryptedKey = decryptAESKey(encKey)
        val originalKey = SecretKeySpec(decryptedKey, 0, decryptedKey.count(), "AES")
        val aesCipher = Cipher.getInstance("AES")
        aesCipher.init(Cipher.DECRYPT_MODE, originalKey)
        return aesCipher.doFinal(data)
    }

    //TODO test this chunk
    /**
     * signs the zip file with the private key
     * @param data the zip files ByteArray
     * @return the signed data
     */
    fun signFile(data: ByteArray): ByteArray? {
        val privateKey = getPrivateKey()
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(data)
        return signature.sign()
    }

    /**
     * verify the signature to authenticate the origin
     * @param data the signed ByteArray
     * @param pubKey the public key of the signer
     * @return true if file is verified, false otherwise
     */
    fun verifyFile(data: ByteArray, pubKey:PublicKey): Boolean {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(pubKey)
        signature.update(data)
        return signature.verify(data)
    }
}