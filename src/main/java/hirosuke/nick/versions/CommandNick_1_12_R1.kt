package hirosuke.nick.versions

import com.mojang.authlib.GameProfile
import hirosuke.nick.CommandNick
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.lang.reflect.Field

class CommandNick_1_12_R1 : CommandNick {

    override fun setNick(player: Player, name: String) {

        player.setPlayerListName(name)
        player.setDisplayName(name)
        player.customName = name
        player.isCustomNameVisible = true

        for (ps in Bukkit.getOnlinePlayers()) {
            if(ps == player) continue

            (ps as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, (player as CraftPlayer).handle))

            var nameField: Field = GameProfile::class.java.getDeclaredField("name")
            nameField.isAccessible = true

            nameField.set(player.profile, name)

            ps.handle.playerConnection.sendPacket(PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, player.handle))
            ps.handle.playerConnection.sendPacket(PacketPlayOutEntityDestroy(player.getEntityId()))
            ps.handle.playerConnection.sendPacket(PacketPlayOutNamedEntitySpawn((player.handle)))
        }
    }
}