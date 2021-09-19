package hirosuke.nick

import com.mojang.authlib.GameProfile
import net.minecraft.server.v1_12_R1.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_12_R1.PacketPlayOutNamedEntitySpawn
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer
import org.bukkit.entity.Player
import java.lang.reflect.Field

class CommandNick : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (command.name == "nick") {
                var player = sender

                if (args.isEmpty()) {
                    setNick(player, GameProfileBuilder.fetch(player.uniqueId).name)
                    sender.sendMessage("Removed your nickname.")
                }

                if (args.lastIndex >= 0) {
                    if (args[0].length > 16) {
                        sender.sendMessage("Nickname length is must be under than 16.")
                        return true
                    }

                    var replaced: String = args[0].replace("&", "§") + "§r"
                    if (args.lastIndex >= 1) {
                        var replacedPlayer: Player = Bukkit.getPlayer(args[1])!!
                        setNick(replacedPlayer, replaced)
                    } else {
                        setNick(player, replaced)
                    }
                    sender.sendMessage("Set nickname to $replaced.")
                }
            }
        }
        return true
    }

    fun setNick(player: Player, name: String) {

        player.playerListName = name
        player.displayName = name
        player.customName = player.name
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