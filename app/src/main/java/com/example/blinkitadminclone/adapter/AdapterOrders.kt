package com.example.blinkitadminclone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blinkitadminclone.databinding.ItemViewOrdersBinding
import com.example.blinkitadminclone.models.Order

class AdapterOrders(
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<AdapterOrders.OrdersViewHolder>() {

    private var orderList: List<Order> = emptyList()

    fun setOrderList(list: List<Order>) {
        this.orderList = list
        notifyDataSetChanged()
    }

    class OrdersViewHolder(val binding: ItemViewOrdersBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val binding = ItemViewOrdersBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrdersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = orderList[position]
        holder.binding.apply {
            tvOrderDate.text = order.date
            tvOrderAmount.text = "₹${order.total_amount}"
            tvOrderStatus.text = order.status
            tvAddress.text = order.profiles?.address ?: "No address provided"
        }
        holder.itemView.setOnClickListener {
            onOrderClick(order)
        }
    }

    override fun getItemCount(): Int = orderList.size
}
