package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MapCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "map"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /map <map name>").color(ChatColor.RED).create()
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
                Playable: ${if (map.playable) "Yes" else "No"}
                Min players: ${map.minNumberOfPlayers}
                Max players: ${map.maxNumberOfPlayers}
                Max impostors: ${map.maxNumberOfImpostors}
                Auto start players: ${map.autoStartNumberOfPlayers}
                Time: ${map.time ?: "unset"}
              """.trimIndent()
            ).color(null).create()
        )
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
