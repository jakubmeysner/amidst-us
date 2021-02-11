package me.jakubmeysner.amidstus.models

import me.jakubmeysner.amidstus.AmidstUs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.ChatColor as BukkitChatColor

class Player(val bukkitPlayer: Player, var pending: Boolean = false) {
  companion object {
    val LeaveGameItemStack = ItemStack(Material.OAK_DOOR).apply {
      itemMeta?.setDisplayName("${BukkitChatColor.RED}Leave game")
    }

    fun playPublicGames(plugin: AmidstUs, map: Map?, bukkitPlayer: Player) {
      val games = plugin.games.filter {
        (map == null || it.map == map) &&
          it.players.size < it.map.maxNumberOfPlayers &&
          it.status == Game.Status.NOT_STARTED
      }.sortedByDescending { it.players.size }

      val game = if (games.isEmpty()) {
        val game = Game(
          if (map != null)
            plugin.maps.find { it == map }!!
          else plugin.maps.filter { it.playable }.shuffled()[0],
          Game.Type.PUBLIC
        )
        plugin.games.add(game)
        game
      } else {
        games[0]
      }

      val player = Player(bukkitPlayer)
      player.joinGame(game, plugin)

      bukkitPlayer.spigot().sendMessage(
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

  var promoted = false
  var impostor = false
  var dead = false

  fun joinGame(game: Game, plugin: AmidstUs) {
    if (!game.players.contains(this)) {
      game.players.add(this)
    }

    bukkitPlayer.teleport(game.map.preGameLocation ?: return)

    for (player in plugin.server.onlinePlayers) {
      if (game.players.any { it.bukkitPlayer == player }) {
        bukkitPlayer.showPlayer(plugin, player)
        player.showPlayer(plugin, bukkitPlayer)
      } else {
        bukkitPlayer.hidePlayer(plugin, player)
        player.hidePlayer(plugin, bukkitPlayer)
      }
    }

    bukkitPlayer.inventory.clear()
    bukkitPlayer.inventory.heldItemSlot = 0
    bukkitPlayer.inventory.setItem(8, LeaveGameItemStack)
  }

  fun leaveGame(game: Game, plugin: AmidstUs) {
    game.players.remove(this)

    if (game.players.size == 0) {
      plugin.games.remove(game)
    } else if (game.type == Game.Type.PRIVATE && game.players.none { it.promoted }) {
      val randomPlayer = game.players.random()
      randomPlayer.promoted = true

      randomPlayer.bukkitPlayer.spigot().sendMessage(
        *ComponentBuilder("Because all promoted players have left the game, you have been randomly promoted.")
          .color(ChatColor.GREEN).create()
      )
    }

    bukkitPlayer.inventory.clear()

    if (pending) return

    if (
      game.players.none { !it.dead && it.impostor } ||
      game.players.count { !it.dead && it.impostor } >= game.players.count { !it.dead && !it.impostor }
    ) {
      game.end(plugin)
    }

    bukkitPlayer.teleport(game.map.postGameLocation ?: return)

    for (player in plugin.server.onlinePlayers) {
      if (plugin.games.any { it.players.any { it.bukkitPlayer == player } }) {
        bukkitPlayer.hidePlayer(plugin, player)
        player.hidePlayer(plugin, bukkitPlayer)
      } else {
        bukkitPlayer.showPlayer(plugin, player)
        player.showPlayer(plugin, bukkitPlayer)
      }
    }

    plugin.server.scoreboardManager?.mainScoreboard?.getTeam("nametagVisNever")
      ?.removeEntry(bukkitPlayer.name)
  }
}
