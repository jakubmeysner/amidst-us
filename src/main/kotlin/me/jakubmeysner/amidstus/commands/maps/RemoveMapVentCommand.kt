package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class RemoveMapVentCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "removemapvent"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 2) {
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
        val vent = map.vents.find { it.id == args[1] }
        if (vent == null) {
          sender.spigot().sendMessage(
            *ComponentBuilder("Could not find any vent with this id!").color(ChatColor.RED).create()
          )
        } else {
          map.vents.remove(vent)
          for (itVent in map.vents){
            itVent.linkedVents.remove(vent.id)
          }
          sender.spigot().sendMessage(
            *ComponentBuilder("Succesfully removed vent ${vent.id} from map ${map.name}.").color(ChatColor.GREEN).create()
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
      2 -> plugin.maps.find { it.name == args[0] }?.vents?.map { it.id }?.filter { it.startsWith(args[1]) } ?: listOf()
      else -> listOf()
    }
  }
}
