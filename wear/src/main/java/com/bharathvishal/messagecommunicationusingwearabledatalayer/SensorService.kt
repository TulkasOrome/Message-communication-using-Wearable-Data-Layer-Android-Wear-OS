package com.betterbrick.proofofconcept;

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.provider.SyncStateContract
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import org.json.JSONException
import org.json.JSONObject
import java.time.Instant

var sensorManager: SensorManager? = null
var sensorG: Sensor? = null
var sensorA: Sensor? = null
var sensorLA: Sensor? = null
var sensorRV: Sensor? = null
var aEventListener: SensorEventListener? = null
var mLightSensorListener: SensorEventListener? = null
var LAeventListener: SensorEventListener? = null
var RVeventListener: SensorEventListener? = null

class SensorService : Service() {


    private val CHANNEL_ID = "ForegroundService Kotlin"


    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {


        val input = intent?.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, 0
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BB Foreground Service Running")
            .setContentText(input)

            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)
        //stopSelf();




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
                sensorRV = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
                sensorLA = sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

                LAeventListener = object : SensorEventListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onSensorChanged(event: SensorEvent) {
                        try {
                            val `object` = JSONObject()
                            `object`.put("linearAccelX", event.values[0].toInt())
                            `object`.put("linearAccelY", event.values[1].toInt())
                            `object`.put("linearAccelZ", event.values[2].toInt())
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

                RVeventListener = object : SensorEventListener {
                    @RequiresApi(Build.VERSION_CODES.O)
                    override fun onSensorChanged(event: SensorEvent) {
                        try {
                            val `object` = JSONObject()
                            `object`.put("rotationVectorX", event.values[0].toInt())
                            `object`.put("rotationVectorY", event.values[1].toInt())
                            `object`.put("rotationVectorZ", event.values[2].toInt())
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
                sensorManager?.registerListener(LAeventListener, sensorG, SensorManager.SENSOR_DELAY_NORMAL)
                sensorManager?.registerListener(RVeventListener, sensorA, SensorManager.SENSOR_DELAY_NORMAL)



                return START_STICKY
            }
          else if (extras.get("Stop") == true)  {

                sensorManager?.unregisterListener(mLightSensorListener)
                sensorManager?.unregisterListener(aEventListener)
                sensorManager?.unregisterListener(LAeventListener)
                sensorManager?.unregisterListener(RVeventListener)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        sensorManager?.unregisterListener(LAeventListener)
        sensorManager?.unregisterListener(RVeventListener)
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
