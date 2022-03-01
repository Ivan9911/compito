package com.example.mygallery

import android.net.Uri
import com.google.android.gms.maps.model.LatLng

class PhotoModel(id:Long,date:Long,uri:Uri) {
    var id=id
    var date=date
    var uri=uri
    var location:LatLng?=null
}