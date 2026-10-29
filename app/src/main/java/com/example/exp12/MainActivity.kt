package com.example.exp12

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var mainScrollView: NestedScrollView
    private lateinit var etId: EditText
    private lateinit var etUrl: EditText
    private lateinit var btnFetch: Button
    private lateinit var btnSend: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResponse: TextView
    private lateinit var cardResponse: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        mainScrollView = findViewById(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainScrollView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etId = findViewById(R.id.etId)
        etUrl = findViewById(R.id.etUrl)
        btnFetch = findViewById(R.id.btnFetch)
        btnSend = findViewById(R.id.btnSend)
        progressBar = findViewById(R.id.progressBar)
        tvResponse = findViewById(R.id.tvResponse)
        cardResponse = findViewById(R.id.cardResponse)

        btnFetch.setOnClickListener {
            hideKeyboard()
            val idText = etId.text.toString().trim()
            val baseUrl = etUrl.text.toString().trim()

            if (baseUrl.isEmpty()) {
                Toast.makeText(this, "Please enter a valid base URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fullUrl = if (idText.isNotEmpty() && !baseUrl.endsWith("/$idText")) {
                if (baseUrl.endsWith("/")) "$baseUrl$idText" else "$baseUrl/$idText"
            } else {
                baseUrl
            }

            executeHttpGetRequest(fullUrl, idText)
        }

        btnSend.setOnClickListener {
            hideKeyboard()
            val idText = etId.text.toString().trim()
            var baseUrl = etUrl.text.toString().trim()

            if (baseUrl.isEmpty()) {
                Toast.makeText(this, "Please enter a valid base URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Standardize POST URL to collection endpoint
            if (baseUrl.endsWith("/1") || baseUrl.endsWith("/$idText")) {
                baseUrl = baseUrl.substringBeforeLast("/") + "/"
                etUrl.setText(baseUrl)
            }

            val idNum = idText.toIntOrNull() ?: 1
            val postJsonPayload = JSONObject().apply {
                put("title", "Android HTTP Web Service Post #$idNum")
                put("body", "HTTP POST request sent successfully for ID $idNum.")
                put("userId", idNum)
            }.toString()

            executeHttpPostRequest(baseUrl, postJsonPayload, idNum)
        }
    }

    private fun executeHttpGetRequest(urlString: String, idText: String) {
        showLoadingState("Fetching data for ID: $idText...\n$urlString")

        lifecycleScope.launch(Dispatchers.IO) {
            val result = performHttpGetCall(urlString, idText)
            withContext(Dispatchers.Main) {
                hideLoadingState(result)
            }
        }
    }

    private fun executeHttpPostRequest(urlString: String, jsonPayload: String, idNum: Int) {
        showLoadingState("Sending POST data for ID: $idNum...\n$urlString")

        lifecycleScope.launch(Dispatchers.IO) {
            val result = performHttpPostCall(urlString, jsonPayload, idNum)
            withContext(Dispatchers.Main) {
                hideLoadingState(result)
            }
        }
    }

    private fun performHttpGetCall(urlString: String, idText: String): String {
        var connection: HttpURLConnection? = null
        val responseBuilder = StringBuilder()

        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "Android-WebServices-Client")

            val statusCode = connection.responseCode
            val responseMsg = connection.responseMessage ?: ""

            val inputStream: InputStream? = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = inputStream?.use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } ?: ""

            responseBuilder.append("===================================\n")
            responseBuilder.append("STATUS: ").append(statusCode).append(" ").append(responseMsg.ifEmpty { "OK" }).append("\n")
            responseBuilder.append("===================================\n")
            responseBuilder.append("Action: Fetch Data (HTTP GET)\n")
            responseBuilder.append("ID Queried: ").append(idText.ifEmpty { "N/A" }).append("\n")
            responseBuilder.append("URL: ").append(urlString).append("\n\n")

            if (statusCode in 200..299 && responseBody.isNotEmpty()) {
                val parsedDetails = parseAndFormatJson(responseBody)
                if (parsedDetails.isNotEmpty()) {
                    responseBuilder.append("[PARSED RESPONSE FIELDS]\n")
                    responseBuilder.append(parsedDetails).append("\n\n")
                }
            }

            responseBuilder.append("[RAW RESPONSE JSON]\n")
            responseBuilder.append(prettyPrintJson(responseBody))

            responseBuilder.toString()
        } catch (e: Exception) {
            buildErrorResponse("GET", urlString, e)
        } finally {
            connection?.disconnect()
        }
    }

    private fun performHttpPostCall(urlString: String, jsonPayload: String, idNum: Int): String {
        var connection: HttpURLConnection? = null
        val responseBuilder = StringBuilder()

        return try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.setRequestProperty("Accept", "application/json")

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(jsonPayload)
                writer.flush()
            }

            val statusCode = connection.responseCode
            val responseMsg = connection.responseMessage ?: ""

            val inputStream: InputStream? = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = inputStream?.use { stream ->
                BufferedReader(InputStreamReader(stream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } ?: ""

            responseBuilder.append("===================================\n")
            responseBuilder.append("STATUS: ").append(statusCode).append(" ").append(responseMsg.ifEmpty { "Created" }).append("\n")
            responseBuilder.append("===================================\n")
            responseBuilder.append("Action: Send Data (HTTP POST)\n")
            responseBuilder.append("ID Associated: ").append(idNum).append("\n")
            responseBuilder.append("URL: ").append(urlString).append("\n\n")

            responseBuilder.append("[SENT JSON PAYLOAD]\n")
            responseBuilder.append(prettyPrintJson(jsonPayload)).append("\n\n")

            responseBuilder.append("[SERVER RESPONSE JSON]\n")
            responseBuilder.append(prettyPrintJson(responseBody))

            responseBuilder.toString()
        } catch (e: Exception) {
            buildErrorResponse("POST", urlString, e)
        } finally {
            connection?.disconnect()
        }
    }

    private fun parseAndFormatJson(jsonString: String): String {
        return try {
            val trimmed = jsonString.trim()
            val sb = StringBuilder()
            if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    sb.append("• ").append(key.uppercase()).append(": ").append(obj.opt(key)).append("\n")
                }
            } else if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                sb.append("Array containing ").append(array.length()).append(" records.\n")
                if (array.length() > 0 && array[0] is JSONObject) {
                    val first = array.getJSONObject(0)
                    val keys = first.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        sb.append("  - ").append(key.uppercase()).append(": ").append(first.opt(key)).append("\n")
                    }
                }
            }
            sb.toString().trim()
        } catch (_: Exception) {
            ""
        }
    }

    private fun prettyPrintJson(rawJson: String): String {
        val trimmed = rawJson.trim()
        return try {
            if (trimmed.startsWith("{")) {
                JSONObject(trimmed).toString(2)
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed).toString(2)
            } else {
                rawJson
            }
        } catch (_: Exception) {
            rawJson
        }
    }

    private fun buildErrorResponse(method: String, urlString: String, e: Exception): String {
        return StringBuilder().apply {
            append("===================================\n")
            append("STATUS: REQUEST FAILED\n")
            append("===================================\n")
            append("Method: ").append(method).append("\n")
            append("URL: ").append(urlString).append("\n")
            append("Error: ").append(e.javaClass.simpleName).append("\n")
            append("Details: ").append(e.localizedMessage ?: e.toString()).append("\n\n")
            append("Please check device internet connection and verify the endpoint URL.")
        }.toString()
    }

    private fun showLoadingState(message: String) {
        progressBar.visibility = View.VISIBLE
        btnFetch.isEnabled = false
        btnSend.isEnabled = false
        tvResponse.text = message

        mainScrollView.post {
            mainScrollView.smoothScrollTo(0, cardResponse.top)
        }
    }

    private fun hideLoadingState(result: String) {
        progressBar.visibility = View.GONE
        btnFetch.isEnabled = true
        btnSend.isEnabled = true
        tvResponse.text = result

        mainScrollView.post {
            mainScrollView.smoothScrollTo(0, cardResponse.top)
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus ?: View(this)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}