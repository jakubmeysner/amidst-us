package me.jakubmeysner.amidstus.commands

import me.jakubmeysner.amidstus.AmidstUs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CommandMap(val plugin: AmidstUs) : CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /map <name>").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        sender.spigot().sendMessage(
          *ComponentBuilder("Map details:\n").color(ChatColor.BLUE)
            .append(
              """
                Name: ${map.name}
                Display name: ${map.displayName}
              """.trimIndent()
            ).color(null).create()
        )
      }
    }

    return true
  }
}
