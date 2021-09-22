package hirosuke.nick

import com.mojang.authlib.GameProfile
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.reflect.Field
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy
import net.minecraft.server.v1_16_R3.PacketPlayOutNamedEntitySpawn
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer

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
                        var beforeName = GameProfileBuilder.fetch(replacedPlayer.uniqueId).name
                        setNick(replacedPlayer, replaced)
                        sender.sendMessage("Set ${beforeName}'s nickname to §l$replaced.")
                    } else {
                        setNick(player, replaced)
                        sender.sendMessage("Set your nickname to §l$replaced.")
                    }
                }
            }
        }
        return true
    }

    fun setNick(player: Player, name: String) {

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