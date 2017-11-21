package io.geeteshk.hyper.helper

import android.util.Log
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

object HTMLParser {

    private val TAG = HTMLParser::class.java.simpleName

    private fun getSoup(name: String): Document? = try {
        Jsoup.parse(ProjectManager.getIndexFile(name), "UTF-8")
    } catch (e: IOException) {
        Log.e(TAG, e.toString())
        null
    }

    fun getProperties(projName: String): Array<String?> {
        val soup = getSoup(projName)
        val properties = arrayOfNulls<String>(4)

        soup?.let {
            properties[0] = soup.head().getElementsByTag("title").text()
            val metas = soup.head().getElementsByTag("meta")
            for (meta in metas) {
                val content = meta.attr("content")
                val name = meta.attr("name")

                when (name) {
                    "author" -> properties[1] = content
                    "description" -> properties[2] = content
                    "keywords" -> properties[3] = content
                }
            }
        }

        return properties
    }
}
