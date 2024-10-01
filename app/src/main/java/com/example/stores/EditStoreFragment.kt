package com.example.stores

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.stores.databinding.FragmentEditStoreBinding
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.LinkedBlockingQueue

class EditStoreFragment : Fragment() {

    private lateinit var mBinding: FragmentEditStoreBinding
    private var mActivity: MainActivity? = null
    private var mIsEditMode : Boolean = false
    private var mStoreEntity : StoreEntity? = null

    //Se vincula y se puede inicializar el layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,): View? {

        mBinding = FragmentEditStoreBinding.inflate(inflater,container,false)

        return mBinding.root
    }

    //Se crea por completo
    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id = arguments?.getLong(getString(R.string.arg_id),0)
        if(id != null && id != 0L){
           mIsEditMode = true
            getStore(id)
        }else{
            Toast.makeText(activity, id.toString(), Toast.LENGTH_SHORT).show()
        }

        //Conseguimos la actividad en la que se aloja el fragmente y casteamos
        mActivity = activity as? MainActivity

        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //Cambiar titulo
        mActivity?.supportActionBar?.title = getString(R.string.edit_store_title_add)

        setHasOptionsMenu(true)

        mBinding.etPhotoUrl.addTextChangedListener{
            Glide.with(this)
                .load(mBinding.etPhotoUrl.text.toString())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(mBinding.imgPhoto)

        }
    }

    private fun getStore(id:Long) {
        val queue =  LinkedBlockingQueue<StoreEntity?>()
        Thread{
            mStoreEntity = StoreApplication.database.storeDao().getStoreById(id)
            queue.add(mStoreEntity)
        }.start()

        queue.take()?.let {
            setUiStore(it!!)
        }
    }

    private fun setUiStore(it: StoreEntity) {
        with(mBinding){
            etName.setText(it.name)
            etPhone.setText(it.phone)
            etWebsite.setText(it.website)
            etPhotoUrl.setText(it.photoUrl)
            Glide.with(requireActivity())
                .load(it.photoUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .into(mBinding.imgPhoto)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_save, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                mActivity?.onBackPressedDispatcher?.onBackPressed()
                true
            }
            R.id.action_save -> {
                val store = StoreEntity(name =  mBinding.etName.text.toString().trim(),
                    phone = mBinding.etPhone.text.toString().trim(),
                    website = mBinding.etWebsite.text.toString().trim(),
                    photoUrl = mBinding.etPhotoUrl.text.toString().trim())

                val queue = LinkedBlockingQueue<Long?>()
                Thread{
                    val id = StoreApplication.database.storeDao().addStore(store)
                    store.id = id
                    queue.add(id)
                }.start()

                queue.take()?.let{
                    mActivity?.addStore(store)
                    hideKeyBoard()

                    Toast.makeText(mActivity, getString(R.string.edit_store_message_save_success), Toast.LENGTH_SHORT)
                        .show()

                    mActivity?.onBackPressedDispatcher?.onBackPressed()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    //Ocultar el teclado
    private fun hideKeyBoard(){
        val imn = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        imn.hideSoftInputFromWindow(requireView().windowToken, 0)

    }
    //Ciclo de vida en el que se desvincula la vista antes de onDestroy()
    override fun onDestroyView() {
        hideKeyBoard()
        super.onDestroyView()
    }

    override fun onDestroy() {
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        mActivity?.supportActionBar?.title = getString(R.string.app_name)
        mActivity?.hideFab(true)

        setHasOptionsMenu(false)
        super.onDestroy()
    }





}