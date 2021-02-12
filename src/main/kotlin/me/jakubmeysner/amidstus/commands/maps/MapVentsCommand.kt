package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MapVentsCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "mapvents"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /removemapvent <map name> <vent id>").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        if (map.vents.isEmpty()) {
          sender.spigot().sendMessage(
            *ComponentBuilder("This map does not have any vents!").create()
          )
        } else {
          sender.spigot().sendMessage(
            *ComponentBuilder("List of vents of map ${map.name}:").color(ChatColor.BLUE).create(),
            *map.vents.flatMap {
              ComponentBuilder("- ${it.id}: ${it.location.x}, ${it.location.y}, ${it.location.z} ")
                .append("[X]").color(ChatColor.RED)
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/removemapvent ${map.name} ${it.id}"))
                .create()
                .toList()
            }.toTypedArray()
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
