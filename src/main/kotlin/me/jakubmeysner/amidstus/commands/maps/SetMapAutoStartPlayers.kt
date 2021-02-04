package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class SetMapAutoStartPlayers(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "setmapautostartplayers"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 2) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /setmapautostartplayers <map name> <number>").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        val number = args[1].toIntOrNull()

        if (number == null || number !in map.minNumberOfPlayers..map.maxNumberOfPlayers) {
          sender.spigot().sendMessage(
            *ComponentBuilder("Auto start number of players must be a valid integer between " +
              "${map.minNumberOfPlayers} and ${map.maxNumberOfImpostors}!").color(ChatColor.RED).create()
          )
        } else {
          map.autoStartNumberOfPlayers = number
          sender.spigot().sendMessage(
            *ComponentBuilder("Set auto start number of players for map ${map.displayName} to ${number}.")
              .color(ChatColor.GREEN).create()
          )
        }
      }
    }

    return true
  }

  override fun onTabComplete(
    sender: CommandSender,
    command: Command,
    alias: String,
    args: Array<out String>
  ): List<String> {
    return when (args.size) {
      1 -> plugin.maps.map { it.name }.filter { it.startsWith(args[0]) }
      2 -> plugin.maps.find { it.name == args[0] }?.let {
        (it.minNumberOfPlayers..it.maxNumberOfPlayers).map { it.toString() }.filter { it.startsWith(args[1]) }
      } ?: listOf()
      else -> listOf()
    }
  }
}
