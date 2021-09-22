package hirosuke.nick

import com.google.gson.*
import java.util.UUID
import com.mojang.util.UUIDTypeAdapter
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import java.util.HashMap
import org.bukkit.craftbukkit.libs.org.apache.commons.io.IOUtils
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import kotlin.jvm.JvmOverloads
import kotlin.Throws
import java.io.IOException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.ArrayList

object GameProfileBuilder {
    private const val SERVICE_URL = "https://sessionserver.mojang.com/session/minecraft/profile/%s?unsigned=false"
    private const val JSON_SKIN =
        "{\"timestamp\":%d,\"profileId\":\"%s\",\"profileName\":\"%s\",\"isPublic\":true,\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}"
    private const val JSON_CAPE =
        "{\"timestamp\":%d,\"profileId\":\"%s\",\"profileName\":\"%s\",\"isPublic\":true,\"textures\":{\"SKIN\":{\"url\":\"%s\"},\"CAPE\":{\"url\":\"%s\"}}}"
    private val gson = GsonBuilder().disableHtmlEscaping().registerTypeAdapter(
        UUID::class.java, UUIDTypeAdapter()
    ).registerTypeAdapter(GameProfile::class.java, GameProfileSerializer()).registerTypeAdapter(
        PropertyMap::class.java, PropertyMap.Serializer()
    ).create()
    private val cache = HashMap<UUID, CachedProfile>()
    private var cacheTime: Long = -1
    /**
     * Don't run in main thread!
     *
     * Fetches the GameProfile from the Mojang servers
     * @param uuid The player uuid
     * @param forceNew If true the cache is ignored
     * @return The GameProfile
     * @throws IOException If something wents wrong while fetching
     * @see GameProfile
     */
    /**
     * Don't run in main thread!
     *
     * Fetches the GameProfile from the Mojang servers
     *
     * @param uuid The player uuid
     * @return The GameProfile
     * @throws IOException If something wents wrong while fetching
     * @see GameProfile
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun fetch(uuid: UUID, forceNew: Boolean = false): GameProfile {
        return if (!forceNew && cache.containsKey(uuid) && cache[uuid]!!.isValid) {
            cache[uuid]!!.profile
        } else {
            val connection =
                URL(String.format(SERVICE_URL, UUIDTypeAdapter.fromUUID(uuid)))
                    .openConnection() as HttpURLConnection
            connection.readTimeout = 5000
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val json =
                    IOUtils.toString(connection.inputStream, StandardCharsets.UTF_8)
                val result = gson.fromJson(json, GameProfile::class.java)
                cache[uuid] = CachedProfile(result)
                result
            } else {
                if (!forceNew && cache.containsKey(uuid)) {
                    return cache[uuid]!!.profile
                }
                val error = JsonParser()
                    .parse(BufferedReader(InputStreamReader(connection.errorStream)).readLine()) as JsonObject
                throw IOException(error["error"].asString + ": " + error["errorMessage"].asString)
            }
        }
    }

    /**
     * Builds a GameProfile for the specified args
     *
     * @param uuid The uuid
     * @param name The name
     * @param skin The url from the skin image
     * @return A GameProfile built from the arguments
     * @see GameProfile
     */
    fun getProfile(uuid: UUID?, name: String?, skin: String?): GameProfile {
        return getProfile(uuid, name, skin, null)
    }

    /**
     * Builds a GameProfile for the specified args
     *
     * @param uuid The uuid
     * @param name The name
     * @param skinUrl Url from the skin image
     * @param capeUrl Url from the cape image
     * @return A GameProfile built from the arguments
     * @see GameProfile
     */
    fun getProfile(uuid: UUID?, name: String?, skinUrl: String?, capeUrl: String?): GameProfile {
        val profile = GameProfile(uuid, name)
        val cape = capeUrl != null && !capeUrl.isEmpty()
        val args: MutableList<Any?> = ArrayList()
        args.add(System.currentTimeMillis())
        args.add(UUIDTypeAdapter.fromUUID(uuid))
        args.add(name)
        args.add(skinUrl)
        if (cape) args.add(capeUrl)
        profile.properties.put(
            "textures",
            Property(
                "textures",
                Base64Coder.encodeString(String.format(if (cape) JSON_CAPE else JSON_SKIN, *args.toTypedArray()))
            )
        )
        return profile
    }

    /**
     * Sets the time as long as you want to keep the gameprofiles in cache (-1 = never remove it)
     * @param time cache time (default = -1)
     */
    fun setCacheTime(time: Long) {
        cacheTime = time
    }

    private class GameProfileSerializer : JsonSerializer<GameProfile>, JsonDeserializer<GameProfile> {
        @Throws(JsonParseException::class)
        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): GameProfile {
            val `object` = json as JsonObject
            val id =
                if (`object`.has("id")) context.deserialize<Any>(`object`["id"], UUID::class.java) as UUID else null
            val name = if (`object`.has("name")) `object`.getAsJsonPrimitive("name").asString else null
            val profile = GameProfile(id, name)
            if (`object`.has("properties")) {
                for ((key, value) in (context.deserialize<Any>(
                    `object`["properties"],
                    PropertyMap::class.java
                ) as PropertyMap).entries()) {
                    profile.properties.put(key, value)
                }
            }
            return profile
        }

        override fun serialize(profile: GameProfile, type: Type, context: JsonSerializationContext): JsonElement {
            val result = JsonObject()
            if (profile.id != null) result.add("id", context.serialize(profile.id))
            if (profile.name != null) result.addProperty("name", profile.name)
            if (!profile.properties.isEmpty) result.add("properties", context.serialize(profile.properties))
            return result
        }
    }

    private class CachedProfile(val profile: GameProfile) {
        private val timestamp = System.currentTimeMillis()
        val isValid: Boolean
            get() = if (cacheTime < 0) true else System.currentTimeMillis() - timestamp < cacheTime
    }
}