package hirosuke.nick;

import org.bukkit.plugin.java.JavaPlugin


class Nick : JavaPlugin() {

    override fun onEnable() {
        config.addDefault("name_limit", 15)
        config.options().copyDefaults(true)
        saveConfig()

        getCommand("nick").executor = CommandNick()
        getCommand("rlnick").executor = CommandNick()
        logger.info("plugin has loaded.")
    }

    override fun onDisable() {
        logger.info("plugin has unloaded.")
    }
}
