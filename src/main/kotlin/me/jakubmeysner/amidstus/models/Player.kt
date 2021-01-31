package me.jakubmeysner.amidstus.models

import me.jakubmeysner.amidstus.AmidstUs
import org.bukkit.entity.Player

class Player(val bukkitPlayer: Player) {
  fun joinGame(game: Game, plugin: AmidstUs) {
    game.players.add(this)
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
  }

  fun leaveGame(game: Game, plugin: AmidstUs) {
    game.players.remove(this)
    bukkitPlayer.teleport(game.map.postGameLocation ?: return)

    if (game.players.size == 0) {
      plugin.games.remove(game)
    }

    for (player in plugin.server.onlinePlayers) {
      if (plugin.games.any { it.players.any { it.bukkitPlayer == player } }) {
        bukkitPlayer.hidePlayer(plugin, player)
        player.hidePlayer(plugin, bukkitPlayer)
      } else {
        bukkitPlayer.showPlayer(plugin, player)
        player.showPlayer(plugin, bukkitPlayer)
      }
    }
  }
}
