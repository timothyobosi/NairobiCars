package com.code.nairobicars

data class Car(
    val make: String = "",
    val model: String = "",
    val year: Long = 0,
    val price: Long = 0L,
    val images: List<String> = emptyList() // URLs of uploaded images
)
