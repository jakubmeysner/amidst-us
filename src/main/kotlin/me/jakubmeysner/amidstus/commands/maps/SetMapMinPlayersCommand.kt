package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class SetMapMinPlayersCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "setmapminplayers"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 2) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /setmapminplayers <map name> <number>").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        val number = args[1].toIntOrNull()

        if (number == null || number < 4 || number > 25) {
          sender.spigot().sendMessage(
            *ComponentBuilder("Minimum number of players must be a valid integer between 4 and 25!")
              .color(ChatColor.RED).create()
          )
        } else if (number > map.maxNumberOfPlayers) {
          sender.spigot().sendMessage(
            *ComponentBuilder("Minimum number of players must not be greater than the maximum!")
              .color(ChatColor.RED).create()
          )
        } else {
          map.minNumberOfPlayers = number

          sender.spigot().sendMessage(
            *ComponentBuilder("Minimum number of players for map ${map.displayName} has been set to $number.")
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
      else -> listOf()
    }
  }
}
