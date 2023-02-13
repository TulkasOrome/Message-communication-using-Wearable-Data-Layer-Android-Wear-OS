package com.betterbrick.proofofconcept;

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant

var sensorManager: SensorManager? = null
var sensorG: Sensor? = null
var sensorA: Sensor? = null

class SensorService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        sensorG = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorA = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

         val mLightSensorListener: SensorEventListener = object : SensorEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onSensorChanged(event: SensorEvent) {
                try {
                    val `object` = JSONObject()
                    `object`.put("gyroscopeX ", event.values[0].toInt())
                    `object`.put("gyroscopeY ", event.values[1].toInt())
                    `object`.put("gyroscopeZ ", event.values[2].toInt())
                    `object`.put("timestamp", Instant.now().toString())
                    com.betterbrick.proofofconcept.MessageSender(
                        "/MessageChannel",
                        `object`.toString(),
                        applicationContext
                    ).start()
                } catch (e: JSONException) {
                    Log.e(ContentValues.TAG, "Failed to create JSON object")
                }

                Log.d("MY_APP", event.values[0].toString())
                /* if (mobileDeviceConnected) {

                     val nodeId: String = messageEvent?.sourceNodeId!!
                     // Set the data of the message to be the bytes of the Uri.
                     val payload: ByteArray =
                         event.values[0].toString().toByteArray()

                     // Send the rpc
                     // Instantiates clients without member variables, as clients are inexpensive to
                     // create. (They are cached and shared between GoogleApi instances.)
                     val sendMessageTask =
                         Wearable.getMessageClient(activityContext!!)
                             .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)

                     binding.deviceconnectionStatusTv.visibility = View.GONE
                 }*/

            }




             override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                 Log.d("MY_APP", "${sensor.id} - $accuracy")
             }
         }
        sensorManager?.registerListener(mLightSensorListener, sensorG, SensorManager.SENSOR_DELAY_NORMAL)



        return START_STICKY
}


fun onStopCommand(intent: Intent, flags: Int, startId: Int): Int{
TODO("Return the communication channel to the service.")
}
    fun initialise(){

    }
}