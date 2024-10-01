package com.example.stores

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.stores.databinding.ActivityMainBinding
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
        mGridLayout = GridLayoutManager(this, 2)

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
        mAdapter.update(queue.take())

    }

    override fun onDeleteStore(storeEntity: StoreEntity) {
        val queue = LinkedBlockingQueue<StoreEntity>()
        Thread{
            StoreApplication.database.storeDao().deleteStore(storeEntity)
            queue.add(storeEntity)
        }.start()
        mAdapter.delete(queue.take())
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