package com.projet.sluca.smallbrother

import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.*
import java.security.KeyStore.PrivateKeyEntry
import java.util.*
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
    fun getPrivateKey(): PrivateKey {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val entry = ks.getEntry(KEYSTORE_ALIAS, null) as PrivateKeyEntry
        return entry.privateKey
    }
}