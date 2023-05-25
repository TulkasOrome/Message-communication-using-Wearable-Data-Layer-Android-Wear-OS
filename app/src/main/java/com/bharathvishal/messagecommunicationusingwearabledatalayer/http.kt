package com.bharathvishal.messagecommunicationusingwearabledatalayer

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL

class HttpBB {
    fun sendGetRequest(userName:String, password:String) {

        val url = URL("https://bbpoc2-dot-betterbricks.ts.r.appspot.com/algo")
        val connection = url.openConnection()
        BufferedReader(InputStreamReader(connection.getInputStream())).use { inp ->
            var line: String?
            while (inp.readLine().also { line = it } != null) {
                println(line)
            }
        }
    }
}