package hirosuke.nick

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

object HttpGetter {

    fun getBeforeName(uuid: String): String {

        val request: Request = Request.Builder().url("https://sessionserver.mojang.com/session/minecraft/profile/$uuid").build()
        val client = OkHttpClient.Builder().build()
        val response: Response = client.newCall(request).execute()
        val json = response.body!!.string().split('"')

        return json[7]
    }
}
