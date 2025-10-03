package com.example.fingerprintauth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.fingerprintauth.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch
import java.util.concurrent.Executor

class LoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityLoginBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var apiService: ApiService
    private lateinit var sharedPrefsManager: SharedPrefsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        apiService = ApiClient.create()
        sharedPrefsManager = SharedPrefsManager(this)
        
        setupBiometric()
        setupUI()
    }
    
    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    showMessage("Authentication error: $errString")
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val username = binding.usernameInput.text.toString().trim()
                    if (username.isNotEmpty()) {
                        verifyUser(username)
                    } else {
                        showMessage("Please enter your username")
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    showMessage("Authentication failed")
                }
            })
        
        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Verify Identity")
            .setSubtitle("Place your finger on the sensor to login")
            .setDescription("Use your registered fingerprint to authenticate")
            .setNegativeButtonText("Cancel")
            .build()
    }
    
    private fun setupUI() {
        binding.apply {
            instructionText.text = """
                Login Instructions:
                1. Enter your registered username
                2. Tap 'Verify Fingerprint'
                3. Place your registered finger on the sensor
                4. Wait for verification
            """.trimIndent()
            
            // Pre-fill username if available
            val savedUsername = sharedPrefsManager.getUsername()
            if (savedUsername.isNotEmpty()) {
                usernameInput.setText(savedUsername)
            }
            
            loginButton.setOnClickListener {
                val username = usernameInput.text.toString().trim()
                if (username.isEmpty()) {
                    showMessage("Please enter your username")
                    return@setOnClickListener
                }
                
                // Start biometric authentication
                biometricPrompt.authenticate(promptInfo)
            }
            
            registerButton.setOnClickListener {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
            
            backButton.setOnClickListener {
                finish()
            }
        }
    }
    
    private fun verifyUser(username: String) {
        binding.loginButton.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            try {
                // Get stored fingerprint data
                val fingerprintData = sharedPrefsManager.getFingerprintData()
                if (fingerprintData.isEmpty()) {
                    showMessage("No fingerprint data found. Please register first.")
                    return@launch
                }
                
                val request = VerifyRequest(username, fingerprintData)
                val response = apiService.verifyUser(request)
                
                if (response.success) {
                    showWelcomeMessage(response.message ?: "Welcome!")
                } else {
                    showMessage(response.message ?: "Verification failed")
                }
                
            } catch (e: Exception) {
                showMessage("Network error: ${e.message}")
            } finally {
                binding.loginButton.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun showWelcomeMessage(message: String) {
        // Show welcome dialog or navigate to main app
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Authentication Successful")
            .setMessage(message)
            .setPositiveButton("Continue") { _, _ ->
                // Navigate to main app or dashboard
                showMessage("Login successful! Welcome to the app.")
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}