package com.code.nairobicars

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ViewCarsActivity : AppCompatActivity() {

    private lateinit var recyclerViewCars: RecyclerView
    private lateinit var searchView: SearchView
    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerPrice: Spinner

    private val carList = mutableListOf<Car>() // Store the cars retrieved from Firestore
    private lateinit var carAdapter: CarListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_cars)

        // Check for storage permission and request it if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request the permission
            requestStoragePermission()
        }else{
            // Permission has already been granted, proceed with loading cars
            loadCarData()
        }

        // Initialize views
        recyclerViewCars = findViewById(R.id.recyclerViewCars)
        searchView = findViewById(R.id.searchView)
        spinnerYear = findViewById(R.id.spinnerYear)
        spinnerPrice = findViewById(R.id.spinnerPrice)

        // Set up RecyclerView and adapter
        recyclerViewCars.layoutManager = LinearLayoutManager(this)
        carAdapter = CarListAdapter(carList, this)
        recyclerViewCars.adapter = carAdapter

        // Load car data from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("cars").get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val car = document.toObject(Car::class.java)
                    carList.add(car)
                }
                carAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load cars: $e", Toast.LENGTH_SHORT).show()
            }

        // Implement search functionality
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = carList.filter { car ->
                    car.make.contains(newText ?: "", ignoreCase = true) ||
                            car.model.contains(newText ?: "", ignoreCase = true)
                }
                carAdapter.updateCarList(filteredList)  // Update RecyclerView with filtered list
                return true
            }
        })

        // Set up filtering functionality for Spinners
        spinnerYear.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedYear = parent.getItemAtPosition(position).toString()
                filterCars(selectedYear, spinnerPrice.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        spinnerPrice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedPrice = parent.getItemAtPosition(position).toString()
                filterCars(spinnerYear.selectedItem.toString(), selectedPrice)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted to load car data
                loadCarData()
            } else {
                // Permission denied, show a message to the user
                Toast.makeText(this, "Permission to access storage denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Method to filter cars by year and price
    private fun filterCars(year: String, price: String) {
        val filteredList = carList.filter { car ->
            val yearMatches = (year == "All Years" || car.year.toString() == year)

            // Convert car price to a number for proper comparison
            val priceMatches = when (price) {
                "All Prices" -> true
                "Below 1M" -> car.price < 1_000_000 // Assuming price is stored as Int or Long
                "1M - 3M" -> car.price in 1_000_000..3_000_000
                "Above 3M" -> car.price > 3_000_000
                else -> true
            }

            yearMatches && priceMatches
        }

        carAdapter.updateCarList(filteredList)  // Update RecyclerView with filtered list
    }

    //Method to request Permission
    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
            ){
            //Provide an explanation to the user asynchronously
            Toast.makeText(this,"Storage access is required to view car images", Toast.LENGTH_LONG)
                .show()

            //Request permission again
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1
            )
        }
    }

    private fun loadCarData(){
        //load car data from firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("cars").get()
            .addOnSuccessListener {result->
                for(document in result){
                    for (document in result){
                        val car = document.toObject(Car::class.java)
                        carList.add(car)
                    }
                    carAdapter.notifyDataSetChanged()
                }
            }
            .addOnSuccessListener { e->
                Toast.makeText(this,"Failed to load cars:$e",Toast.LENGTH_LONG).show()

            }
    }
}
