package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor

class GameOptionsCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "gameoptions"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    val game = plugin.games.find { it.players.any { it.bukkitPlayer == sender } }

    if (game == null) {
      sender.spigot().sendMessage(
        *ComponentBuilder("You are not in game at the moment!").color(ChatColor.RED).create()
      )
    } else {
      val player = game.players.find { it.bukkitPlayer == sender }!!

      if (args.isEmpty()) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Game options:").color(ChatColor.BLUE)
            .append(
              """
                Map: ${game.map.displayName}
                Max number of impostors: ${game.maxNumberOfImpostors}
                Kill cooldown seconds: ${game.killCooldownSeconds}
              """.trimIndent(),
              ComponentBuilder.FormatRetention.NONE
            ).create()
        )
      } else if (args.size == 1) {
        when (args[0]) {
          "map" -> {
            sender.spigot().sendMessage(
              *ComponentBuilder("Map is ${game.map.displayName}.").create()
            )
          }

          "maxnoimpostors" -> {
            sender.spigot().sendMessage(
              *ComponentBuilder("Max number of impostors is ${game.maxNumberOfImpostors}.").create()
            )
          }

          "killcooldownseconds" -> {
            sender.spigot().sendMessage(
              *ComponentBuilder("Kill cooldown seconds is ${game.killCooldownSeconds}.").create()
            )
          }
        }
      } else {
        if (!player.promoted) {
          sender.spigot().sendMessage(
            *ComponentBuilder("To be able to modify game options you need to be promoted!")
              .color(ChatColor.RED).create()
          )
        } else {
          when (args[0]) {
            "map" -> {
              val map = plugin.maps.find { it.name == args[1] }

              if (map == null || !map.playable) {
                sender.spigot().sendMessage(
                  *ComponentBuilder("Could not find any map with this name or it is not playable!")
                    .color(ChatColor.RED).create()
                )
              } else if (map == game.map) {
                sender.spigot().sendMessage(
                  *ComponentBuilder("You are already playing on this map!")
                    .color(ChatColor.RED).create()
                )
              } else {
                game.map = map
                game.maxNumberOfImpostors = minOf(game.maxNumberOfImpostors, game.map.maxNumberOfImpostors)

                for (itPlayer in game.players) {
                  itPlayer.bukkitPlayer.teleport(game.map.preGameLocation!!)

                  if (map.time != null) {
                    itPlayer.bukkitPlayer.setPlayerTime(map.time!!.toLong(), false)
                  } else {
                    itPlayer.bukkitPlayer.resetPlayerTime()
                  }
                }

                sender.spigot().sendMessage(
                  *ComponentBuilder("Changed map to ${map.displayName}.").color(ChatColor.GREEN).create()
                )
              }
            }

            "maxnoimpostors" -> {
              val newValue = args[1].toIntOrNull()

              if (args.size != 2 || newValue == null || newValue !in 1..game.map.maxNumberOfImpostors) {
                sender.spigot().sendMessage(
                  *ComponentBuilder(
                    "New value must be a valid integer between 1 and " +
                      "${game.map.maxNumberOfImpostors}!"
                  ).color(ChatColor.RED).create()
                )
              } else {
                game.maxNumberOfImpostors = newValue
                sender.spigot().sendMessage(
                  *ComponentBuilder("Max number of impostors has been set to ${game.maxNumberOfImpostors}.")
                    .color(ChatColor.GREEN).create()
                )
              }
            }

            "killcooldownseconds" -> {
              val newValue = args[1].toIntOrNull()

              if (args.size != 2 || newValue == null || newValue !in 10..60) {
                sender.spigot().sendMessage(
                  *ComponentBuilder("New value must be a valid integer between 1 and 60!")
                    .color(ChatColor.RED).create()
                )
              } else {
                game.killCooldownSeconds = newValue
                sender.spigot().sendMessage(
                  *ComponentBuilder("Kill cooldown seconds has been set to ${game.killCooldownSeconds}.")
                    .color(ChatColor.GREEN).create()
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
      1 -> listOf(
        "map",
        "maxnoimpostors",
        "killcooldownsecs",
      )

      2 -> when (args[0]) {
        "map" -> plugin.maps.filter { it.playable }.map { it.name }.filter { it.startsWith(args[1]) }
        "maxnoimpostors" -> plugin.games.find { it.players.any { it.bukkitPlayer == sender } }?.map?.let {
          (1..it.maxNumberOfImpostors).map { it.toString() }.filter { it.startsWith(args[1]) }
        } ?: (1..6).map { it.toString() }.filter { it.startsWith(args[1]) }
        "killcooldownsecs" -> (10..60).map { it.toString() }.filter { it.startsWith(args[1]) }
        else -> listOf()
      }

      else -> listOf()
    }
  }
}
