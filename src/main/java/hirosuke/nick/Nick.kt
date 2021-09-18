package hirosuke.nick;

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException





class Nick : JavaPlugin() {

    companion object {
        lateinit var instance: Plugin
            private set
    }

    override fun onEnable() {
        instance = this

        val customYml: File = File(instance.dataFolder.toString() + "/db.yml")
        val db: FileConfiguration = YamlConfiguration.loadConfiguration(customYml)

        saveConfig()

        getCommand("nick")?.executor = CommandNick()
        logger.info("plugin has loaded.")
    }

    override fun onDisable() {
        logger.info("plugin has unloaded.")
    }

    fun saveDB(ymlConfig: FileConfiguration, ymlFile: File?) {
        try {
            ymlConfig.save(ymlFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
