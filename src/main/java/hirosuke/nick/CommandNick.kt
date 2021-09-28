package hirosuke.nick

import com.mojang.authlib.GameProfile
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.lang.reflect.Constructor
import java.lang.reflect.Field

open class CommandNick : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (command.name == "nick") {

                if (args.isEmpty()) {
                    getBeforeName(sender.uniqueId.toString())?.let { setNick(sender, it) }
                    sender.sendMessage("Removed your nickname.")
                }

                if (args.lastIndex >= 0) {
                    if (args[0].length > 16) {
                        sender.sendMessage("Nickname length is must be under than 16.")
                        return true
                    }

                    var replaced: String = args[0].replace("&", "§")
                    if (args.lastIndex >= 1) {
                        var replacedPlayer: Player = Bukkit.getPlayer(args[1])!!
                        setNick(replacedPlayer, replaced)
                        sender.sendMessage("Set ${getBeforeName(replacedPlayer.uniqueId.toString())}'s nickname to §l$replaced.")
                    } else {
                        setNick(sender, replaced)
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

    private fun getBeforeName(uuid: String): String {
        return HttpGetter.getBeforeName(uuid)
    }

    fun setNick(player: Player, name: String) {
        var player = Bukkit.getPlayer(player.uniqueId)!!
        player.setPlayerListName(name)
        player.setDisplayName(name)
        player.customName = name
        player.isCustomNameVisible = true
        Nick.instance.config.set("nicknames." + player.uniqueId.toString(), name)
        Nick.instance.saveConfig()

        for (ps in Bukkit.getOnlinePlayers()) {
            if(ps == player) continue

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
        }
    }

    fun applyOtherNickToPlayer(ps: Player) {

        for (player in Bukkit.getOnlinePlayers()) {
            if(ps == player) continue

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
            val nickname = if((Nick.instance.config.get("nicknames") as ConfigurationSection).contains(player.uniqueId.toString())) (Nick.instance.config.get("nicknames") as ConfigurationSection).getString(player.uniqueId.toString())!! else player.name
            java.lang.reflect.Array.set(array, 0, entityPlayer)

            sendPacket(ps, packetPlayOutPlayerInfoConstructor.newInstance(enumRemovePlayer, array))

            var nameField: Field = GameProfile::class.java.getDeclaredField("name")
            nameField.isAccessible = true
            nameField.set(playerProfile, nickname)

            sendPacket(ps, packetPlayOutPlayerInfoConstructor.newInstance(enumAddPlayer, array))
            sendPacket(ps, packetPlayOutEntityDestroyConstructor.newInstance(intArrayOf(player.entityId)))
            sendPacket(ps, packetPlayOutNamedEntitySpawnConstructor.newInstance(handlePlayer))
        }
    }
}