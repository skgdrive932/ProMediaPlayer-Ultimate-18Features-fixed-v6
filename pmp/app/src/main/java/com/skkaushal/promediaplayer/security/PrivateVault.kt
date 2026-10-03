package com.skkaushal.promediaplayer.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class PrivateVault(private val activity: FragmentActivity) {
    fun authenticate(onSuccess:()->Unit) {
        val manager=BiometricManager.from(activity)
        if(manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            != BiometricManager.BIOMETRIC_SUCCESS) return
        val executor=ContextCompat.getMainExecutor(activity)
        val prompt=BiometricPrompt(activity,executor,object:BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationSucceeded(r:BiometricPrompt.AuthenticationResult){onSuccess()}
        })
        prompt.authenticate(BiometricPrompt.PromptInfo.Builder()
            .setTitle("Private Media Vault").setSubtitle("Authenticate to open protected media")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL).build())
    }
}
