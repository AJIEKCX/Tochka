package com.example.ajiekc.tochka.widget

import android.support.annotation.CallSuper
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.example.ajiekc.tochka.LceState
import java.util.ArrayList

abstract class BaseRecyclerAdapter<VH : RecyclerView.ViewHolder, T>(items: List<T>) : RecyclerView.Adapter<VH>() {

    private val mItems = ArrayList<T>()

    private var mOnItemClickListener: OnItemClickListener<T>? = null

    private val mInternalListener = View.OnClickListener {view ->
        if (mOnItemClickListener != null) {
            val position = view.tag as Int
            val item = mItems[position]
            mOnItemClickListener!!.onItemClick(item)
        }
    }

    private var mRecyclerView: LceRecyclerView? = null

    init {
        mItems.addAll(items)
    }

    fun attachToRecyclerView(recyclerView: LceRecyclerView) {
        mRecyclerView = recyclerView
        mRecyclerView!!.adapter = this
        refreshRecycler()
    }

    fun add(value: T) {
        mItems.add(value)
        refreshRecycler()
    }

    fun addAll(values: List<T>) {
        mItems.addAll(values)
        refreshRecycler()
    }

    fun changeDataSet(values: List<T>) {
        mItems.clear()
        mItems.addAll(values)
        refreshRecycler()
    }

    fun removeLastItem() {
        if (itemCount > 0) {
            mItems.removeAt(itemCount - 1)
            refreshRecycler()
        }
    }

    fun replaceLastItem(user: T) {
        if (itemCount > 0) {
            mItems[itemCount - 1] = user
            refreshRecycler()
        }
    }

    fun clear() {
        mItems.clear()
        refreshRecycler()
    }

    private fun refreshRecycler() {
        notifyDataSetChanged()
        if (mRecyclerView != null) {
            mRecyclerView!!.updateView()
        }
    }

    fun getLastItem() : T? {
        if (itemCount > 0) {
            return mItems[itemCount - 1]
        }
        return null
    }

    fun getItem(position: Int): T {
        return mItems[position]
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    @CallSuper
    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.itemView.tag = position
        holder.itemView.setOnClickListener(mInternalListener)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<T>?) {
        mOnItemClickListener = onItemClickListener
    }

    interface OnItemClickListener<T> {
        fun onItemClick(item: T)
    }
}