package com.sachin.app.whatsclean.ui.authentication

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.mikhaellopez.biometric.BiometricHelper
import com.mikhaellopez.biometric.BiometricPromptInfo
import com.sachin.app.whatsclean.R
import kotlinx.coroutines.delay

class AuthenticationFragment : Fragment(R.layout.fragment_authentication) {

    override fun onStart() {
        super.onStart()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            delay(300)
            BiometricHelper(this@AuthenticationFragment).showBiometricPrompt(
                BiometricPromptInfo(
                    title = getString(R.string.fingerprint_prompt_title),
                    negativeButtonText = getString(R.string.cancel),
                    description = getString(R.string.fingerprint_prompt_desc)
                ),
                onError = { _, errString: CharSequence ->
                    //          fingerprintView.setImageResource(R.drawable.ic_round_error_outline_24)
                    Log.i(TAG, "onViewCreated: Error = $errString")
                }, onFailed = {
                    //        fingerprintView.setImageResource(R.drawable.ic_round_error_outline_24)
                    Log.i(TAG, "onViewCreated: Failed")
                }, onSuccess = {
                    //      fingerprintView.setImageResource(R.drawable.ic_round_check_circle_24)
                    Log.i(TAG, "onViewCreated: Success")
                    findNavController().navigate(
                        R.id.action_authenticationFragment_to_dashboardFragment
                    )
                }
            )
        }
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.show()
    }

}

private const val TAG = "AuthenticationFragment"