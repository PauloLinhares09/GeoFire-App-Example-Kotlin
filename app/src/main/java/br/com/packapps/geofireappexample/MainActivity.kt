package br.com.packapps.geofireappexample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.HashMap

class MainActivity : AppCompatActivity() {

    var db : DatabaseReference? = null
    var geofire : GeoFire? = null

    var timeMillisId : Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //patio belem : -1.4580218, -48.4968418
        //grao pará : -1.3904519, -48.4673761
        //** The distance between two points is approximately 8km

        timeMillisId = System.currentTimeMillis()

        db = FirebaseDatabase.getInstance().getReference("geo/providers")
        geofire = GeoFire(db)

        //### set Location
        setLocationInDatabase(timeMillisId.toString())

        //### Getting providers
        val geoQuery : GeoQuery = geofire!!.queryAtLocation(GeoLocation(-1.4580218, -48.4968418), 7.0)

        geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                Log.i("TAG", String.format("Provider %s is within your search range [%f,%f]", key, location.latitude, location.longitude))
            }

            override fun onKeyExited(key: String) {
                Log.i("TAG", String.format("Provider %s is no longer in the search area", key))
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                Log.i("TAG", String.format("Provider %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude))
            }

            override fun onGeoQueryReady() {
                Log.i("TAG", "onGeoQueryReady")
            }

            override fun onGeoQueryError(error: DatabaseError) {
                Log.e("TAG", "error: " + error)
            }
        })


        //### listener for disconnect
        db!!.child(timeMillisId.toString()).onDisconnect().removeValue()
        db!!.child("details").child(timeMillisId.toString()).onDisconnect().removeValue()

    }

    private fun setLocationInDatabase(key : String) {
        geofire!!.setLocation(key, GeoLocation(-1.3904518, -48.4673762), GeoFire.CompletionListener { key, error ->
            if (error == null) {
                Log.i("TAG", "geo added successful: " + key)
                //Save detail
                val moreDataProvider: HashMap<String, Any> = hashMapOf("car" to "Honda Fit", "color" to "grey", "is_available" to 0)
                db!!.child("details").child(key).setValue(moreDataProvider)

            }else {
                Log.i("TAG", "geo added error: " + error.message)
            }
        })
    }

}
