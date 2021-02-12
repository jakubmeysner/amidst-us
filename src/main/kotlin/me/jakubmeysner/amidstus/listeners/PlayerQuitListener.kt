package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(val plugin: AmidstUs) : Listener {
  @EventHandler
  fun onPlayerQuit(event: PlayerQuitEvent) {
    val game = plugin.games.find { it.players.any { it.bukkit == event.player } } ?: return
    val player = game.players.find { it.bukkit == event.player }!!
    player.leaveGame(game, plugin)
  }
}
