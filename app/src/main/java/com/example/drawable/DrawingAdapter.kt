package com.example.drawable

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.drawable.databinding.DrawingItemBinding

class DrawingAdapter(private var drawings: List<Drawing>, private val sendClick: (index: Int) -> Unit
): RecyclerView.Adapter<DrawingAdapter.DrawingViewHolder>() {

    /**
     * Updates the list of drawings
     * @param newDrawings the new list of drawings
     */
    @SuppressLint("NotifyDataSetChanged")
    fun updateDrawings(newDrawings: List<Drawing>){
        drawings = newDrawings
        notifyDataSetChanged()
    }

    inner class DrawingViewHolder(val binding: DrawingItemBinding): RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DrawingViewHolder {
        return DrawingViewHolder(DrawingItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    /**
     * Binds the drawing item with its required values
     */
    override fun onBindViewHolder(holder: DrawingViewHolder, position: Int) {
        val item = drawings[position]
//        holder.binding.dateView.text = item.date
//        holder.binding.title.text = item.name
        holder.binding.drawing.setImageBitmap(item.bitmap)
        holder.binding.drawingItem.setOnClickListener { sendClick(position) }
    }

    /**
     *Gets the count of the items in the list
     */
    override fun getItemCount(): Int = drawings.size

}
