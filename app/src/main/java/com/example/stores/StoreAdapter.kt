package com.example.stores

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stores.databinding.ItemStoreBinding

class StoreAdapter(private var stores : MutableList<StoreEntity>, private var listener: OnClickListener): RecyclerView.Adapter<StoreAdapter.ViewHolder>() {

    private lateinit var mContext: Context


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        mContext = parent.context
        //Inflamos la vista
        val view = LayoutInflater.from(mContext).inflate(R.layout.item_store, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val store = stores[position]

        with(holder){
            setListener(store)
            binding.tvName.text = store.name
            binding.cbFavourite.isChecked = store.isFavourite

            Glide.with(mContext)
                .load(store.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(binding.imgFoto)

        }
    }


    override fun getItemCount(): Int = stores.size

    fun add(storeEntity: StoreEntity) {
        if(!stores.contains(storeEntity)){
            stores.add(storeEntity)
            notifyItemInserted(stores.size -1)
        }

    }

    fun setStores(stores: MutableList<StoreEntity>) {
        this.stores = stores
        notifyDataSetChanged()
    }

    fun update(storeEntity: StoreEntity) {
        val index = stores.indexOf(storeEntity)
        if(index != -1){
            stores[index] = storeEntity
            notifyItemChanged(index)
        }
    }

    fun delete(storeEntity: StoreEntity) {
        val index = stores.indexOf(storeEntity)
        if(index != -1){
            stores.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        //Habilitar viewBinding en build.gradle(app)
        val binding = ItemStoreBinding.bind(view)

        fun setListener(storeEntity:StoreEntity){
            with(binding.root){
                setOnClickListener{listener.onClick(storeEntity.id)}
                setOnLongClickListener{
                    listener.onDeleteStore(storeEntity)
                    true
                }
            }
            binding.cbFavourite.setOnClickListener{
                listener.onFavoritesStores(storeEntity)
            }
        }
    }
}