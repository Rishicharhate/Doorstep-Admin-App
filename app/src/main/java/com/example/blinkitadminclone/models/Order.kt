package com.example.blinkitadminclone.models

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String? = null,
    val date: String? = null,
    val total_amount: Int? = null,
    val status: String? = null,
    val profile_id: String? = null,
    // This field can be used to hold the joined profile data if needed, 
    // but typically we'll do a join query or fetch profile separately.
    val profiles: Profile? = null 
)

@Serializable
data class Profile(
    val id: String? = null,
    val address: String? = null
)
