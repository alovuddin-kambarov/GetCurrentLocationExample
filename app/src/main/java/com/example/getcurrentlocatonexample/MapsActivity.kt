package com.example.getcurrentlocatonexample

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.getcurrentlocatonexample.databinding.ActivityMapsBinding
import com.example.getcurrentlocatonexample.models.MyLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class MapsActivity : FragmentActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("activities")

        val arrayList = ArrayList<MyLocation>()

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                arrayList.clear()
                val children = snapshot.children

                for (child in children) {
                    val value = child.getValue(MyLocation::class.java)
                    if (value != null) {
                        arrayList.add(value)
                    }

                }

                arrayList.forEach {

                    val latL = LatLng(it!!.lat!!,it.long!!)
                    mMap.addMarker(MarkerOptions().position(latL).title(it.name))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latL))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latL, 15f))
                }



            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
            }
        })


    }
}