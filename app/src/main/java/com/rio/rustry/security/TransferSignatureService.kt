// generated/phase3/app/src/main/java/com/rio/rustry/security/TransferSignatureService.kt

package com.rio.rustry.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferSignatureService @Inject constructor() {
    
    private val keyAlias = "transfer_signing_key"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    
    init {
        generateKeyPairIfNeeded()
    }
    
    fun signData(data: Map<String, String>): String {
        val privateKey = getPrivateKey()
        val signature = Signature.getInstance("SHA256withECDSA")
        signature.initSign(privateKey)
        
        val dataString = data.entries.sortedBy { it.key }
            .joinToString("&") { "${it.key}=${it.value}" }
        
        signature.update(dataString.toByteArray())
        val signatureBytes = signature.sign()
        
        return android.util.Base64.encodeToString(signatureBytes, android.util.Base64.DEFAULT)
    }
    
    fun verifySignature(data: Map<String, String>, signatureString: String): Boolean {
        return try {
            val publicKey = keyStore.getCertificate(keyAlias).publicKey
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initVerify(publicKey)
            
            val dataString = data.entries.sortedBy { it.key }
                .joinToString("&") { "${it.key}=${it.value}" }
            
            signature.update(dataString.toByteArray())
            val signatureBytes = android.util.Base64.decode(signatureString, android.util.Base64.DEFAULT)
            
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            false
        }
    }
    
    private fun generateKeyPairIfNeeded() {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC,
                "AndroidKeyStore"
            )
            
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
            )
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .setUserAuthenticationRequired(false)
                .build()
            
            keyPairGenerator.initialize(keyGenParameterSpec)
            keyPairGenerator.generateKeyPair()
        }
    }
    
    private fun getPrivateKey(): PrivateKey {
        return keyStore.getKey(keyAlias, null) as PrivateKey
    }
}