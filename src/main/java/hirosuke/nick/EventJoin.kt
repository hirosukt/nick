package hirosuke.nick

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class EventJoin : Listener {

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        var player = Bukkit.getPlayer(e.player.uniqueId)!!

        var nickSection = Nick.instance.config.getConfigurationSection("nicknames") != null
        val nickname = if(nickSection) Nick.instance.config.getConfigurationSection("nicknames")!!.getString(player.uniqueId.toString())!! else player.name
        CommandNick().setNick(player, nickname)
        CommandNick().applyOtherNickToPlayer(player)
    }


}