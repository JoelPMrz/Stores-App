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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
            mIsEditMode = false
            mStoreEntity = StoreEntity(name = "", phone = "", photoUrl = "")
        }

        setupActionBar()
        setupTextFields()
    }

    private fun setupActionBar() {
        //Obtenemos la actividad en la que se aloja el fragment y casteamos
        mActivity = activity as? MainActivity

        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //Cambiar título
        mActivity?.supportActionBar?.title = if(mIsEditMode) getString(R.string.edit_store_title_edit)
            else getString(R.string.edit_store_title_add)


        setHasOptionsMenu(true)
    }

    private fun setupTextFields() {
        with(mBinding) {
            etName.addTextChangedListener { validateFields(mBinding.tilName) }
            etPhone.addTextChangedListener { validateFields(mBinding.tilPhone) }
            etPhotoUrl.addTextChangedListener {
                validateFields(mBinding.tilPhotoUrl)
                loadImage(it.toString().trim())
            }
        }
    }

    private fun loadImage(url : String){
        Glide.with(this)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()
            .into(mBinding.imgPhoto)

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
                if(mStoreEntity != null && validateFields(mBinding.tilPhotoUrl, mBinding.tilPhone,mBinding.tilName)){
                    with(mStoreEntity!!){
                        name =  mBinding.etName.text.toString().trim()
                        phone = mBinding.etPhone.text.toString().trim()
                        website = mBinding.etWebsite.text.toString().trim()
                        photoUrl = mBinding.etPhotoUrl.text.toString().trim()
                    }

                    val queue = LinkedBlockingQueue<StoreEntity?>()
                    Thread{
                        if(mIsEditMode) StoreApplication.database.storeDao().updateStore(mStoreEntity!!)
                        else mStoreEntity!!.id = StoreApplication.database.storeDao().addStore(mStoreEntity!!)
                        queue.add(mStoreEntity)
                    }.start()

                    with(queue.take()){

                        hideKeyBoard()

                        if(mIsEditMode){
                            mActivity?.upDateStore(mStoreEntity!!)
                            Snackbar.make(mBinding.root, R.string.edit_store_message_update_success, Toast.LENGTH_SHORT).show()
                        }else {
                            mActivity?.addStore(mStoreEntity!!)

                            Toast.makeText(
                                mActivity,
                                getString(R.string.edit_store_message_save_success),
                                Toast.LENGTH_SHORT
                            ).show()

                            mActivity?.onBackPressedDispatcher?.onBackPressed()
                        }
                    }
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun validateFields(vararg textFields: TextInputLayout):Boolean{
        var isValid = true

        for (textField in textFields){
            if(textField.editText?.text.toString().trim().isEmpty()){
                textField.error = getString(R.string.helper_required)
                isValid = false
            }else{
                textField.error = null
            }
        }

        if(!isValid){
            Snackbar.make(mBinding.root, R.string.edit_store_message_valid, Snackbar.LENGTH_SHORT).show()
        }

        return isValid
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