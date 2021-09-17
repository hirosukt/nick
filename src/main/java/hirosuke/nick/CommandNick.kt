package hirosuke.nick

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class CommandNick : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Player) {
            if (command.name == "nick") {

                var target = sender.player

                if (args.size >= 2) {
                    target = Bukkit.getPlayer(args[1])
                }

                if (args.isEmpty()) {
                    setNick(target, target.name)
                    sender.sendMessage("Nickname reset.")
                } else {
                    if (args[0].length <= Nick.instance.config.getInt("name_limit", 251)) {
                        setNick(target, args[0])
                        sender.sendMessage("Nickname set to §l" + args[0] + "§r.")
                    } else {
                        sender.sendMessage("Your nickname is too long!")
                    }
                }
                return true
            }

            else if (command.name == "rlnick") {
                Nick.instance.reloadConfig()
                sender.sendMessage("Nick config reloaded.")
                return true
            }
        }
        return true
    }

    fun setNick(player: Player, name: String) {
        var target = Bukkit.getPlayer(player.name)
        target.displayName = name
        target.playerListName = name
    }
}