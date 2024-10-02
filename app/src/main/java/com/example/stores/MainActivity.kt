package com.example.stores

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.stores.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.concurrent.LinkedBlockingQueue

class MainActivity : AppCompatActivity() , OnClickListener, MainAux{

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mAdapter: StoreAdapter
    private lateinit var mGridLayout: GridLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.containerMain)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Añadir store
        /*mBinding.btnSave.setOnClickListener{
            val store = StoreEntity(name= mBinding.etName.text.toString().trim())

            Thread {
                StoreApplication.database.storeDao().addStore(store)
            }.start()

            mAdapter.add(store)
        }
        */
        mBinding.fab.setOnClickListener{
            launchEditFragment()
        }


        setupRecyclerView()
    }

    private fun launchEditFragment(args: Bundle? = null) {
        val fragment = EditStoreFragment()
        if(args != null)fragment.arguments = args

        //Gestor de android para controlar los fragmentos
        val managerFragment = supportFragmentManager
        //Como se va a ejecutar
        val fragmentTransaction = managerFragment.beginTransaction()

        fragmentTransaction.add(R.id.containerMain, fragment)
        //habilitar la vuelta hacia atrás
        fragmentTransaction.addToBackStack(null)
        //agregar cambios
        fragmentTransaction.commit()

        //Método para olcultar o mostrar el fab
        hideFab()
    }

    //Configuración RecyclerView
    private fun setupRecyclerView() {
        mAdapter = StoreAdapter(mutableListOf(), this)
        mGridLayout = GridLayoutManager(this, resources.getInteger(R.integer.main_columns))

        getStores()

        mBinding.recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = mGridLayout
            adapter = mAdapter
        }
    }

    private fun getStores(){
        //Crear cola
        val queue = LinkedBlockingQueue<MutableList<StoreEntity>>()
        //Nuevo hilo
        Thread{
            //Consulta a la base de datos
            val stores = StoreApplication.database.storeDao().getAllStores()
            //Se añade el valor de la consulta a la cola
            queue.add(stores)
        }.start()

        //Espera el resultado para ejecutar setStores()
        mAdapter.setStores(queue.take())
    }

    /*
    OnClickListener
    */

    override fun onClick(storeId: Long) {
        val args = Bundle()
        args.putLong(getString(R.string.arg_id), storeId)

        launchEditFragment(args)
    }

    override fun onFavoritesStores(storeEntity: StoreEntity) {
        storeEntity.isFavourite = !storeEntity.isFavourite
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread{
            StoreApplication.database.storeDao().updateStore(storeEntity)
            queue.add(storeEntity)
        }.start()
        upDateStore(queue.take())

    }

    override fun onDeleteStore(storeEntity: StoreEntity) {
        val items = resources.getStringArray(R.array.array_options_item)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_options_title)
            .setItems(items) { dialogInterface, i ->
                when(i){
                    0 -> confirmDelete(storeEntity)
                    1 -> dial(storeEntity.phone)
                    2 -> goToWebsite(storeEntity.website)
                }
            }
            .show()
    }

    private fun confirmDelete(storeEntity: StoreEntity){
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_delete_title)
            .setPositiveButton(R.string.dialog_delete_confirm) { dialogInterface, i ->
                val queue = LinkedBlockingQueue<StoreEntity>()
                Thread {
                    StoreApplication.database.storeDao().deleteStore(storeEntity)
                    queue.add(storeEntity)
                }.start()
                mAdapter.delete(queue.take())
            }
            .setNegativeButton(R.string.dialog_delete_cancel, null)
            .show()
    }

    //Abrir programa en segundo plano
    private fun dial (phone: String){
        val callIntent = Intent().apply {
            action = Intent.ACTION_DIAL
            data = Uri.parse("tel: $phone")
        }
        startIntent(callIntent)

    }

    //Lanzar navegador
    private fun goToWebsite(website:String){
        if(website.isEmpty()){
            Toast.makeText(this, R.string.main_error_no_website, Toast.LENGTH_LONG).show()
        }else{
            val websiteIntent = Intent().apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(website)
            }
            startIntent(websiteIntent)
        }
    }

    private fun startIntent(intent : Intent){
        //Confirmamos si existe alguna actividad para evitar errores
        if(intent.resolveActivity(packageManager) != null){
            startActivity(intent)
        }else{
            Toast.makeText(this, R.string.main_error_no_resolve, Toast.LENGTH_LONG).show()
        }
    }

    /*
    MainAux
    */
    override fun hideFab(isVisible: Boolean) {
        if (isVisible) mBinding.fab.show() else mBinding.fab.hide()
    }

    override fun addStore(storeEntity: StoreEntity) {
       mAdapter.add(storeEntity)
    }

    override fun upDateStore(storeEntity: StoreEntity){
        mAdapter.update(storeEntity)
    }
}