package br.com.tln.personalcard.usuario.extensions

import android.util.Base64
import android.util.Patterns
import br.com.tln.personalcard.usuario.BuildConfig
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

fun String.sha1(): String {
    return HashUtils.sha1(this)
}

fun String.sha256(): String {
    return HashUtils.sha256(this)
}

fun String.sha384(): String {
    return HashUtils.sha384(this)
}

fun String.sha512(): String {
    return HashUtils.sha512(this)
}

fun String.isEmail(): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(this).matches()
}

fun String.isAlphaNumeric(requireNumbersAndLetters: Boolean = false): Boolean {
    if (!(matches(Regex(".*[A-Za-z].*")) && matches(Regex(".*[0-9].*")))) {
        return false
    }

    if (requireNumbersAndLetters && !matches(Regex("[A-Za-z0-9]*"))) {
        return false
    }

    return true
}

fun String.initials(): String {
    val nameSplit = trim().split(" ")

    return if (nameSplit.size > 1) {
        "${nameSplit.first().first()}${nameSplit.last().first()}"
    } else {
        "${nameSplit.first().first()}"
    }
}

object HashUtils {

    fun sha1(input: String) = hashString("SHA-1", input)

    fun sha512(input: String) = hashString("SHA-512", input)

    fun sha384(input: String) = hashString("SHA-384", input)

    fun sha256(input: String) = hashString("SHA-256", input)

    /**
     * Supported algorithms on Android:
     *
     * Algorithm	Supported API Levels
     * MD5          1+
     * SHA-1	    1+
     * SHA-224	    1-8,22+
     * SHA-256	    1+
     * SHA-384	    1+
     * SHA-512	    1+
     */
    private fun hashString(type: String, input: String): String {
        val HEX_CHARS = "0123456789ABCDEF"
        val bytes = MessageDigest
            .getInstance(type)
            .digest(input.toByteArray())
        val result = StringBuilder(bytes.size * 2)

        bytes.forEach {
            val i = it.toInt()
            result.append(HEX_CHARS[i shr 4 and 0x0f])
            result.append(HEX_CHARS[i and 0x0f])
        }

        return result.toString()
    }
}

object CryptographyUtils {

    fun String.encrypt(
        key: String = BuildConfig.DEFAULT_CRYPTOGRAPHY_KEY,
        initializationVector: String = BuildConfig.DEFAULT_CRYPTOGRAPHY_INITIALIZATION_VECTOR,
        transformation: String = BuildConfig.DEFAULT_CRYPTOGRAPHY_TRANSFORMATION,
        algorithm: String = BuildConfig.DEFAULT_CRYPTOGRAPHY_ALGORITHM
    ): String {

        val valueByteArray = this.toByteArray()
        val keyByteArray = key.toByteArray()
        val initializationVectoryByteArray = initializationVector.toByteArray()

        val cipher = Cipher.getInstance(transformation)
        val secretKeyKey = SecretKeySpec(keyByteArray, algorithm)
        val ivSpec = IvParameterSpec(initializationVectoryByteArray)

        cipher.init(Cipher.ENCRYPT_MODE, secretKeyKey, ivSpec)

        val cipherText = cipher.doFinal(valueByteArray)

        return Base64.encodeToString(cipherText, Base64.NO_WRAP)
    }

    fun String.decrypt(
        key: String = BuildConfig.DEFAULT_CRYPTOGRAPHY_KEY,
        initializationVector: String = BuildConfig.DEFAULT_CRYPTOGRAPHY_INITIALIZATION_VECTOR,
        transformation: String = BuildConfig.DEFAULT_CRYPTOGRAPHY_TRANSFORMATION,
        algorithm: String = BuildConfig.DEFAULT_CRYPTOGRAPHY_ALGORITHM
    ): String {

        val valueByteArray = this.toByteArray()
        val keyByteArray = key.toByteArray()
        val initializationVectoryByteArray = initializationVector.toByteArray()

        val cipher = Cipher.getInstance(transformation)
        val secretKeyKey = SecretKeySpec(keyByteArray, algorithm)
        val ivSpec = IvParameterSpec(initializationVectoryByteArray)

        cipher.init(Cipher.DECRYPT_MODE, secretKeyKey, ivSpec)

        val decryptedText = cipher.doFinal(Base64.decode(valueByteArray, Base64.DEFAULT))

        return String(decryptedText)
    }

}