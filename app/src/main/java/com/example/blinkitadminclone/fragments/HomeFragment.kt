package com.example.blinkitadminclone.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.blinkitadminclone.R
import com.example.blinkitadminclone.SupabaseClient
import com.example.blinkitadminclone.adapter.AdapterCategory
import com.example.blinkitadminclone.adapter.AdapterProduct
import com.example.blinkitadminclone.databinding.EditProductLayoutBinding
import com.example.blinkitadminclone.databinding.FragmentHomeBinding
import com.example.blinkitadminclone.models.Categories
import com.example.blinkitadminclone.models.Product
import com.example.blinkitadminclone.utils.constants
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapterProduct: AdapterProduct
    private lateinit var adapterCategory: AdapterCategory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setAllCategories()
        setupSearch()
        fetchProducts()
    }

    private fun setupRecyclerViews() {
        adapterProduct = AdapterProduct { product ->
            showEditDialog(product)
        }
        binding.rvProduct.adapter = adapterProduct

        adapterProduct.setOnFilterAppliedListener { count ->
            if (count > 0) {
                binding.rvProduct.visibility = View.VISIBLE
                binding.tvText.visibility = View.GONE
            } else {
                binding.rvProduct.visibility = View.GONE
                binding.tvText.visibility = View.VISIBLE
            }
        }
    }

    private fun fetchProducts() {
        lifecycleScope.launch {
            try {
                // Fetching from "admin" table as requested
                val products = SupabaseClient.client.postgrest["admin"].select().decodeList<Product>()
                adapterProduct.setProductList(products)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setAllCategories() {
        val categoryList = ArrayList<Categories>()
        categoryList.add(Categories("All", R.drawable.baseline_category_24))
        
        for (i in constants.allProductsCategory.indices) {
            categoryList.add(
                Categories(
                    constants.allProductsCategory[i],
                    constants.allProductsCategoryIcon[i]
                )
            )
        }

        adapterCategory = AdapterCategory(categoryList) { category ->
            adapterProduct.filter.filter(category.category)
        }
        binding.rvCategories.adapter = adapterCategory
    }

    private fun setupSearch() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapterProduct.filter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showEditDialog(product: Product) {
        val editBinding = EditProductLayoutBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(editBinding.root)

        // Pre-fill existing data
        editBinding.etProductTitle.setText(product.productTitle)
        editBinding.etProductQuantity.setText(product.productQuantity.toString())
        editBinding.etProductPrice.setText(product.productPrice.toString())
        editBinding.etProductStock.setText(product.productStock.toString())
        editBinding.etProductCategory.setText(product.productCategory)
        editBinding.etProductType.setText(product.productType)
        editBinding.etProductUnit.setText(product.productUnit)

        // Setup Dropdowns for Edit Dialog
        val units = arrayOf("kg", "gm", "ltr", "ml", "pkt", "pc")
        editBinding.etProductUnit.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, units))

        val categories = constants.allProductsCategory
        editBinding.etProductCategory.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories))

        val types = arrayOf("Organic", "Processed", "Fresh", "Frozen")
        editBinding.etProductType.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, types))

        editBinding.btnAdd.text = "Update"
        editBinding.btnAdd.setOnClickListener {
            val title = editBinding.etProductTitle.text.toString()
            val quantity = editBinding.etProductQuantity.text.toString()
            val price = editBinding.etProductPrice.text.toString()
            val stock = editBinding.etProductStock.text.toString()
            val category = editBinding.etProductCategory.text.toString()
            val type = editBinding.etProductType.text.toString()
            val unit = editBinding.etProductUnit.text.toString()

            if (title.isEmpty() || quantity.isEmpty() || price.isEmpty() || stock.isEmpty() || category.isEmpty() || type.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    try {
                        val updatedProduct = product.copy(
                            productTitle = title,
                            productQuantity = quantity.toInt(),
                            productPrice = price.toInt(),
                            productStock = stock.toInt(),
                            productCategory = category,
                            productType = type,
                            productUnit = unit
                        )
                        
                        SupabaseClient.client.postgrest["admin"].update(updatedProduct) {
                            filter {
                                eq("id", product.id ?: "")
                            }
                        }
                        
                        Toast.makeText(requireContext(), "Product Updated", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        fetchProducts() // Refresh the list
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        editBinding.btnEdit.setOnClickListener { dialog.dismiss() } // Using it as Cancel button

        dialog.show()
    }
}
