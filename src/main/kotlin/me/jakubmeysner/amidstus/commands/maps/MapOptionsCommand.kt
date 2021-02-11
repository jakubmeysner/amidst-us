package me.jakubmeysner.amidstus.commands.maps

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import me.jakubmeysner.amidstus.models.Map
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class MapOptionsCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "mapoptions"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size < 2) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /mapoptions <map name> <option> [new value]").color(ChatColor.RED).create()
      )
    } else {
      val map = plugin.maps.find { it.name == args[0] }

      if (map == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name!").color(ChatColor.RED).create()
        )
      } else {
        when (args[1]) {
          "name" -> {
            if (args.size == 2) {
              sender.spigot().sendMessage(
                *ComponentBuilder("Name of map ${map.displayName} is ${map.name}.").create()
              )
            } else {
              if (args.size != 3 || !Map.namePattern.matcher(args[2]).matches()) {
                sender.spigot().sendMessage(
                  *ComponentBuilder("New name must only contain lowercase letters, numbers and underscores!")
                    .color(ChatColor.RED).create()
                )
              } else if (plugin.maps.any { it.name == args[2] }) {
                sender.spigot().sendMessage(
                  *ComponentBuilder("New value must be unique!").color(ChatColor.RED).create()
                )
              } else {
                map.name = args[2]
                sender.spigot().sendMessage(
                  *ComponentBuilder("Changed name of map ${map.displayName} to ${map.name}.")
                    .color(ChatColor.GREEN).create()
                )
              }
            }
          }

          "displayname" -> {
            if (args.size == 2) {
              sender.spigot().sendMessage(
                *ComponentBuilder("Display name of map ${map.displayName} is ${map.displayName}.").create()
              )
            } else {
              map.displayName = args.drop(2).joinToString(" ")
              sender.spigot().sendMessage(
                *ComponentBuilder("Changed display name of map ${map.displayName} to ${map.displayName}.")
                  .color(ChatColor.GREEN).create()
              )
            }
          }

          "minnoplayers" -> {
            if (args.size == 2) {
              sender.spigot().sendMessage(
                *ComponentBuilder("Min number of players of map ${map.displayName} is ${map.minNumberOfPlayers}.")
                  .create()
              )
            } else {
              val newValue = args[2].toIntOrNull()

              if (args.size != 3 || newValue == null || newValue !in 4..map.maxNumberOfPlayers) {
                sender.spigot().sendMessage(
                  *ComponentBuilder("New value must be a valid integer between 4 and ${map.maxNumberOfPlayers}!")
                    .color(ChatColor.RED).create()
                )
              } else {
                map.minNumberOfPlayers = newValue
                sender.spigot().sendMessage(
                  *ComponentBuilder(
                    "Changed min number of players of map ${map.displayName} to " +
                      "${map.minNumberOfPlayers}."
                  ).color(ChatColor.GREEN).create()
                )
              }
            }
          }

          "maxnoplayers" -> {
            if (args.size == 2) {
              sender.spigot().sendMessage(
                *ComponentBuilder("Max number of players of map ${map.displayName} is ${map.maxNumberOfPlayers}.")
                  .create()
              )
            } else {
              val newValue = args[2].toIntOrNull()

              if (args.size != 3 || newValue == null || newValue !in map.minNumberOfPlayers..25) {
                sender.spigot().sendMessage(
                  *ComponentBuilder("New value must be a valid integer between ${map.minNumberOfPlayers} and 25.")
                    .color(ChatColor.RED).create()
                )
              } else {
                map.maxNumberOfPlayers = newValue
                sender.spigot().sendMessage(
                  *ComponentBuilder("Changed max number of players of map ${map.displayName} to " +
                    "${map.maxNumberOfPlayers}").color(ChatColor.GREEN).create()
                )
              }
            }
          }

          "maxnoimpostors" -> {
            if (args.size == 2) {
              sender.spigot().sendMessage(
                *ComponentBuilder("Max number of impostors of map ${map.displayName} is " +
                  "${map.maxNumberOfImpostors}.").create()
              )
            } else {
              val newValue = args[2].toIntOrNull()

              if (args.size != 3 || newValue == null || newValue !in 1..6) {
                sender.spigot().sendMessage(
                  *ComponentBuilder("New value must be a valid integer between 1 and 6!")
                    .color(ChatColor.RED).create()
                )
              } else {
                map.maxNumberOfImpostors = newValue
                sender.spigot().sendMessage(
                  *ComponentBuilder("Changed max number of impostors of map ${map.displayName} to " +
                    "${map.maxNumberOfImpostors}").color(ChatColor.GREEN).create()
                )
              }
            }
          }

          "autostartnoplayers" -> {
            if (args.size == 2) {
              sender.spigot().sendMessage(
                *ComponentBuilder(
                  "Autostart number of players of map ${map.displayName} is " +
                    "${map.autoStartNumberOfPlayers}"
                ).create()
              )
            } else {
              val newValue = args[2].toIntOrNull()

              if (args.size != 3 || newValue == null || newValue !in map.minNumberOfPlayers..map.maxNumberOfPlayers) {
                sender.spigot().sendMessage(
                  *ComponentBuilder(
                    "New value must be a valid integer between ${map.minNumberOfPlayers} and " +
                      "${map.maxNumberOfPlayers}!"
                  ).color(ChatColor.RED).create()
                )
              } else {
                map.autoStartNumberOfPlayers = newValue
                sender.spigot().sendMessage(
                  *ComponentBuilder(
                    "Changed auto start number of players of map ${map.displayName} to " +
                      "${map.autoStartNumberOfPlayers}."
                  ).color(ChatColor.GREEN).create()
                )
              }
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
      2 -> listOf(
        "name",
        "displayname",
        "minnoplayers",
        "maxnoplayers",
        "maxnoimpostors",
        "autostartnoplayers"
      ).filter { it.startsWith(args[1]) }
      3 -> when (args[1]) {
        "minnoplayers" -> plugin.maps.find { it.name == args[0] }?.let {
          (4..it.maxNumberOfPlayers).map { it.toString() }.filter { it.startsWith(args[2]) }
        } ?: listOf()
        "maxnoplayers" -> plugin.maps.find { it.name == args[0] }?.let {
          (it.minNumberOfPlayers..25).map { it.toString() }.filter { it.startsWith(args[2]) }
        } ?: listOf()
        "maxnoimpostors" -> (1..6).map { it.toString() }.filter { it.startsWith(args[2]) }
        "autostartnoplayers" -> plugin.maps.find { it.name == args[0] }?.let {
          (it.minNumberOfPlayers..it.maxNumberOfPlayers).map { it.toString() }.filter { it.startsWith(args[2]) }
        } ?: listOf()
        else -> listOf()
      }
      else -> listOf()
    }
  }
}
