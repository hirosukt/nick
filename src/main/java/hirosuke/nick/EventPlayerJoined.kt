package hirosuke.nick

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class EventPlayerJoined : Listener {

    @EventHandler
    fun onPlayerJoined(e: PlayerJoinEvent) {
        Nick.instance.config.set(e.player.uniqueId.toString(), e.player.name)
    }
}