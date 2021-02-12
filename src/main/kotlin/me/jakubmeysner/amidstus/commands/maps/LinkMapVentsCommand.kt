package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class LinkMapVentsCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "linkmapvents"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 3) {
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
        val vent1 = map.vents.find { it.id == args[1] }

        if (vent1 == null) {
          sender.spigot().sendMessage(
            *ComponentBuilder("Could not find any vent with this id (first)!").color(ChatColor.RED).create()
          )
        } else {
          val vent2 = map.vents.find { it.id == args[2] }

          if (vent2 == null) {
            sender.spigot().sendMessage(
              *ComponentBuilder("Could not find any vent with this id (second)!").color(ChatColor.RED).create()
            )
          } else {
            if (vent1.linkedVents.contains(vent2.id)) {
              vent1.linkedVents.remove(vent2.id)
              vent2.linkedVents.remove(vent1.id)

              sender.spigot().sendMessage(
                *ComponentBuilder("Succesfully unlinked vents ${vent1.id} and ${vent2.id} in map ${map.name}.")
                  .color(ChatColor.GREEN).create()
              )
            } else {
              vent1.linkedVents.add(vent2.id)
              vent2.linkedVents.add(vent1.id)

              sender.spigot().sendMessage(
                *ComponentBuilder("Succesfully linked vents ${vent1.id} and ${vent2.id} in map ${map.name}.")
                  .color(ChatColor.GREEN).create()
              )
            }
          }
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
      2 -> plugin.maps.find { it.name == args[0] }?.vents?.map { it.id }?.filter { it.startsWith(args[1]) }
        ?: listOf()
      3 -> plugin.maps.find { it.name == args[0] }?.vents?.map { it.id }?.filter { it.startsWith(args[2]) }
        ?: listOf()
      else -> listOf()
    }
  }
}
