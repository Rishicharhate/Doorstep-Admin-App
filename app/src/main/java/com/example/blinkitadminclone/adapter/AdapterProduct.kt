package com.example.blinkitadminclone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.denzcoskun.imageslider.models.SlideModel
import com.example.blinkitadminclone.databinding.ItemViewProductBinding
import com.example.blinkitadminclone.models.Product

class AdapterProduct(
    val onEditButtonClicked: (Product) -> Unit
) : RecyclerView.Adapter<AdapterProduct.ProductViewHolder>(), Filterable {

    private var productList: List<Product> = emptyList()
    private var productListFull: List<Product> = emptyList()
    private var onFilterApplied: ((Int) -> Unit)? = null

    fun setProductList(list: List<Product>) {
        this.productList = list
        this.productListFull = list
        notifyDataSetChanged()
    }

    fun setOnFilterAppliedListener(listener: (Int) -> Unit) {
        this.onFilterApplied = listener
    }

    class ProductViewHolder(val binding: ItemViewProductBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemViewProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.binding.apply {
            val imageList = ArrayList<SlideModel>()
            product.productImagesUris?.forEach {
                it?.let { uri -> imageList.add(SlideModel(uri)) }
            }
            if (imageList.isNotEmpty()) {
                ivImageSlider.setImageList(imageList)
            }

            tvProductTitle.text = product.productTitle
            productQuantity.text = "${product.productQuantity} ${product.productUnit}"
            tvProductPrice.text = "₹${product.productPrice}"

            tvAdd.setOnClickListener {
                onEditButtonClicked(product)
            }
        }
    }

    override fun getItemCount(): Int = productList.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                val resultList = if (charSearch.isEmpty() || charSearch == "All") {
                    productListFull
                } else {
                    val filteredList = ArrayList<Product>()
                    for (row in productListFull) {
                        if (row.productTitle?.lowercase()?.contains(charSearch.lowercase()) == true ||
                            row.productPrice.toString().contains(charSearch) ||
                            row.productCategory?.lowercase()?.contains(charSearch.lowercase()) == true
                        ) {
                            filteredList.add(row)
                        }
                    }
                    filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = resultList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                productList = results?.values as List<Product>
                notifyDataSetChanged()
                onFilterApplied?.invoke(productList.size)
            }
        }
    }
}
