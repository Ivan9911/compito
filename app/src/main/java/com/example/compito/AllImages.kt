package com.example.mygallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.compito.R
import com.example.compito.databinding.AllImagesBinding


class AllImages :Fragment(){
  lateinit  var binding: AllImagesBinding
  var mlist= mutableListOf<PhotoModel>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
       binding= AllImagesBinding.inflate(inflater)
       return binding.root
    }
    fun notify(list:MutableList<PhotoModel>){
        mlist.addAll(list)
        println(mlist.size)
        binding.allimagesRecycler.layoutManager=GridLayoutManager(requireContext(),3,GridLayoutManager.VERTICAL,false)
        binding.allimagesRecycler.adapter=object:RecyclerView.Adapter<MyViewHolder>(){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
                val holder=MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.allimages_item,parent,false))
                holder.image.setOnClickListener {
                      it.transitionName

                }
                return holder
            }

            override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
                   Glide.with(requireContext()).load(mlist[position].uri).diskCacheStrategy(
                       DiskCacheStrategy.AUTOMATIC).centerCrop().into(holder.image)
            }

            override fun getItemCount(): Int {
                return mlist.size
            }

        }

    }
}

class MyViewHolder(view:View) :RecyclerView.ViewHolder(view){
  val image=view.findViewById<ImageView>(R.id.imageview)
}

