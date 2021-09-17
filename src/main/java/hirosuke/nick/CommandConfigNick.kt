package hirosuke.nick

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class CommandConfigNick : CommandExecutor, JavaPlugin() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (command.name == "rlnick") {
            reloadConfig()
            sender.sendMessage("Nick config reloaded.")
            return true
        }
        return true
    }
}