package hirosuke.nick

import com.mojang.authlib.GameProfile
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.lang.reflect.Constructor
import java.lang.reflect.Field

open class CommandNick : CommandExecutor {

    var beforeNames: HashMap<String, String> = HashMap()

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (command.name == "nick") {
                var player = sender

                if (args.isEmpty()) {
                    beforeNames[player.uniqueId.toString()]?.let { setNick(player, it) }
                    sender.sendMessage("Removed your nickname.")
                }

                if (args.lastIndex >= 0) {
                    if (args[0].length > 16) {
                        sender.sendMessage("Nickname length is must be under than 16.")
                        return true
                    }

                    var replaced: String = args[0].replace("&", "§") + "§r"
                    beforeNames.putIfAbsent(player.uniqueId.toString(), player.name)
                    if (args.lastIndex >= 1) {
                        var replacedPlayer: Player = Bukkit.getPlayer(args[1])!!
                        var beforeName = beforeNames[player.uniqueId.toString()]
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

    private fun getNMSClass(name: String): Class<*> {
        return Class.forName("net.minecraft.server." + getPackageVersion() + ".$name")
    }

    private fun getBukkitNMSClass(name: String): Class<*> {
        return Class.forName("org.bukkit.craftbukkit." + getPackageVersion() + ".$name")
    }

    private fun getPackageVersion(): String {
        return Bukkit.getServer().javaClass.getPackage().name.split(".").toTypedArray()[3]
    }

    private fun sendPacket(playerTo: Any, packet: Any?) {
        try {
            var craftBukkitPlayer = getBukkitNMSClass("entity.CraftPlayer")
            val handle = craftBukkitPlayer.cast(playerTo).javaClass.getMethod("getHandle").invoke(playerTo)
            val playerConnection = handle.javaClass.getField("playerConnection")[handle]
            playerConnection.javaClass.getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setNick(player: Player, name: String) {
        for (ps in Bukkit.getOnlinePlayers()) {
            if(ps == player) continue
            player.setPlayerListName(name)
            player.setDisplayName(name)
            player.customName = name
            player.isCustomNameVisible = true

            var conArray: Class<*> = java.lang.reflect.Array.newInstance(getNMSClass("EntityPlayer"), 0).javaClass
            var craftBukkitPlayer = getBukkitNMSClass("entity.CraftPlayer")
            var packetPlayOutPlayerInfoConstructor: Constructor<*> = if(getPackageVersion() != "v1_8_R1") getNMSClass("PacketPlayOutPlayerInfo").getConstructor(getNMSClass("PacketPlayOutPlayerInfo").declaredClasses[1], conArray) else getNMSClass("PacketPlayOutPlayerInfo").getConstructor(getNMSClass("EnumPlayerInfoAction"), conArray)
            var enumAddPlayer = if(getPackageVersion() != "v1_8_R1") getNMSClass("PacketPlayOutPlayerInfo").declaredClasses[1].getField("ADD_PLAYER").get(null) else getNMSClass("EnumPlayerInfoAction").getField("ADD_PLAYER").get(null)
            var enumRemovePlayer = if(getPackageVersion() != "v1_8_R1") getNMSClass("PacketPlayOutPlayerInfo").declaredClasses[1].getField("REMOVE_PLAYER").get(null) else getNMSClass("EnumPlayerInfoAction").getField("REMOVE_PLAYER").get(null)
            var packetPlayOutEntityDestroyConstructor: Constructor<*> = getNMSClass("PacketPlayOutEntityDestroy").getConstructor(IntArray(0).javaClass)
            var packetPlayOutNamedEntitySpawnConstructor: Constructor<*> = getNMSClass("PacketPlayOutNamedEntitySpawn").getConstructor(getNMSClass("EntityHuman"))
            val handlePlayer = craftBukkitPlayer.getMethod("getHandle").invoke(player)
            val playerProfile = craftBukkitPlayer.getDeclaredMethod("getProfile").invoke(craftBukkitPlayer.cast(player))

            val array = java.lang.reflect.Array.newInstance(getNMSClass("EntityPlayer"), 1)
            val entityPlayer: Any = player.javaClass.getMethod("getHandle").invoke(player)
            java.lang.reflect.Array.set(array, 0, entityPlayer)

            sendPacket(ps, packetPlayOutPlayerInfoConstructor.newInstance(enumRemovePlayer, array))

            var nameField: Field = GameProfile::class.java.getDeclaredField("name")
            nameField.isAccessible = true
            nameField.set(playerProfile, name)

            sendPacket(ps, packetPlayOutPlayerInfoConstructor.newInstance(enumAddPlayer, array))
            sendPacket(ps, packetPlayOutEntityDestroyConstructor.newInstance(intArrayOf(player.entityId)))
            sendPacket(ps, packetPlayOutNamedEntitySpawnConstructor.newInstance(handlePlayer))

            player.spigot().respawn()
        }
    }
}