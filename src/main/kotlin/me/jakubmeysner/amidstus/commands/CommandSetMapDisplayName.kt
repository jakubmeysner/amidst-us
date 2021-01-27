package me.jakubmeysner.amidstus.commands

import me.jakubmeysner.amidstus.AmidstUs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CommandSetMapDisplayName(val plugin: AmidstUs) : CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size < 2) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /setmapdisplayname <name> <display name>").color(ChatColor.RED).create()
      )
    } else {
      val map = this.plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        map.displayName = args.drop(1).joinToString(separator = " ")
        sender.spigot().sendMessage(
          *ComponentBuilder("Changed the display name of map \"${map.name}\" to \"${map.displayName}\".")
            .color(ChatColor.GREEN).create()
        )
      }
    }

    return true
  }
}
