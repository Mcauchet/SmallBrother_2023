package com.projet.sluca.smallbrother

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.*
import java.security.KeyStore.PrivateKeyEntry
import java.security.KeyStore.getInstance
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.security.auth.x500.X500Principal


/**
 * manages the public/private keys
 *
 * @author https://github.com/hanmajid
 *
 * Source: https://yggr.medium.com/how-to-generate-public-private-key-in-android-7f3e244c0fd8
 */
object SecurityUtils {

    private const val KEYSTORE_ALIAS_RSA =
        "ksa.test101"

    private const val KEYSTORE_ALIAS_AES =
        "ksa.aes101"

    private const val KEYSTORE_ALIAS_SIGN_RSA =
        "ksa.sign.rsa101"

    /**
     * Load the AndroidKeyStore instance
     */
    private fun loadKeyStore() : KeyStore = getInstance("AndroidKeyStore").apply { load(null) }

    /**
     * Generate the Key pair for signing files
     */
    fun getSignKeyPair() {
        val ks: KeyStore = loadKeyStore()
        val aliases: Enumeration<String> = ks.aliases()
        if(aliases.toList().firstOrNull {it == KEYSTORE_ALIAS_SIGN_RSA} == null) {
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
            initSignKpg(kpg)
            kpg.generateKeyPair()
        } else {
            val entry = ks.getEntry(KEYSTORE_ALIAS_SIGN_RSA, null) as? PrivateKeyEntry
            KeyPair(entry?.certificate?.publicKey, entry?.privateKey)
        }
    }

    /**
     * Configure the sign and verify key pair generator
     */
    private fun initSignKpg(kpg: KeyPairGenerator) {
        kpg.initialize(
            KeyGenParameterSpec
                .Builder(
                    KEYSTORE_ALIAS_SIGN_RSA,
                    KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
                )
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setKeySize(2048)
                .setCertificateSubject(X500Principal("CN=test"))
                .setUserAuthenticationRequired(false)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)
                .build()
        )
    }

    /**
     * Returns the private key with alias [KEYSTORE_ALIAS_SIGN_RSA].
     *
     * @return the reference to the private key
     */
    private fun getSignPrivateKey(): PrivateKey {
        val ks: KeyStore = loadKeyStore()
        val entry = ks.getEntry(KEYSTORE_ALIAS_SIGN_RSA, null) as PrivateKeyEntry
        return entry.privateKey
    }

    /**
     * Returns the public key with alias [KEYSTORE_ALIAS_SIGN_RSA].
     *
     * @return the public key as a String
     */
    fun getSignPublicKey(): String {
        val ks: KeyStore = loadKeyStore()
        val entry = ks.getEntry(KEYSTORE_ALIAS_SIGN_RSA, null) as PrivateKeyEntry
        val pubKey = entry.certificate.publicKey
        return String(Base64.encode(pubKey.encoded, Base64.DEFAULT))
    }

    /**
     * Generate Android KeyStore public and private RSA keys
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 08-01-2023)
     */
    fun getEncryptionKeyPair() {
        val ks: KeyStore = loadKeyStore()
        val aliases: Enumeration<String> = ks.aliases()
        if (aliases.toList().firstOrNull { it == KEYSTORE_ALIAS_RSA } == null) {
            val kpg: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
            initEncKpg(kpg)
            kpg.generateKeyPair()
        } else {
            val entry = ks.getEntry(KEYSTORE_ALIAS_RSA, null) as? PrivateKeyEntry
            KeyPair(entry?.certificate?.publicKey, entry?.privateKey)
        }
    }

    /**
     * Configure the encryption and decryption key pair generator
     */
    private fun initEncKpg(kpg: KeyPairGenerator) {
        kpg.initialize(
            KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS_RSA,
                KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_ENCRYPT
            ).setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
            ).setKeySize(
                2048
            ).setCertificateSubject(X500Principal("CN=test")
            ).build()
        )
    }

    /**
     * deletes the AES Key entry
     */
    fun deleteAESKeyEntry() {
        val ks: KeyStore = loadKeyStore()
        ks.deleteEntry(KEYSTORE_ALIAS_AES)
    }

    /**
     * Returns the public key with alias [KEYSTORE_ALIAS_RSA].
     *
     * @return the public key as a String
     */
    fun getEncPublicKey(): String {
        val ks: KeyStore = loadKeyStore()
        val entry = ks.getEntry(KEYSTORE_ALIAS_RSA, null) as PrivateKeyEntry
        val pubKey = entry.certificate.publicKey
        return String(Base64.encode(pubKey.encoded, Base64.DEFAULT))
    }

    /**
     * Returns the private key with alias [KEYSTORE_ALIAS_RSA].
     *
     * @return the reference to the private key
     */
    private fun getEncPrivateKey(): PrivateKey {
        val ks: KeyStore = loadKeyStore()
        val entry = ks.getEntry(KEYSTORE_ALIAS_RSA, null) as PrivateKeyEntry
        return entry.privateKey
    }

    /**
     * fun to get the AES key
     *
     * @return the AES key as a SecretKey
     */
    fun getAESKey(): SecretKey {
        val ks: KeyStore = loadKeyStore()
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
        aesCipher.init(Cipher.PRIVATE_KEY, getEncPrivateKey())
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

    /**
     * signs the zip file with the private key
     * @param data the zip files ByteArray
     * @return the signed data
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 05-01-2023)
     */
    fun signFile(data: ByteArray): ByteArray {
        val privateKey = getSignPrivateKey()
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
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 05-01-2023)
     */
    fun verifyFile(data: ByteArray, pubKey:PublicKey, signature: ByteArray): Boolean {
        val verifySignature = Signature.getInstance("SHA256withRSA")
        verifySignature.initVerify(pubKey)
        verifySignature.update(data)
        return verifySignature.verify(signature)
    }

    /***
     * Transform a String into a Key object used as a public key
     *
     * @param publicKey the String to transform into a Key
     * @return the Key object
     * @author Maxime Caucheteur
     * @version 1.2 (Updated on 04-12-2022)
     */
    fun loadPublicKey(publicKey: String): Key {
        val data: ByteArray = Base64.decode(publicKey.toByteArray(), Base64.DEFAULT)
        val spec = X509EncodedKeySpec(data)
        val fact = KeyFactory.getInstance("RSA")
        return fact.generatePublic(spec)
    }
}