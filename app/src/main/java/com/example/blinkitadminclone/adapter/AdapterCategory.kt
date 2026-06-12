package com.example.blinkitadminclone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.blinkitadminclone.databinding.ItemViewProductCategoryBinding
import com.example.blinkitadminclone.models.Categories

class AdapterCategory(
    private val categoryList: List<Categories>,
    private val onCategoryClick: (Categories) -> Unit
) : RecyclerView.Adapter<AdapterCategory.CategoryViewHolder>() {

    class CategoryViewHolder(val binding: ItemViewProductCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemViewProductCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categoryList[position]
        holder.binding.apply {
            ivCategoryImage.setImageResource(category.icon)
            tvCategoryTitle.text = category.category
        }
        holder.itemView.setOnClickListener {
            onCategoryClick(category)
        }
    }

    override fun getItemCount(): Int = categoryList.size
}
