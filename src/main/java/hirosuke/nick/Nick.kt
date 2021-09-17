package hirosuke.nick;

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import kotlin.jvm.JvmField as JvmField

class Nick : JavaPlugin() {

    override fun onEnable() {
        getCommand("nick").executor = CommandNick()
        getCommand("rlnick").executor = CommandNick()
        logger.info("plugin has loaded.")
    }

    override fun onDisable() {
        logger.info("plugin has unloaded.")
    }
}
