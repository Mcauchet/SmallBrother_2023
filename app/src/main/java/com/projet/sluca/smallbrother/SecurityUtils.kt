package com.projet.sluca.smallbrother

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.*
import java.security.KeyStore.PrivateKeyEntry
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal


/***
 * manages the public/private keys
 *
 * @author https://github.com/hanmajid
 *
 * Source: https://yggr.medium.com/how-to-generate-public-private-key-in-android-7f3e244c0fd8
 */
object SecurityUtils {

    private const val KEYSTORE_ALIAS =
        "ksa.test6"

    private const val KEYSTORE_ALIAS_AES =
        "ksa.aes"


    fun getKeyPair() {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val aliases: Enumeration<String> = ks.aliases()

        /**
         * Check whether the keypair with the alias [KEYSTORE_ALIAS] exists.
         */
        if (aliases.toList().firstOrNull { it == KEYSTORE_ALIAS } == null) {
            // If it doesn't exist, generate new keypair
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance(
                "RSA"
            )

            kpg.initialize(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS, KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
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
            val entry = ks.getEntry(KEYSTORE_ALIAS, null) as? PrivateKeyEntry
            KeyPair(entry?.certificate?.publicKey, entry?.privateKey)
        }
    }

    /**
     * Returns the public key with alias [KEYSTORE_ALIAS].
     */
    fun getPublicKey(): String {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val entry = ks.getEntry(KEYSTORE_ALIAS, null) as PrivateKeyEntry
        val pubKey = entry.certificate.publicKey
        Log.d("getPublicKey()", String(Base64.encode(pubKey.encoded, Base64.NO_WRAP)))
        return String(Base64.encode(pubKey.encoded, Base64.DEFAULT))
    }

    /**
     * Returns the private key with alias [KEYSTORE_ALIAS].
     */
    private fun getPrivateKey(): PrivateKey {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val entry = ks.getEntry(KEYSTORE_ALIAS, null) as PrivateKeyEntry
        return entry.privateKey
    }

    /***
     * fun to get the AES key
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
            Log.d("AES KEY CREATION REF", aesKey.toString())
            Log.d("getAESKey()", String(Base64.encode(aesKey.encoded, Base64.NO_WRAP)))
            aesKey
        } else {
            ks.getEntry(KEYSTORE_ALIAS_AES, null) as SecretKey
        }
    }

    /***
     * fun to encrypt the zip file
     */
    fun encryptDataAes(data:ByteArray, aesKey: SecretKey): ByteArray {
        val aesCipher = Cipher.getInstance("AES")
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey)
        Log.d("AES KEY USE REF", aesKey.toString())
        Log.d("encryptDataAes()", String(Base64.encode(aesKey.encoded, Base64.NO_WRAP)))
        return aesCipher.doFinal(data)
    }

    /***
     * fun to encrypt the AES key with the public RSA key
     *
     * @param publicKey the public key of the aidant
     */
    fun encryptAESKey(publicKey: PublicKey, aesKey: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.PUBLIC_KEY, publicKey)
        return cipher.doFinal(aesKey.encoded)
    }

    /***
     * fun to decrypt the key with the private key
     *
     * @param encKey the AES key used to encrypt the data by the aide
     */
    private fun decryptAESKey(encKey: ByteArray): ByteArray {
        val aesCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        aesCipher.init(Cipher.PRIVATE_KEY, getPrivateKey())
        return aesCipher.doFinal(encKey)
    }

    fun decryptDataAes(data:ByteArray, encKey: ByteArray):ByteArray {
        val decryptedKey = decryptAESKey(encKey)
        val originalKey = SecretKeySpec(decryptedKey, 0, decryptedKey.count(), "AES")
        Log.d("Decrypted key count", decryptedKey.count().toString())
        Log.d("Decrpted key size", decryptedKey.size.toString())
        val aesCipher = Cipher.getInstance("AES")
        aesCipher.init(Cipher.DECRYPT_MODE, originalKey)
        return aesCipher.doFinal(data)
    }
}