package com.example.blinkitadminclone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitadminclone.R
import com.example.blinkitadminclone.SupabaseClient
import com.example.blinkitadminclone.adapter.AdapterOrders
import com.example.blinkitadminclone.databinding.FragmentOrderBinding
import com.example.blinkitadminclone.models.Order
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.launch

class OrdersFragment : Fragment() {

    private lateinit var binding: FragmentOrderBinding
    private lateinit var adapterOrders: AdapterOrders

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOrderBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchOrders()

        binding.tbProfileFragment.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapterOrders = AdapterOrders { order ->
            val bundle = Bundle().apply {
                putString("orderId", order.id)
                putString("orderStatus", order.status)
            }
            findNavController().navigate(R.id.action_orderFragment_to_orderDetailFragment, bundle)
        }
        binding.rvOrders.adapter = adapterOrders
    }

    private fun fetchOrders() {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.shimmerViewContainer.startShimmer()
        binding.rvOrders.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val columns = Columns.raw("*, profiles(*)")
                val orders = SupabaseClient.client.postgrest["orders"]
                    .select(columns = columns)
                    .decodeList<Order>()

                adapterOrders.setOrderList(orders)

                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.rvOrders.visibility = View.VISIBLE

            } catch (e: Exception) {
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                Toast.makeText(requireContext(), "Error fetching orders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
