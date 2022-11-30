package com.projet.sluca.smallbrother

import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.*
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
        "ksa.test0"


    fun getKeyPair(): KeyPair? {
        val ks: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
        val aliases: Enumeration<String> = ks.aliases()

        /**
         * Check whether the keypair with the alias [KEYSTORE_ALIAS] exists.
         */
        val keyPair: KeyPair? = if (aliases.toList().firstOrNull { it == KEYSTORE_ALIAS } == null) {
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
            val entry = ks.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.PrivateKeyEntry
            KeyPair(entry?.certificate?.publicKey, entry?.privateKey)
        }
        return keyPair
    }

    /**
     * Returns the public key with alias [KEYSTORE_ALIAS].
     */
    fun getPublicKey(keyPair: KeyPair?): String? {
        Log.d("keypair1", keyPair.toString())
        Log.d("PubKey1", String(Base64.encode(keyPair?.public?.encoded, Base64.DEFAULT)))
        val publicKey = keyPair?.public ?: return null
        return String(Base64.encode(publicKey.encoded, Base64.DEFAULT))
    }

    /**
     * Returns the private key with alias [KEYSTORE_ALIAS].
     */
    fun getPrivateKey(keyPair: KeyPair?): PrivateKey? {
        Log.d("keypair2", keyPair.toString())
        Log.d("PubKey2", String(Base64.encode(keyPair?.public?.encoded, Base64.DEFAULT)))
        return keyPair?.private ?: return null
    }
}