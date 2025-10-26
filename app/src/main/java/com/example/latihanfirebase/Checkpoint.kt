package com.example.latihanfirebase

data class Checkpoint(
    var id: String? = null,
    var userId: String? = null,
    var name: String? = null,
    var description: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var timestamp: Long? = null
)