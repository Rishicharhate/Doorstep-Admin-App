package com.example.blinkitadminclone.models

import kotlinx.serialization.Serializable

@Serializable
data class OrderItem(
    val id: String? = null,
    val order_id: String? = null,
    val product_id: String? = null,
    val item_quantity: Int? = null,
    val admin: Product? = null
)
