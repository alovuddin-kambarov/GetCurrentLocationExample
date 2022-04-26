package com.example.getcurrentlocatonexample

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.getcurrentlocatonexample.databinding.ActivityMainBinding
import com.example.getcurrentlocatonexample.models.MyLocation
import com.example.getcurrentlocatonexample.utils.NetworkHelper
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    private lateinit var locationRequest: LocationRequest
    private lateinit var binding: ActivityMainBinding
    private val abs = 10001
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        turnOnGPS()
        allow()
        getLocation()


    }

    private fun getLocation() {

        binding.button.setOnClickListener {
            if (NetworkHelper(this).isNetworkConnected()) {
                if (isGPSEnabled()) {
                    getMyLocation()
                } else {
                    Toast.makeText(
                        this,
                        "Your GPS location is disabled. When GPS is enabled, you can use this app",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(this, "Please check your internet!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun turnOnGPS() {

        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val result = LocationServices.getSettingsClient(this)
            .checkLocationSettings(builder.build())



        result.addOnCompleteListener { task ->
            try {
                val response = task.getResult(ApiException::class.java)


                Toast.makeText(this@MainActivity, "GPS is already toured on", Toast.LENGTH_SHORT)
                    .show()


            } catch (e: ApiException) {

                when (e.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->

                        try {

                            val resolvableApiException = e as ResolvableApiException
                            resolvableApiException.startResolutionForResult(
                                this,
                                abs
                            )


                        } catch (e: IntentSender.SendIntentException) {
                            e.printStackTrace()
                        }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                        //Device does not have location
                    }
                }
            }
        }


    }

    private fun isGPSEnabled(): Boolean {
        var locationManager: LocationManager? = null

        if (locationManager == null) {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    @SuppressLint("SetTextI18n")
    fun getMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            println()
        } else {
            return
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val lastLocation = fusedLocationProviderClient.lastLocation

        lastLocation.addOnCompleteListener { task ->

            val result = task.result

            if (result != null && binding.name.text.isNotBlank()) {

                //val mLocation = LatLng(result.latitude, result.longitude)
                /*  mMap.addMarker(MarkerOptions().position(mLocation).title("?"))
                  mMap.moveCamera(CameraUpdateFactory.newLatLng(mLocation))
*/
                /*       val location = Location()


                       mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 15f))*/

                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("activities/${binding.name.text}")


                myRef.setValue(
                    MyLocation(
                        binding.name.text.toString(),
                        result.latitude,
                        result.longitude
                    )
                )


                startActivity(Intent(this, MapsActivity::class.java))


            }

        }


    }

    private fun allow() {

        askPermission(
            Manifest.permission.ACCESS_FINE_LOCATION
        ) {

            if (it.isAccepted) {
                Toast.makeText(this, ":)", Toast.LENGTH_SHORT).show()
            }

        }.onDeclined { e ->
            if (e.hasDenied()) {

                AlertDialog.Builder(this)
                    .setMessage("Please, allow our permissions!!!")
                    .setPositiveButton(
                        "Ok"
                    ) { _, _ -> e.askAgain() }
                    .setNegativeButton(
                        "No"
                    ) { dialog, _ -> dialog?.dismiss() }
                    .show()


            }
            if (e.hasForeverDenied()) {

                e.goToSettings()
            }


        }
    }

}
