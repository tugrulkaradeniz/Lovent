import com.tkara.lovent.StaticValues
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


class HttpJsonClient(private val baseUrl: String = StaticValues.BASE_URL) {

    private var connectTimeout = StaticValues.CONNECT_TIMEOUT
    private var readTimeout = StaticValues.READ_TIMEOUT

    companion object {
        private const val TAG = "HttpJsonClient"
    }

    // Timeout ayarları
    fun setTimeouts(connectTimeout: Int, readTimeout: Int) {
        this.connectTimeout = connectTimeout
        this.readTimeout = readTimeout
    }

    /**
     * Map'i JSON'a çevirip POST isteği gönderen method
     */
    suspend fun postJson(endpoint: String, data: Map<String, Any?>): String {
        return withContext(Dispatchers.IO) {
            val jsonData = mapToJson(data)
            val fullUrl = StaticValues.getFullUrl(endpoint)
            StaticValues.debugLog(TAG, "POST Request to: $fullUrl")
            StaticValues.debugLog(TAG, "Request Data: $jsonData")

            postJsonString(endpoint, jsonData)
        }
    }

    /**
     * Direkt JSON string gönderen method
     */
    suspend fun postJsonString(endpoint: String, jsonData: String): String {
        return withContext(Dispatchers.IO) {
            val fullUrl = StaticValues.getFullUrl(endpoint)
            val url = URL(fullUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                connectTimeout = this@HttpJsonClient.connectTimeout
                readTimeout = this@HttpJsonClient.readTimeout
            }

            try {
                // JSON data gönder
                connection.outputStream.use { output ->
                    OutputStreamWriter(output, StandardCharsets.UTF_8).use { writer ->
                        writer.write(jsonData)
                        writer.flush()
                    }
                }

                // Response al
                when (connection.responseCode) {
                    in 200..299 -> readResponse(connection.inputStream)
                    else -> {
                        val errorResponse = readResponse(connection.errorStream)
                        throw RuntimeException("HTTP Error ${connection.responseCode}: $errorResponse")
                    }
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * GET request
     */
    suspend fun getJson(endpoint: String): String {
        return withContext(Dispatchers.IO) {
            val url = URL(baseUrl + endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                connectTimeout = this@HttpJsonClient.connectTimeout
                readTimeout = this@HttpJsonClient.readTimeout
            }

            try {
                when (connection.responseCode) {
                    in 200..299 -> readResponse(connection.inputStream)
                    else -> {
                        val errorResponse = readResponse(connection.errorStream)
                        throw RuntimeException("HTTP Error ${connection.responseCode}: $errorResponse")
                    }
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * PUT request
     */
    suspend fun putJson(endpoint: String, data: Map<String, Any?>): String {
        return withContext(Dispatchers.IO) {
            val jsonData = mapToJson(data)
            val url = URL(baseUrl + endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                doOutput = true
                connectTimeout = this@HttpJsonClient.connectTimeout
                readTimeout = this@HttpJsonClient.readTimeout
            }

            try {
                connection.outputStream.use { output ->
                    OutputStreamWriter(output, StandardCharsets.UTF_8).use { writer ->
                        writer.write(jsonData)
                        writer.flush()
                    }
                }

                when (connection.responseCode) {
                    in 200..299 -> readResponse(connection.inputStream)
                    else -> {
                        val errorResponse = readResponse(connection.errorStream)
                        throw RuntimeException("HTTP Error ${connection.responseCode}: $errorResponse")
                    }
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * DELETE request
     */
    suspend fun deleteJson(endpoint: String): String {
        return withContext(Dispatchers.IO) {
            val url = URL(baseUrl + endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "DELETE"
                setRequestProperty("Accept", "application/json")
                connectTimeout = this@HttpJsonClient.connectTimeout
                readTimeout = this@HttpJsonClient.readTimeout
            }

            try {
                when (connection.responseCode) {
                    in 200..299 -> readResponse(connection.inputStream)
                    else -> {
                        val errorResponse = readResponse(connection.errorStream)
                        throw RuntimeException("HTTP Error ${connection.responseCode}: $errorResponse")
                    }
                }
            } finally {
                connection.disconnect()
            }
        }
    }

    /**
     * Map'i JSON string'e çeviren method
     * Android'de JSONObject kullanıyoruz - daha güvenli
     */
    private fun mapToJson(map: Map<String, Any?>): String {
        val jsonObject = JSONObject()
        for ((key, value) in map) {
            jsonObject.put(key, value)
        }
        return jsonObject.toString()
    }

    /**
     * JSON response'u Map'e çeviren method
     */
    fun parseJsonResponse(jsonResponse: String): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()

        try {
            val jsonObject = JSONObject(jsonResponse)
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value = jsonObject.get(key)

                // Nested JSON objelerini de parse et
                result[key] = when (value) {
                    is JSONObject -> parseNestedJsonObject(value)
                    else -> value
                }
            }
        } catch (e: Exception) {
            StaticValues.debugLog("HttpJsonClient", "JSON parsing error: ${e.message}")
        }
        return result
    }

    private fun parseNestedJsonObject(jsonObject: JSONObject): Map<String, Any?> {
        val result = mutableMapOf<String, Any?>()
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            result[key] = jsonObject.get(key)
        }
        return result
    }

    /**
     * InputStream'den string okuma helper method
     */
    private fun readResponse(inputStream: java.io.InputStream?): String {
        if (inputStream == null) return ""

        return inputStream.use { stream ->
            BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).use { reader ->
                reader.readText()
            }
        }
    }
}

// Extension function - Kotlin'in güzel özelliklerinden biri
fun Map<String, Any?>.toJsonString(): String {
    val jsonObject = JSONObject()
    for ((key, value) in this) {
        jsonObject.put(key, value)
    }
    return jsonObject.toString()
}

// Kullanım örneği
suspend fun main() {
    val client = HttpJsonClient("http://10.0.2.2/api.php") // Android emülatör için

    try {
        // Map ile kullanım - çok daha temiz!
        val userData = mapOf(
            "name" to "Ahmet Kotlin",
            "email" to "ahmet@kotlin.com",
            "age" to 27,
            "active" to true
        )

        // POST request
        val response = client.postJson("/users", userData)
        println("POST Response: $response")

        // Response'u parse et
        val parsedResponse = client.parseJsonResponse(response)
        println("Parsed: $parsedResponse")

        // GET request
        val getResponse = client.getJson("/users/1")
        println("GET Response: $getResponse")

        // PUT request
        val updateData = mapOf(
            "name" to "Ahmet Güncellendi",
            "age" to 28
        )
        val putResponse = client.putJson("/users/1", updateData)
        println("PUT Response: $putResponse")

        // DELETE request
        val deleteResponse = client.deleteJson("/users/1")
        println("DELETE Response: $deleteResponse")

    } catch (e: Exception) {
        println("Hata: ${e.message}")
        e.printStackTrace()
    }
}

