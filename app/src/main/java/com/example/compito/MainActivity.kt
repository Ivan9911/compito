package com.example.mygallery

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.media.ExifInterface
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.example.compito.R
import com.example.compito.databinding.ActivityMainBinding

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class MainActivity : AppCompatActivity(),OnMapReadyCallback,GoogleMap.OnMarkerClickListener {
   lateinit var binding: ActivityMainBinding
    var photos= mutableListOf<PhotoModel>()
    var relativeImages= mutableListOf<Int>()
    var areinit=false
    lateinit var bottom:BottomSheetBehavior<ConstraintLayout>
    var markers= mutableListOf<Marker>()
    lateinit var mMap:GoogleMap
    var fragments= mutableListOf<Fragment>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (supportFragmentManager.findFragmentById(R.id.mappa) as SupportMapFragment).getMapAsync(this)
        val view=binding.bottom
        bottom=BottomSheetBehavior.from(binding.bottom).apply {
            peekHeight=200
            binding.recycler.layoutManager=LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
            binding.recycler.adapter=object:RecyclerView.Adapter<MyViewHolder>(){
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                   return MyViewHolder(layoutInflater.inflate(R.layout.allimages_item,parent,false))
                }

                override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
                    Glide.with(this@MainActivity).load(photos[relativeImages[position]].uri).centerCrop().into(holder.image)
                }

                override fun getItemCount(): Int {
                   return relativeImages.size
                }


            }
        }


    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
           requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
        }else {


        }
    }
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
       when(requestCode){
           1->{
               if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                   initImages()
               }else{

               }
           }


       }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun initImages() {
        val job= CoroutineScope(Dispatchers.IO)
        job.async {
          val res=async {
              val collection = arrayOf(MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
              var projection = arrayOf(
                  MediaStore.Images.Media.BUCKET_ID,
                  MediaStore.Images.Media.DATE_TAKEN,
                  MediaStore.Images.Media._ID


              )
              var sort = "${MediaStore.Images.Media.DATE_TAKEN} ASC"
              var cursor = contentResolver.query(
                  MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                  projection,
                  null,
                  null,
                  sort
              )
              cursor!!.moveToFirst()
              for (i in 0..cursor!!.count - 1) {
                  val date = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                  val id = cursor.getColumnIndex(MediaStore.Images.Media._ID)

                      photos.add(
                          PhotoModel(
                              cursor.getLong(id),
                              cursor.getLong(id),
                              ContentUris.withAppendedId(
                                  MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                  cursor.getLong(id))

                          )
                      )


                  cursor.moveToNext()
              }
              setlocations()

          }.await()
            initMarkers()

            println("${photos.size}")

        }
    }

    private fun initMarkers() {
        runOnUiThread {

            for (i in photos) {

                if (i.location != null && (i.location!!.latitude!=0.0&& i.location!!.longitude!=0.0)) {
                  var  marker=mMap.addMarker(MarkerOptions().position(i.location!!))
                    markers.add(marker)
                }
            }
           
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap!!.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
       mMap.setOnMarkerClickListener(this)
mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.style))
            initImages()


    }
 @RequiresApi(Build.VERSION_CODES.N)
 suspend fun setlocations(){
     println(" number of ${photos.size}")
     val float = FloatArray(2)
     for (i in 0..photos.size - 1) {
             val exif = ExifInterface(contentResolver.openInputStream(photos[i].uri)!!)
             exif.getLatLong(float)

             photos[i].location= LatLng(float[0].toDouble(),float[1].toDouble())
         }
println("finish")
 }

    override fun onMarkerClick(p0: Marker?): Boolean {
        relativeImages.clear()
        for(i in 0..photos.size-1){
            if(p0!!.position.latitude==photos[i].location!!.latitude&&p0!!.position.longitude==photos[i].location!!.longitude){
                relativeImages.add(i)
            }
        }
        binding.recycler.adapter!!.notifyDataSetChanged()
        bottom.state=BottomSheetBehavior.STATE_EXPANDED
        return true
    }
    class MyViewHolder(view: View):RecyclerView.ViewHolder(view){
        val image=view.findViewById<ImageView>(R.id.imageview)
    }
}