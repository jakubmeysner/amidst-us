package me.jakubmeysner.amidstus.commands

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.models.Map
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.util.regex.Pattern

class CommandCreateMap(val plugin: AmidstUs) : CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /createmap <name>").color(ChatColor.RED).create()
      )
    } else if (!Pattern.compile("^[a-z]+$").matcher(args[0]).matches()) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Name may only include lowercase letters!").color(ChatColor.RED).create()
      )
    } else if (this.plugin.maps.find { it.name == args[0] } != null) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Name must be unique!").color(ChatColor.RED).create()
      )
    } else {
      val map = Map(args[0])
      plugin.maps.add(map)
      sender.spigot().sendMessage(
        *ComponentBuilder("Created new map \"${map.name}\".").color(ChatColor.GREEN).create()
      )
    }

    return true
  }
}
