package hirosuke.nick

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.lang.reflect.Field

class EventJoin : Listener {

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
//        var ps = e.player
//
//        for (player in Bukkit.getOnlinePlayers()) {
//            if(player == ps) continue
//
//            (ps as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, (player as CraftPlayer).handle))
//
//            var nameField: Field = GameProfile::class.java.getDeclaredField("name")
//            nameField.isAccessible = true
//
//            nameField.set(player.profile, player.customName)
//
//            ps.handle.playerConnection.sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player.handle))
//            player.handle.playerConnection.sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, ps.handle))
//            ps.handle.playerConnection.sendPacket(PacketPlayOutEntityDestroy(player.getEntityId()))
//            player.handle.playerConnection.sendPacket(PacketPlayOutEntityDestroy(ps.getEntityId()))
//            ps.handle.playerConnection.sendPacket(PacketPlayOutNamedEntitySpawn((player.handle)))
//            player.handle.playerConnection.sendPacket(PacketPlayOutNamedEntitySpawn((ps.handle)))
//        }
    }
}