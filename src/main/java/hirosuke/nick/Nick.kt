package hirosuke.nick;

import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

class Nick : JavaPlugin() {

    companion object {
        lateinit var instance: Plugin
            private set
    }

    override fun onEnable() {
        getCommand("nick")?.executor = CommandNick()

        server.pluginManager.registerEvents(EventPlayerJoined(), this)
        logger.info("plugin has loaded.")
    }

    override fun onDisable() {
        logger.info("plugin has unloaded.")
    }
}
