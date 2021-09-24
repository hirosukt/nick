package hirosuke.nick

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.reflect.Constructor
import java.lang.reflect.Field

open class CommandNick : CommandExecutor {

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

                        player.setPlayerListName(replaced)
                        player.setDisplayName(replaced)
                        player.customName = replaced
                        player.isCustomNameVisible = true
                        setNick(player, replaced)
                        sender.sendMessage("Set your nickname to §l$replaced.")
                    }
                }
            }
        }
        return true
    }

    private fun getNMSClass(name: String): Class<*> {
        Nick.instance.logger.info(Bukkit.getServer().javaClass.getPackage().name.split(".").toTypedArray().size.toString())
        Nick.instance.logger.info(Bukkit.getServer().javaClass.getPackage().name.split(".").toTypedArray()[0])
        Nick.instance.logger.info(Bukkit.getServer().javaClass.getPackage().name.split(".").toTypedArray().toString())

        return Class.forName("net.minecraft.server." + Bukkit.getServer().javaClass.getPackage().name.split(".").toTypedArray()[3] + ".$name")
    }

    private fun getBukkitNMSClass(name: String): Class<*> {
        return Class.forName("org.bukkit.craftbukkit." + Bukkit.getServer().javaClass.getPackage().name.split(".").toTypedArray()[3] + ".$name")
    }

    private fun sendPacket(player: Any, packet: Any?) {
        try {
            val handle = player.javaClass.getMethod("getHandle")
            val playerConnection = handle.javaClass.getField("playerConnection")[handle]
            playerConnection.javaClass.getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setNick(player: Player, name: String) {
        for (ps in Bukkit.getOnlinePlayers()) {
            if(ps == player) continue

//            var array: Any = java.lang.reflect.Array.newInstance(getNMSClass("EntityPlayer"), 1)
            var craftBukkitPlayer = getBukkitNMSClass("entity.CraftPlayer")
            var enumAddPlayer = getNMSClass("PacketPlayOutPlayerInfo.EnumPlayerInfoAction").enumConstants[0]
            var enumRemovePlayer = getNMSClass("PacketPlayOutPlayerInfo").getField("REMOVE_PLAYER").get(null)
            var packetPlayOutPlayerInfoConstructor: Constructor<*> = getNMSClass("PacketPlayOutPlayerInfo").getConstructor(getNMSClass("PacketPlayOutPlayerInfo"))
            var packetPlayOutEntityDestroyConstructor: Constructor<*> = getNMSClass("PacketPlayOutEntityDestroy").getConstructor(getNMSClass("PacketPlayOutEntityDestroy"))
            var packetPlayOutNamedEntitySpawn: Constructor<*> = getNMSClass("PacketPlayOutNamedEntitySpawn").getConstructor(getNMSClass("PacketPlayOutNamedEntitySpawn"))
            val handlePlayer = player.javaClass.getMethod("getHandle").invoke(player)
            val handlePs = ps.javaClass.getMethod("getHandle").invoke(ps)

            sendPacket(craftBukkitPlayer.cast(ps), packetPlayOutPlayerInfoConstructor.newInstance(enumRemovePlayer, handlePlayer))

            var nameField: Field = getNMSClass("GameProfile")::class.java.getDeclaredField("name")
            nameField.isAccessible = true

            nameField.set(handlePlayer.javaClass.getMethod("getProfile").invoke(handlePlayer), name)

            sendPacket(handlePs, packetPlayOutPlayerInfoConstructor.newInstance(enumAddPlayer, handlePlayer))
            sendPacket(handlePs, packetPlayOutEntityDestroyConstructor.newInstance(player.entityId))
            sendPacket(handlePs, packetPlayOutNamedEntitySpawn.newInstance((handlePlayer)))
        }
    }
}