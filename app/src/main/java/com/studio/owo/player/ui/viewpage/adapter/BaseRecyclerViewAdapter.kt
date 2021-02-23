package com.studio.owo.player.ui.viewpage.adapter

import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerViewAdapter<B : ViewDataBinding, D>(val list: List<D>) :
    RecyclerView.Adapter<BaseRecyclerViewAdapter<B, D>.ViewHolder>(),
    Comparable<BaseRecyclerViewAdapter<B, D>> {


    var onClickListener: (position: Int, view: View, data: D) -> Unit = { _, _, _ -> }
    var onKeyListener = View.OnKeyListener { _, _, _ -> false }
    fun father() : BaseRecyclerViewAdapter<ViewDataBinding,Any> = this as BaseRecyclerViewAdapter<ViewDataBinding,Any>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = inflate(parent)
        val holder = ViewHolder(binding)
        clickLayout(binding).setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                hasFocusHolder = holder.adapterPosition
            } else {
                notFocusHolder = holder.adapterPosition
            }
        }
        clickLayout(binding).setOnClickListener {
            onClickListener(holder.adapterPosition, holder.itemView , list[holder.adapterPosition])
        }
        clickLayout(binding).setOnKeyListener { _, keyCode, event ->
            onKeyListener.onKey(holder.itemView, keyCode, event)
        }
        return holder
    }

    abstract val inflate: (parent: ViewGroup) -> B
    abstract val clickLayout: (binder: B) -> View

    /*override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        /*holder.binding.num = position.toString()
        holder.binding.song = songs[position]*/
    }*/

    var hasFocusHolder = -1
    var notFocusHolder = -1
    var lastHasFocusHolder = -1
    override fun getItemCount(): Int = list.size
    inner class ViewHolder(val binding: B) : RecyclerView.ViewHolder(binding.root)
    override fun compareTo(other: BaseRecyclerViewAdapter<B, D>): Int = 0
}