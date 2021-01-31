package me.jakubmeysner.amidstus.models

import me.jakubmeysner.amidstus.AmidstUs
import org.bukkit.Location
import org.bukkit.entity.Player

class Player(val bukkitPlayer: Player) {
  fun joinGame(game: Game, plugin: AmidstUs) {
    bukkitPlayer.teleport(game.map.preGameLocation ?: return)

    for (player in bukkitPlayer.server.onlinePlayers) {
      if (game.players.none { it.bukkitPlayer == player }) {
        bukkitPlayer.hidePlayer(plugin, player)
      }
    }

    for (player in game.players) {
      player.bukkitPlayer.showPlayer(plugin, bukkitPlayer)
    }
  }
}
