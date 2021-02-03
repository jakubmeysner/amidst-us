package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MapSeatsCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "mapseats"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /mapseats <map name>").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        if (map.seats.isEmpty()) {
          sender.spigot().sendMessage(
            *ComponentBuilder("Could not find any seats for this map!").color(ChatColor.GRAY).create()
          )
        } else {
          sender.spigot().sendMessage(
            *ComponentBuilder("Seats of map ${map.displayName}:\n").color(ChatColor.BLUE)
              .append(map.seats.flatMap {
                ComponentBuilder("- ${map.seats.indexOf(it)}: ${it.x}, ${it.y}, ${it.z} ").color(null)
                  .append("[X]").color(ChatColor.RED)
                  .event(ClickEvent(
                    ClickEvent.Action.RUN_COMMAND, "/removemapseat ${map.name} ${map.seats.indexOf(it)}"
                  ))
                  .append(if (it != map.seats.last()) "\n" else "").create().toList()
              }.toTypedArray()).create()
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
