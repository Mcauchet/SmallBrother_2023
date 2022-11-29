package com.projet.sluca.smallbrother

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.*
import java.util.*

/***
 * manages the public/private keys
 *
 * @author https://github.com/hanmajid
 *
 * Source: https://yggr.medium.com/how-to-generate-public-private-key-in-android-7f3e244c0fd8
 */
object SecurityUtils {

    private const val KEYSTORE_ALIAS =
        "aaaaa"


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

            kpg.initialize(2048)

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
    fun getPublicKey(): String? {
        val keyPair = getKeyPair()
        val publicKey = keyPair?.public ?: return null
        return String(Base64.encode(publicKey.encoded, Base64.DEFAULT))
    }

    /**
     * Returns the private key with alias [KEYSTORE_ALIAS].
     */
    fun getPrivateKey(): PrivateKey? {
        val keyPair = getKeyPair()
        return keyPair?.private ?: return null
    }
}