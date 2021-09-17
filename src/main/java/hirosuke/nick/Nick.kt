package hirosuke.nick;

import org.bukkit.plugin.java.JavaPlugin


class Nick : JavaPlugin() {

    override fun onEnable() {
        saveDefaultConfig()

        getCommand("nick").executor = CommandNick()
        logger.info("plugin has loaded.")
    }

    override fun onDisable() {
        logger.info("plugin has unloaded.")
    }
}
