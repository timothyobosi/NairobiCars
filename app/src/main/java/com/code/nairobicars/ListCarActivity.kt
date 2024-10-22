package com.code.nairobicars

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.lang.Exception

class ListCarActivity : AppCompatActivity() {

    private lateinit var carMakeEditText: EditText
    private lateinit var carModelEditText: EditText
    private lateinit var uploadImagesButton: Button
    private lateinit var listCarButton: Button
    private lateinit var imageContainer: LinearLayout
    private val selectedImages = mutableListOf<Uri>() // To hold the selected image URIs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_car)

        carMakeEditText = findViewById(R.id.carMakeEditText)
        carModelEditText = findViewById(R.id.carModelEditText)
        uploadImagesButton = findViewById(R.id.uploadImagesButton)
        listCarButton = findViewById(R.id.listCarButton)
        imageContainer = findViewById(R.id.imageContainer)

        uploadImagesButton.setOnClickListener {
            openImagePicker()
        }

        listCarButton.setOnClickListener {
            // Logic to list the car (to be implemented)
            val make = carMakeEditText.text.toString()
            val model = carModelEditText.text.toString()
            // Save car details and images to Firestore or other logic

            if(make.isEmpty() || selectedImages.isEmpty()){
                Toast.makeText(this,"Please fill all fields and upload at least one image",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            uploadCarDetails(make, model)
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data?.clipData != null) {
                // Multiple images selected
                val count = data.clipData?.itemCount ?: 0
                for (i in 0 until count) {
                    val imageUri = data.clipData?.getItemAt(i)?.uri
                    imageUri?.let {
                        selectedImages.add(it)
                        displayImage(it)
                    }
                }
            } else if (data?.data != null) {
                // Single image selected
                val imageUri = data.data
                imageUri?.let {
                    selectedImages.add(it)
                    displayImage(it)
                }
            }
        }
    }

    private fun displayImage(imageUri: Uri) {
        val imageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                marginEnd = 8
            }
            setImageURI(imageUri)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }
        imageContainer.addView(imageView)
    }

    companion object {
        const val IMAGE_PICK_REQUEST_CODE = 1000
    }

    private fun uploadCarDetails(make: String, model: String){
        val db = FirebaseFirestore.getInstance()
        val carId = db.collection("cars").document().id
        val carImages = mutableListOf<String>()

        //Upload images
        val storageReference = FirebaseStorage.getInstance().reference.child("cars_images/$carId")
        val uploadTasks = mutableListOf<Task<Uri>>()// to hold all upload tasks
        for (imageUri in selectedImages){
            val uploadTask = storageReference.child("${System.currentTimeMillis()}.jpg").putFile(imageUri)
                .continueWithTask{task->
                    if(!task.isSuccessful){
                        throw task.exception ?: Exception("Image upload failed")
                    }
                    storageReference.downloadUrl
                }
                .addOnSuccessListener { uri->
                    carImages.add(uri.toString())//To store the image Url
                }
                .addOnSuccessListener {
                    Toast.makeText(this,"Failed to upload image", Toast.LENGTH_LONG).show()
                }
            uploadTasks.add(uploadTask)
        }

        //wait for all uploads to complete
        Tasks.whenAllComplete(uploadTasks)
            .addOnSuccessListener {
                //After all images are uploaded, save car details to Firestore
                val carData = hashMapOf(
                    "make" to make,
                    "model" to model,
                    "images" to carImages
                )

                db.collection("cars").document(carId).set(carData)
                    .addOnSuccessListener {
                        Toast.makeText(this,"Car listed succesfully!",Toast.LENGTH_LONG).show()
                        //Optionally navigate to another activity or clear fields
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"Failed to list car", Toast.LENGTH_LONG).show()
                    }
            }
    }
}
