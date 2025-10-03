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
import com.example.fingerprintauth.databinding.ActivityFaceRegisterBinding
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FaceRegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityFaceRegisterBinding
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
        binding = ActivityFaceRegisterBinding.inflate(layoutInflater)
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
    
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                showMessage("Camera permission is required for face registration")
                finish()
            }
        }
    }
    
    private fun setupUI() {
        binding.apply {
            instructionText.text = """
                Face Registration Instructions:
                1. Make sure you are in good lighting
                2. Look directly at the front camera
                3. Tap 'Register Face'
                4. Follow the prompts to scan your face
                5. Your face will be securely stored
            """.trimIndent()
            
            registerFaceButton.setOnClickListener {
                if (!sharedPrefsManager.isUserRegistered()) {
                    showMessage("Please register with fingerprint first")
                    return@setOnClickListener
                }
                
                if (sharedPrefsManager.isFaceRegistered()) {
                    showMessage("Face already registered for this user")
                    return@setOnClickListener
                }
                
                // Capture face image
                capturePhoto()
            }
            
            backButton.setOnClickListener {
                finish()
            }
        }
    }
    
    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return
        
        binding.registerFaceButton.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(
            createTempFile("face_capture", ".jpg")
        ).build()
        
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) {
                    showMessage("Photo capture failed: ${exception.message}")
                    binding.registerFaceButton.isEnabled = true
                    binding.progressBar.visibility = android.view.View.GONE
                }
                
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    if (savedUri != null) {
                        processCapturedImage(savedUri.path!!)
                    }
                }
            }
        )
    }
    
    private fun processCapturedImage(imagePath: String) {
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
            val username = sharedPrefsManager.getUsername()
            
            if (username.isNotEmpty()) {
                registerFace(username, faceData)
            } else {
                showMessage("Username not found. Please register with fingerprint first.")
                resetUI()
            }
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
        binding.registerFaceButton.isEnabled = true
        binding.progressBar.visibility = android.view.View.GONE
    }
    
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        // Use NO_WRAP to avoid line breaks in Base64 string
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
    
    private fun registerFace(username: String, faceData: String) {
        binding.registerFaceButton.isEnabled = false
        binding.progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val request = RegisterFaceRequest(username, faceData)
                val response = apiService.registerFace(request)
                
                if (response.success) {
                    sharedPrefsManager.saveFaceData(faceData)
                    showMessage("Face registration successful!")
                    finish()
                } else {
                    showMessage(response.message ?: "Face registration failed")
                }
                
            } catch (e: Exception) {
                showMessage("Network error: ${e.message}")
            } finally {
                binding.registerFaceButton.isEnabled = true
                binding.progressBar.visibility = android.view.View.GONE
            }
        }
    }
    
    private fun generateFaceData(username: String): String {
        // In a real application, this would be actual face template data
        // For demo purposes, we'll create a unique identifier
        val timestamp = System.currentTimeMillis()
        val deviceId = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
        return "face-$username-$deviceId-$timestamp".hashCode().toString()
    }
    
    private fun showError(message: String) {
        binding.instructionText.text = message
        binding.registerFaceButton.isEnabled = false
    }
    
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
