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
var aEventListener: SensorEventListener? = null
var mLightSensorListener: SensorEventListener? = null

class SensorService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val extras = intent.extras
        if (extras != null) {
            Log.d("param", extras.get("Start").toString())
            Log.d("param2", extras.get("Stop").toString())
        }


        if (extras != null) {
            if (extras.get("Start") == true){
                Log.d("param", extras.get("Start").toString())



                sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
                sensorG = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
                sensorA = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//changw to global variable
                 aEventListener = object : SensorEventListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onSensorChanged(event: SensorEvent) {
                        try {
                            val `object` = JSONObject()
                            `object`.put("accelerometerX", event.values[0].toInt())
                            `object`.put("accelerometerY", event.values[1].toInt())
                            `object`.put("accelerometerZ", event.values[2].toInt())
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


                    }

                    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                        Log.d("MY_APP", "${sensor.id} - $accuracy")
                    }}

                 mLightSensorListener = object : SensorEventListener {
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


                    }


                    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                        Log.d("MY_APP", "${sensor.id} - $accuracy")
                    }
                }
                sensorManager?.registerListener(mLightSensorListener, sensorG, SensorManager.SENSOR_DELAY_NORMAL)
                sensorManager?.registerListener(aEventListener, sensorA, SensorManager.SENSOR_DELAY_NORMAL)



                return START_STICKY
            }
          else if (extras.get("Stop") == true)  {

                sensorManager?.unregisterListener(mLightSensorListener)
                sensorManager?.unregisterListener(aEventListener)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        sensorManager?.unregisterListener(mLightSensorListener)
        sensorManager?.unregisterListener(aEventListener)
        stopSelf()


    }
fun onStopCommand(intent: Intent, flags: Int, startId: Int): Int{
stopSelf()
    return STOP_FOREGROUND_REMOVE
    }
    fun initialise(){

    }}
