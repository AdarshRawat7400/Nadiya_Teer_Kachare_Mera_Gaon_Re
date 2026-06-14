import java.net.HttpURLConnection
import java.net.URL
import java.io.InputStreamReader
import java.io.BufferedReader

fun main() {
    val urls = listOf(
        "https://drive.google.com/uc?export=download&id=1KBJHD7SvqU9Kpg-WfyKdpvnCDm227sYq",
        "https://drive.google.com/uc?export=download&id=1KBJHD7SvqU9Kpg-WfyKdpvnCDm227sYq&confirm=t"
    )
    
    for (urlString in urls) {
        try {
            println("Testing URL: $urlString")
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.instanceFollowRedirects = true
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
            
            val responseCode = connection.responseCode
            println("Response Code: $responseCode")
            val contentType = connection.contentType
            println("Content-Type: $contentType")
            
            // Read first few lines of response if it's not a video
            if (contentType != null && contentType.startsWith("text/html")) {
                val errorStream = connection.inputStream ?: connection.errorStream
                if (errorStream != null) {
                    val reader = BufferedReader(InputStreamReader(errorStream))
                    var line: String? = null
                    var i = 0
                    while (reader.readLine().also { line = it } != null && i < 10) {
                        println(line)
                        i++
                    }
                    reader.close()
                }
            } else {
                println("Seems like a valid stream! Content type: $contentType")
            }
            println("--------------------------")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
