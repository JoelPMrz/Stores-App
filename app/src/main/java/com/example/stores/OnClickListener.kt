package com.example.stores

interface OnClickListener {
    fun onClick(storeId:Long)
    fun onFavoritesStores(storeEntity: StoreEntity)
    fun onDeleteStore(storeEntity: StoreEntity)
}