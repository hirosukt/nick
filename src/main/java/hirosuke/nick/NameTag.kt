package hirosuke.nick

import org.bukkit.entity.Player
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.wrappers.WrappedGameProfile
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import com.comphenix.protocol.wrappers.PlayerInfoData
import com.comphenix.protocol.PacketType
import com.google.common.base.Preconditions
import org.bukkit.Bukkit
import com.google.common.base.Predicates
import java.lang.reflect.InvocationTargetException

interface NameTag {
    val text: String?
    fun applyTo(player: Player) {
        val protocolManager = ProtocolLibrary.getProtocolManager()
        val id = player.entityId
        val ping = 0
        val location = player.location
        val wrappedGameProfile = WrappedGameProfile.fromPlayer(player)
        val nativeGameMode = NativeGameMode.fromBukkit(player.gameMode)
        val tabName = WrappedChatComponent.fromText(player.playerListName)
        val dataWatcher = WrappedDataWatcher.getEntityWatcher(player)
        val wrappedSignedProperty =
            PlayerInfoData(wrappedGameProfile, ping, nativeGameMode, tabName).profile.properties["textures"].iterator()
                .next()
        val removePlayer = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO)
        val addPlayer = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO)
        val destroyEntity = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY)
        val namedEntitySpawn = protocolManager.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN)
        removePlayer.playerInfoAction.write(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER)
        //        removePlayer.getPlayerInfoDataLists().write(0, NmsUtil.getPlayerInfoDataList(player));
        addPlayer.playerInfoAction.write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER)
        val playerInfoData = PlayerInfoData(wrappedGameProfile.withName(text), ping, nativeGameMode, tabName)
        playerInfoData.profile.properties.clear()
        playerInfoData.profile.properties["textures"].add(wrappedSignedProperty)
        addPlayer.playerInfoDataLists.write(0, listOf(playerInfoData))
        destroyEntity.integerArrays.write(0, intArrayOf(id))
        namedEntitySpawn.integers.write(0, id)
        namedEntitySpawn.uuiDs.write(0, player.uniqueId)
        namedEntitySpawn.doubles.write(0, location.x)
        namedEntitySpawn.doubles.write(1, location.y)
        namedEntitySpawn.doubles.write(2, location.z)
        namedEntitySpawn.bytes.write(0, (location.yaw * 256.0f / 360.0f).toInt().toByte())
        namedEntitySpawn.bytes.write(1, (location.pitch * 256.0f / 360.0f).toInt().toByte())
        namedEntitySpawn.dataWatcherModifier.write(0, dataWatcher)
        protocolManager.broadcastServerPacket(removePlayer)
        protocolManager.broadcastServerPacket(addPlayer)
        Bukkit.getOnlinePlayers().stream().filter(Predicates.not { obj: Any? -> player.equals(obj) })
            .forEach { o: Player? ->
                try {
                    protocolManager.sendServerPacket(o, destroyEntity)
                    protocolManager.sendServerPacket(o, namedEntitySpawn)
                } catch (exception: InvocationTargetException) {
                    exception.printStackTrace()
                }
            }
    }

    companion object {
        fun of(text: String): SimpleNameTag {
            Preconditions.checkArgument(text.length < 16, "Name length must not exceed 16 characters.")
            return SimpleNameTag(text)
        }
    }
}