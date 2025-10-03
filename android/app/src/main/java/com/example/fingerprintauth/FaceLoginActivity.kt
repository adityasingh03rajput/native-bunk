package com.example.fingerprintauth

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.fingerprintauth.databinding.ActivityFaceLoginBinding
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceLoginActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFaceLoginBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var apiService: ApiService
    private lateinit var sharedPrefsManager: SharedPrefsManager
    private var imageCapture: ImageCapture? = null
    
    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        apiService = ApiClient.create()
        sharedPrefsManager = SharedPrefsManager(this)
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }
        
        setupUI()
    }
    
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }
            
            imageCapture = ImageCapture.Builder().build()
            
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                showMessage("Failed to start camera: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    
    private fun setupUI() {
        binding.apply {
            instructionText.text = """
                Face Login Instructions:
                1. Enter your registered username
                2. Make sure you are in good lighting
                3. Tap 'Verify Face'
                4. Look directly at the front camera
                5. Wait for verification
            """.trimIndent()
            
            // Pre-fill username if available
            val savedUsername = sharedPrefsManager.getUsername()
            if (savedUsername.isNotEmpty()) {
                usernameInput.setText(savedUsername)
            }
            
            verifyFaceButton.setOnClickListener {
                val username = usernameInput.text.toString().trim()
                if (username.isEmpty()) {
                    showMessage("Please enter your username")
                    return@setOnClickListener
                }
                
                // Capture face for verification
                capturePhoto()
            }
            
            backButton.setOnClickListener {
                finish()
            }
        }
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                showMessage("Camera permission is required for face login")
                finish()
            }
        }
    }
    
    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return
        val username = binding.usernameInput.text.toString().trim()
        
        if (username.isEmpty()) {
            showMessage("Please enter your username")
            return
        }
        
        binding.verifyFaceButton.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            createTempFile("face_login", ".jpg")
        ).build()
        
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    showMessage("Photo capture failed: ${exception.message}")
                    binding.verifyFaceButton.isEnabled = true
                    binding.progressBar.visibility = android.view.View.GONE
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    if (savedUri != null) {
                        processCapturedImage(savedUri.path!!, username)
                    }
                }
            }
        )
    }
    
    private fun processCapturedImage(imagePath: String, username: String) {
        try {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            
            if (bitmap == null) {
                showMessage("Failed to load captured image")
                resetUI()
                return
            }
            
            // Resize image for better processing
            val resizedBitmap = resizeBitmap(bitmap, 512, 512)
            val faceData = convertBitmapToBase64(resizedBitmap)
            verifyFace(username, faceData)
        } catch (e: Exception) {
            showMessage("Error processing image: ${e.message}")
            resetUI()
        }
    }
    
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = minOf(scaleWidth, scaleHeight)
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    private fun resetUI() {
        binding.verifyFaceButton.isEnabled = true
        binding.progressBar.visibility = android.view.View.GONE
    }
    
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        // Use NO_WRAP to avoid line breaks in Base64 string
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    private fun verifyFace(username: String, faceData: String) {
        binding.verifyFaceButton.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val request = VerifyFaceRequest(username, faceData)
                val response = apiService.verifyFace(request)
                
                if (response.success) {
                    showWelcomeMessage(response.message ?: "Welcome!")
                } else {
                    showMessage(response.message ?: "Face verification failed")
                }
                
            } catch (e: Exception) {
                showMessage("Network error: ${e.message}")
            } finally {
                binding.verifyFaceButton.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun showWelcomeMessage(message: String) {
        // Show welcome dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Face Authentication Successful")
            .setMessage(message)
            .setPositiveButton("Continue") { _, _ ->
                showMessage("Face login successful! Welcome to the app.")
                finish()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showError(message: String) {
        binding.instructionText.text = message
        binding.verifyFaceButton.isEnabled = false
    }
    
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
