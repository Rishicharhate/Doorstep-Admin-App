package com.example.blinkitadminclone.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitadminclone.SupabaseClient
import com.example.blinkitadminclone.adapter.AdapterOrderItem
import com.example.blinkitadminclone.databinding.FragmentOrderDetailBinding
import com.example.blinkitadminclone.models.OrderItem
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {

    private lateinit var binding: FragmentOrderDetailBinding
    private lateinit var adapterOrderItem: AdapterOrderItem
    private var orderId: String? = null
    private var orderStatus: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            orderId = it.getString("orderId")
            orderStatus = it.getString("orderStatus")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderDetailBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupStatusUI()
        fetchOrderItems()

        binding.tbOrderDetailFragment.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnChangeStatus.setOnClickListener {
            showStatusDialog()
        }
    }

    private fun setupRecyclerView() {
        adapterOrderItem = AdapterOrderItem()
        binding.rvProductsItems.adapter = adapterOrderItem
    }

    private fun setupStatusUI() {
        val status = orderStatus?.lowercase() ?: ""
        
        binding.iv1.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(if (status == "placed" || status == "ordered" || status == "packed" || status == "shipped" || status == "delivered") com.example.blinkitadminclone.R.color.green else com.example.blinkitadminclone.R.color.grey)
        )

        //packed
        binding.iv2.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(if (status == "packed" || status == "shipped" || status == "delivered") com.example.blinkitadminclone.R.color.green else com.example.blinkitadminclone.R.color.grey)
        )
        binding.view1.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(if (status == "packed" || status == "shipped" || status == "delivered") com.example.blinkitadminclone.R.color.green else com.example.blinkitadminclone.R.color.grey)
        )

        //shipped
        binding.iv3.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(if (status == "shipped" || status == "delivered") com.example.blinkitadminclone.R.color.green else com.example.blinkitadminclone.R.color.grey)
        )
        binding.view2.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(if (status == "shipped" || status == "delivered") com.example.blinkitadminclone.R.color.green else com.example.blinkitadminclone.R.color.grey)
        )

        //delivered
        binding.iv4.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(if (status == "delivered") com.example.blinkitadminclone.R.color.green else com.example.blinkitadminclone.R.color.grey)
        )
        binding.view3.backgroundTintList = android.content.res.ColorStateList.valueOf(
            resources.getColor(if (status == "delivered") com.example.blinkitadminclone.R.color.green else com.example.blinkitadminclone.R.color.grey)
        )
    }

    private fun showStatusDialog() {
        val statusOptions = arrayOf("Packed", "Shipped", "Delivered")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Update Order Status")
        builder.setItems(statusOptions) { _, which ->
            val selectedStatus = statusOptions[which]
            updateOrderStatus(selectedStatus)
        }
        builder.show()
    }

    private fun updateOrderStatus(newStatus: String) {
        if (orderId == null) return

        lifecycleScope.launch {
            try {
                SupabaseClient.client.postgrest["orders"].update(
                    mapOf("status" to newStatus)
                ) {
                    filter {
                        eq("id", orderId!!)
                    }
                }
                orderStatus = newStatus
                setupStatusUI()
                Toast.makeText(requireContext(), "Status updated to $newStatus", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchOrderItems() {
        if (orderId == null) return

        lifecycleScope.launch {
            try {
                val columns = Columns.raw("*, admin(*)")
                val items = SupabaseClient.client.postgrest["order_items"]
                    .select(columns = columns) {
                        filter {
                            eq("order_id", orderId!!)
                        }
                    }
                    .decodeList<OrderItem>()

                adapterOrderItem.setOrderItemList(items)

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching items: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
