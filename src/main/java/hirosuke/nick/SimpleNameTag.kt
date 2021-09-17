package hirosuke.nick

import org.bukkit.ChatColor

class SimpleNameTag internal constructor(text: String) : NameTag {
    override val text: String

    init {
        this.text = ChatColor.translateAlternateColorCodes('&', text)
    }
}