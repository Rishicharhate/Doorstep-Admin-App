package com.example.blinkitadminclone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blinkitadminclone.R
import com.example.blinkitadminclone.databinding.ItemViewCheckoutOrderBinding
import com.example.blinkitadminclone.models.OrderItem

class AdapterOrderItem : RecyclerView.Adapter<AdapterOrderItem.OrderItemViewHolder>() {

    private var orderItemList: List<OrderItem> = emptyList()

    fun setOrderItemList(list: List<OrderItem>) {
        this.orderItemList = list
        notifyDataSetChanged()
    }

    class OrderItemViewHolder(val binding: ItemViewCheckoutOrderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemViewCheckoutOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val orderItem = orderItemList[position]
        val product = orderItem.admin

        holder.binding.apply {
            tvProductTitle.text = product?.productTitle ?: "Unknown Product"
            tvProductQuantity.text = "${product?.productQuantity} ${product?.productUnit}"
            tvProductPrice.text = "₹${product?.productPrice}"
            // Using item_quntity column as requested
            tvProductCount.text = orderItem.item_quantity.toString()

            val imageUrl = product?.productImagesUris?.firstOrNull()
            if (!imageUrl.isNullOrEmpty()) {
                Glide.with(ivProductImage.context)
                    .load(imageUrl)
                    .placeholder(R.drawable.baseline_image_24)
                    .into(ivProductImage)
            } else {
                ivProductImage.setImageResource(R.drawable.baseline_image_24)
            }
        }
    }

    override fun getItemCount(): Int = orderItemList.size
}
