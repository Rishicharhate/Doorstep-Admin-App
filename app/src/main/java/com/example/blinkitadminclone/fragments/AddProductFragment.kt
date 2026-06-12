package com.example.blinkitadminclone.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitadminclone.SupabaseClient
import com.example.blinkitadminclone.adapter.AdapterSelectedImage
import com.example.blinkitadminclone.databinding.FragmentAddProductBinding
import com.example.blinkitadminclone.models.Product
import com.example.blinkitadminclone.utils.constants
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import java.util.UUID

class AddProductFragment : Fragment() {

    private lateinit var binding: FragmentAddProductBinding
    private val selectedImageUris = ArrayList<Uri>()
    private lateinit var adapterSelectedImage: AdapterSelectedImage

    private val selectImagesLauncher =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isNotEmpty()) {
                selectedImageUris.clear()
                selectedImageUris.addAll(uris)
                adapterSelectedImage.notifyDataSetChanged()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupRecyclerView()

        binding.btnSelectImage.setOnClickListener {
            selectImagesLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnAddProduct.setOnClickListener {
            validateAndUpload()
        }
    }

    private fun setupDropdowns() {
        val units = arrayOf("kg", "gm", "ltr", "ml", "pkt", "pc")
        val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, units)
        binding.etProductUnit.setAdapter(unitAdapter)

        // Using constants for categories
        val categories = constants.allProductsCategory
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        binding.etProductCategory.setAdapter(categoryAdapter)

        val types = arrayOf("Organic", "Processed", "Fresh", "Frozen")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, types)
        binding.etProductType.setAdapter(typeAdapter)
    }

    private fun setupRecyclerView() {
        adapterSelectedImage = AdapterSelectedImage(selectedImageUris)
        binding.rvProductImage.adapter = adapterSelectedImage
    }

    private fun validateAndUpload() {
        val title = binding.etProductTitle.text.toString()
        val quantity = binding.etProductQuantity.text.toString()
        val unit = binding.etProductUnit.text.toString()
        val price = binding.etProductPrice.text.toString()
        val stock = binding.etProductStock.text.toString()
        val category = binding.etProductCategory.text.toString()
        val type = binding.etProductType.text.toString()

        if (title.isEmpty() || quantity.isEmpty() || unit.isEmpty() || price.isEmpty() ||
            stock.isEmpty() || category.isEmpty() || type.isEmpty() || selectedImageUris.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Please fill all details and select images", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                binding.btnAddProduct.isEnabled = false
                Toast.makeText(requireContext(), "Uploading product...", Toast.LENGTH_SHORT).show()

                val uploadedImageUrls = uploadImages()
                
                val product = Product(
                    id = UUID.randomUUID().toString(),
                    productTitle = title,
                    productQuantity = quantity.toInt(),
                    productUnit = unit,
                    productPrice = price.toInt(),
                    productStock = stock.toInt(),
                    productCategory = category,
                    productType = type,
                    productImagesUris = ArrayList(uploadedImageUrls)
                )

                SupabaseClient.client.postgrest["admin"].insert(product)

                Toast.makeText(requireContext(), "Product Added Successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnAddProduct.isEnabled = true
            }
        }
    }

    private suspend fun uploadImages(): List<String> {
        val urls = mutableListOf<String>()
        val bucket = SupabaseClient.client.storage.from("product-images")

        selectedImageUris.forEach { uri ->
            val fileName = "products/${UUID.randomUUID()}.jpg"
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: throw Exception("Could not read image bytes")
            inputStream.close()

            bucket.upload(fileName, bytes)
            val publicUrl = bucket.publicUrl(fileName)
            urls.add(publicUrl)
        }
        return urls
    }
}
