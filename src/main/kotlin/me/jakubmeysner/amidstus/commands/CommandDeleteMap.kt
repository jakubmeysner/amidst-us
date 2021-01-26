package me.jakubmeysner.amidstus.commands

import me.jakubmeysner.amidstus.AmidstUs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CommandDeleteMap(val plugin: AmidstUs) : CommandExecutor {
  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /deletemap <name>").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        plugin.maps.remove(map)
        sender.spigot().sendMessage(
          *ComponentBuilder("Deleted map \"${args[0]}\".").color(ChatColor.GREEN).create()
        )
      }
    }

    return true
  }
}
