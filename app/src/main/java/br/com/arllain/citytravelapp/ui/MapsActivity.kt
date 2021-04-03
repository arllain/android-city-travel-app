package br.com.arllain.citytravelapp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import br.com.arllain.citytravelapp.R
import br.com.arllain.citytravelapp.model.Pacote

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var pacote: Pacote? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        pacote = intent.getParcelableExtra<Pacote>(MainActivity.MAIN_ACTIVITY_IMAGE_EXTRA_ID)

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

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }else {
            val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            ActivityCompat.requestPermissions(this, permissions, 201)
        }


        // Add a marker in Sydney and move the camera
        val cityLocation = getCityLocation(pacote?.local)
        mMap.addMarker(MarkerOptions().position(cityLocation).title("${pacote?.local}"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(cityLocation))
    }

    private fun getCityLocation(local: String?): LatLng {
        return when (local) {
            "São Paulo" -> LatLng(-23.5489, -46.6388)
            "Belo Horizonte" -> LatLng(-19.8157, -43.9542)
            "Rio de Janeiro" -> LatLng(-22.9035, -43.2096)
            "Salvador" -> LatLng(-12.9704, -38.5124)
            "Foz do Iguaçu" -> LatLng(-25.5469, -54.5882)
            else -> LatLng(-8.0578, -34.882)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == 201
                && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
    }

}