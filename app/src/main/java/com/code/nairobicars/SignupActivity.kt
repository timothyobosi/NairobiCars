package com.code.nairobicars

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var signupButton: Button
    private lateinit var profileImageView: ImageView
    private  var profileImageUri: Uri?  = null// To hold the image URI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()
        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        signupButton = findViewById(R.id.signupButton)
        profileImageView = findViewById(R.id.profileImageView)

        // Handle Sign-Up
        signupButton.setOnClickListener {
            createAccount()
        }
    }

    // Open image picker for profile photo
    fun openImagePicker(view: View) {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            profileImageUri = data?.data
            profileImageView.setImageURI(profileImageUri) // Show selected image
        }
    }

    private fun createAccount() {
        val fullName = fullNameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || profileImageUri == null) {
            Toast.makeText(this, "Please fill all fields and upload a profile photo", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Upload profile photo to Firebase Storage
                    uploadProfilePhoto(fullName)
                } else {
                    Toast.makeText(this, "Sign Up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadProfilePhoto(fullName: String) {
        val userId = auth.currentUser?.uid ?: return
        val storageReference = FirebaseStorage.getInstance().reference.child("profile_photos/$userId")

        profileImageUri?.let { uri ->
            storageReference.putFile(uri).addOnSuccessListener {
                // Photo uploaded successfully, now store user info in Firestore
                storeUserInfo(fullName, userId)
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to upload profile photo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeUserInfo(fullName: String, userId: String) {
        val userMap = hashMapOf(
            "fullName" to fullName,
            "email" to auth.currentUser?.email,
            "profileImage" to "profile_photos/$userId"
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).set(userMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                // Navigate to Home Screen or another activity
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to store user info", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val IMAGE_PICK_REQUEST_CODE = 1000
    }
}
