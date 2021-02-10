package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import me.jakubmeysner.amidstus.models.*
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.ChatColor as BukkitChatColor
import org.bukkit.entity.Player as BukkitPlayer

class PlayCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "play"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (plugin.maps.none { it.playable }) {
      sender.spigot().sendMessage(
        *ComponentBuilder("No maps exist or are playable!").color(ChatColor.RED).create()
      )
    } else if (sender !is BukkitPlayer) {
      sender.spigot().sendMessage(
        *ComponentBuilder("This command can only be used by players!").color(ChatColor.RED).create()
      )
    } else if (plugin.games.any { it.players.any { it.bukkitPlayer == sender } }) {
      sender.spigot().sendMessage(
        *ComponentBuilder("You are already in game!").color(ChatColor.RED).create()
      )
    } else if (args.size > 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /play [map name]").color(ChatColor.RED).create()
      )
    } else {
      if (args.size == 1 && plugin.maps.none { it.playable && it.name == args[0] }) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any map with this name or it is not playable!")
            .color(ChatColor.RED).create()
        )
      } else {
        val games = plugin.games.filter {
          (args.isEmpty() || it.map.name == args[0]) &&
            it.players.size < it.map.maxNumberOfPlayers &&
            it.status == Game.Status.PRE_GAME
        }.sortedByDescending { it.players.size }

        val game = if (games.isEmpty()) {
          val game = Game(
            if (args.isNotEmpty())
              plugin.maps.find { it.name == args[0] }!!
            else plugin.maps.filter { it.playable }.shuffled()[0],
            Game.Type.PUBLIC
          )
          plugin.games.add(game)
          game
        } else {
          games[0]
        }

        val player = Player(sender)
        player.joinGame(game, plugin)

        sender.spigot().sendMessage(
          *ComponentBuilder("You are now playing on ${game.map.displayName}!")
            .color(ChatColor.GREEN).create()
        )

        if (game.players.size == game.map.autoStartNumberOfPlayers) {
          if (game.autoStartTask == null) {
            game.autoStartTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
              if (game.type == Game.Type.PRIVATE) {
                game.autoStartTask?.cancel()
                game.autoStartTask = null
              } else if (game.players.size < game.map.autoStartNumberOfPlayers) {
                game.autoStartTask?.cancel()
                game.autoStartTask = null
                game.autoStartSecondsLeft = null
              } else {
                if (game.autoStartSecondsLeft == null || game.autoStartSecondsLeft in 2..15) {
                  if (game.autoStartSecondsLeft == null) {
                    game.autoStartSecondsLeft = 15
                  } else {
                    game.autoStartSecondsLeft = game.autoStartSecondsLeft!! - 1
                  }
                } else {
                  game.autoStartTask?.cancel()
                  game.autoStartTask = null
                  game.autoStartSecondsLeft = null
                  game.start(plugin)
                }

                for (player in game.players) {
                  player.bukkitPlayer.sendTitle(
                    "${BukkitChatColor.BLUE}${BukkitChatColor.BOLD}${game.autoStartSecondsLeft}",
                    null,
                    0,
                    20,
                    0
                  )
                }
              }
            }, 0, 20)
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
      else -> listOf()
    }
  }
}
