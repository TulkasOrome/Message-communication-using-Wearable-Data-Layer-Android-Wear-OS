package com.betterbrick.proofofconcept

import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.betterbrick.proofofconcept.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.nio.charset.StandardCharsets
import java.util.*
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope(),

    DataClient.OnDataChangedListener,
    MessageClient.OnMessageReceivedListener,
    CapabilityClient.OnCapabilityChangedListener {
    var activityContext: Context? = null
    private val wearableAppCheckPayload = "AppOpenWearable"
    private val wearableAppCheckPayloadReturnACK = "AppOpenWearableACK"
    private var wearableDeviceConnected: Boolean = false

    private var currentAckFromWearForAppOpenCheck: String? = null
    private val APP_OPEN_WEARABLE_PAYLOAD_PATH = "/APP_OPEN_WEARABLE_PAYLOAD"

    private val MESSAGE_ITEM_RECEIVED_PATH: String = "/message-item-received"

    private val TAG_GET_NODES: String = "getnodes1"
    private val TAG_MESSAGE_RECEIVED: String = "receive1"

    private var messageEvent: MessageEvent? = null
    private var wearableNodeUri: String? = null

    private lateinit var binding: ActivityMainBinding
    public val dataUpdate = StringBuilder()
    var dataStream = ArrayList<String>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        activityContext = this
        wearableDeviceConnected = false


        binding.checkwearablesButton.setOnClickListener {
            if (!wearableDeviceConnected) {
                val tempAct: Activity = activityContext as MainActivity
                //Couroutine
                initialiseDevicePairing(tempAct)
            }
        }

     //   binding.saveButton.setOnClickListener {
      //      writeFileOnInternalStorage(activityContext as MainActivity, "data.txt", dataUpdate.toString())
    //    }



        /* binding.sendmessageButton.setOnClickListener {
            if (wearableDeviceConnected) {
                if (binding.messagecontentEditText.text!!.isNotEmpty()) {

                    val nodeId: String = messageEvent?.sourceNodeId!!
                    // Set the data of the message to be the bytes of the Uri.
                    val payload: ByteArray =
                        binding.messagecontentEditText.text.toString().toByteArray()

                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask =
                        Wearable.getMessageClient(activityContext!!)
                            .sendMessage(nodeId, MESSAGE_ITEM_RECEIVED_PATH, payload)

                    sendMessageTask.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("send1", "Message sent successfully")
                            val sbTemp = StringBuilder()
                            sbTemp.append("\n")
                            sbTemp.append(binding.messagecontentEditText.text.toString())
                            sbTemp.append(" (Sent to Wearable)")
                            Log.d("receive1", " $sbTemp")
                            binding.messagelogTextView.append(sbTemp)

                            binding.scrollviewText.requestFocus()
                            binding.scrollviewText.post {
                                binding.scrollviewText.scrollTo(0, binding.scrollviewText.bottom)
                            }
                        } else {
                            Log.d("send1", "Message failed.")
                        }
                    }
                } else {
                    Toast.makeText(
                        activityContext,
                        "Message content is empty. Please enter some message and proceed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
*/
    }

    @SuppressLint("SetTextI18n")
    private fun initialiseDevicePairing(tempAct: Activity) {
        //Coroutine
        launch(Dispatchers.Default) {
            var getNodesResBool: BooleanArray? = null

            try {
                getNodesResBool =
                    getNodes(tempAct.applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //UI Thread
            withContext(Dispatchers.Main) {
                if (getNodesResBool!![0]) {
                    //if message Acknowlegement Received
                    if (getNodesResBool[1]) {
                        Toast.makeText(
                            activityContext,
                            "Wearable device paired and app is open. Tap the \"Send Message to Wearable\" button to send the message to your wearable device.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.deviceconnectionStatusTv.text =
                            "Wearable device paired and app is open."
                        binding.deviceconnectionStatusTv.visibility = View.VISIBLE
                        wearableDeviceConnected = true
                       // binding.sendmessageButton.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            activityContext,
                            "A wearable device is paired but the wearable app on your watch isn't open. Launch the wearable app and try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        binding.deviceconnectionStatusTv.text =
                            "Wearable device paired but app isn't open."
                        binding.deviceconnectionStatusTv.visibility = View.VISIBLE
                        wearableDeviceConnected = false
                       // binding.sendmessageButton.visibility = View.GONE
                    }
                } else {
                    Toast.makeText(
                        activityContext,
                        "No wearable device paired. Pair a wearable device to your phone using the Wear OS app and try again.",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.deviceconnectionStatusTv.text =
                        "Wearable device not paired and connected."
                    binding.deviceconnectionStatusTv.visibility = View.VISIBLE
                    wearableDeviceConnected = false
                   // binding.sendmessageButton.visibility = View.GONE
                }
            }
        }
    }


    private fun getNodes(context: Context): BooleanArray {
        val nodeResults = HashSet<String>()
        val resBool = BooleanArray(2)
        resBool[0] = false //nodePresent
        resBool[1] = false //wearableReturnAckReceived
        val nodeListTask =
            Wearable.getNodeClient(context).connectedNodes
        try {
            // Block on a task and get the result synchronously (because this is on a background thread).
            val nodes =
                Tasks.await(
                    nodeListTask
                )
            Log.e(TAG_GET_NODES, "Task fetched nodes")
            for (node in nodes) {
                Log.e(TAG_GET_NODES, "inside loop")
                nodeResults.add(node.id)
                try {
                    val nodeId = node.id
                    // Set the data of the message to be the bytes of the Uri.
                    val payload: ByteArray = wearableAppCheckPayload.toByteArray()
                    // Send the rpc
                    // Instantiates clients without member variables, as clients are inexpensive to
                    // create. (They are cached and shared between GoogleApi instances.)
                    val sendMessageTask =
                        Wearable.getMessageClient(context)
                            .sendMessage(nodeId, APP_OPEN_WEARABLE_PAYLOAD_PATH, payload)
                    try {
                        // Block on a task and get the result synchronously (because this is on a background thread).
                        val result = Tasks.await(sendMessageTask)
                        Log.d(TAG_GET_NODES, "send message result : $result")
                        resBool[0] = true
                        //Wait for 1000 ms/1 sec for the acknowledgement message
                        //Wait 1
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(100)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 1")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        //Wait 2
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(150)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 2")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        //Wait 3
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(200)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 3")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        //Wait 4
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(250)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 4")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        //Wait 5
                        if (currentAckFromWearForAppOpenCheck != wearableAppCheckPayloadReturnACK) {
                            Thread.sleep(350)
                            Log.d(TAG_GET_NODES, "ACK thread sleep 5")
                        }
                        if (currentAckFromWearForAppOpenCheck == wearableAppCheckPayloadReturnACK) {
                            resBool[1] = true
                            return resBool
                        }
                        resBool[1] = false
                        Log.d(
                            TAG_GET_NODES,
                            "ACK thread timeout, no message received from the wearable "
                        )
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                } catch (e1: Exception) {
                    Log.d(TAG_GET_NODES, "send message exception")
                    e1.printStackTrace()
                }
            } //end of for loop
        } catch (exception: Exception) {
            Log.e(TAG_GET_NODES, "Task failed: $exception")
            exception.printStackTrace()
        }
        return resBool
    }


    override fun onDataChanged(p0: DataEventBuffer) {
    }



  /*  fun createDataStream(data: String) {

        if (dataStream.size <= 50) {
             dataStream.add(data)
             checkForDelta(dataStream)
        } else if (dataStream.size == 50) {
            dataStream.removeLast()
        }
    }
    var isComma = ' '
    var x1 = ' '
    var x2 = ' '
    var x3= ' '
    var x4= ' '
    var y1= ' '
    var y2= ' '
    var y3= ' '
    var y4= ' '
*/

   // fun String.addCharAtIndex(char: Char, index: Int) =
      //  StringBuilder(this).apply { insert(index, char) }.toString()

    /*fun checkForDelta(arr: ArrayList<String>){
        var x = ""
        var isComma = ' '
        for(item in arr){
            if (item.contains("gyroscope")){
                val gyroFindX = item.indexOf("X")
                val gyroXStart = gyroFindX + 4
                 while (isComma != ','){
                   x1 = item[gyroXStart]
                    val check = item[gyroXStart + 1]
                    if (item[gyroXStart+1] == ','){
                      isComma = ','
                }
                    else if (item[gyroXStart+2] == ','){
                        x2 = item[gyroXStart + 1]
                        isComma = item[gyroXStart+2]
                }
                   else if (item[gyroXStart+3] == ','){
                      x3 = item[gyroXStart + 2]
                       isComma = item[gyroXStart+3]
                }
                   else if (item[gyroXStart+4] == ','){
                        x4 = item[gyroXStart + 3]
                       isComma = item[gyroXStart+4]
                   }}
                if (x2 != null){
                  x = x.addCharAtIndex(x1, 0)
                  x = x.addCharAtIndex(x2, 1)

                }

                print(x)



            }
            else if (item.contains("gyroscope")){
                val gyroFindY = item.indexOf("Y")
                val gyroYStart = gyroFindY + 4
                while (isComma != ','){
                    val y1 = item[gyroYStart]
                    val check = item[gyroYStart + 1]
                    if (item[gyroYStart+1] == ','){
                        isComma = ','
                    }
                    else if (item[gyroYStart+2] == ','){
                        val y2 = item[gyroYStart] + 1
                        isComma = item[gyroYStart+2]
                    }
                    else if (item[gyroYStart+3] == ','){
                        val y3 = item[gyroYStart] + 2
                        isComma = item[gyroYStart+3]
                    }
                    else if (item[gyroYStart+4] == ','){
                        val y4 = item[gyroYStart] + 3
                        isComma = item[gyroYStart+4]
                    }}



            }
        }
        }
  */

    fun httpReq(){
        val volleyQueue = Volley.newRequestQueue(this)
        val url = "https://bbpoc2-dot-betterbricks.ts.r.appspot.com/algo"
        //val url = "http://10.0.2.2:5000/algo"

        val jsonObjectRequest = JsonObjectRequest(
            // we are using GET HTTP request method
            Request.Method.GET,
            // url we want to send the HTTP request to
            url,
            // this parameter is used to send a JSON object
            // to the server, since this is not required in
            // our case, we are keeping it `null`
            null,

            // lambda function for handling the case
            // when the HTTP request succeeds
            { response ->
                // get the image url from the JSON object

                //val json = response["response"]
                Log.d(TAG, response.toString());
                binding.httpresponse.text = response.toString()



                // load the image into the ImageView using Glide.


            },

            // lambda function for handling the
            // case when the HTTP request fails
            { error ->
                // make a Toast telling the user
                // that something went wrong
                Toast.makeText(this, "An error occured getting brick count from the backend service", Toast.LENGTH_LONG).show()
                // log the error message in the error stream
                Log.e("MainActivity", "Backend Connection Error error: ${error.localizedMessage}")
            }
        )

        // add the json request object created
        // above to the Volley request queue
        volleyQueue.add(jsonObjectRequest)

    }


    @SuppressLint("SetTextI18n")
    override fun onMessageReceived(p0: MessageEvent) {
        if (String(p0.data, StandardCharsets.UTF_8) == "Stopped"){
            binding.deviceconnectionStatusTv.text = "Stopped but still connected"
            binding.messagelogTextView.text = "Data Stopped"
            binding.httpresponse.text = "Bricks"
            httpReq()


        }
        if (String(p0.data, StandardCharsets.UTF_8) == "Started"){
            binding.deviceconnectionStatusTv.text = "Connected"
            binding.messagelogTextView.text = "Data Incoming"

        }
        try {
            val s =
                String(p0.data, StandardCharsets.UTF_8)
            val messageEventPath: String = p0.path
            Log.d(
                TAG_MESSAGE_RECEIVED,
                "onMessageReceived() Received a message from watch:"
                        + p0.requestId
                        + " "
                        + messageEventPath
                        + " "
                        + s
            )

            Log.d("DEBUG",String(p0.data, StandardCharsets.UTF_8))

           // var obj = JSONObject(s)

            val updateMap: MutableMap<String, Any> = HashMap()
            updateMap["s"] = s
            val db= FirebaseFirestore.getInstance()
            //db.collection("sensorData").add(updateMap)
           // dataUpdate.append(s)

        //    createDataStream(s)






            if (messageEventPath == APP_OPEN_WEARABLE_PAYLOAD_PATH) {
                currentAckFromWearForAppOpenCheck = s
                Log.d(
                    TAG_MESSAGE_RECEIVED,
                    "Received acknowledgement message that app is open in wear"
                )

                val sbTemp = StringBuilder()
                sbTemp.append(binding.messagelogTextView.text.toString())
                sbTemp.append("\nWearable device connected.")
                Log.d("receive1", " $sbTemp")
                binding.messagelogTextView.text = sbTemp
                binding.messagelogTextView.visibility = View.VISIBLE

                binding.checkwearablesButton.visibility = View.GONE
                messageEvent = p0
                wearableNodeUri = p0.sourceNodeId
            }


        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("receive1", "Handled")
        }
        }

    override fun onCapabilityChanged(p0: CapabilityInfo) {
    }



    override fun onPause() {
        super.onPause()
        // try {
        // Wearable.getDataClient(activityContext!!).removeListener(this)
        // Wearable.getMessageClient(activityContext!!).removeListener(this)
        // Wearable.getCapabilityClient(activityContext!!).removeListener(this)
        //  } catch (e: Exception) {
        //   e.printStackTrace()
        // }
        // }
    }

    override fun onResume() {
        super.onResume()
        try {
            Wearable.getDataClient(activityContext!!).addListener(this)
            Wearable.getMessageClient(activityContext!!).addListener(this)
            Wearable.getCapabilityClient(activityContext!!)
                .addListener(this, Uri.parse("wear://"), CapabilityClient.FILTER_REACHABLE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun writeFileOnInternalStorage(context: Context, sFileName: String?, sBody: String?) {

        val dir = File(context.filesDir, "/")
        Log.d("DATALOC", dir.absolutePath)
        if (!dir.exists()) {
            dir.mkdir()
        }
        try {
            val gpxfile = sFileName?.let { File(dir, it) }
            val writer = FileWriter(gpxfile)
            writer.append(sBody)
            Log.d("DATALOC", dir.path)
            Log.d("DATANAME", dir.name)
            writer.flush()
            writer.close()

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
      } /*
        try {
            val fileout: FileOutputStream = openFileOutput("mytextfile.txt", MODE_PRIVATE)
            val outputWriter = OutputStreamWriter(fileout)
            outputWriter.append(sBody)
            outputWriter.close()
            //display file saved message
            //Toast.makeText(
              //  baseContext, "File saved successfully!",
              //  Toast.LENGTH_SHORT
          //  ).show()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }*/

    }
}
