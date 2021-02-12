package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(val plugin: AmidstUs) : Listener {
  @EventHandler
  fun onPlayerJoin(event: PlayerJoinEvent) {
    val players = plugin.games.map { it.players }.flatten().filter { !it.pending }.map { it.bukkitPlayer }

    for (player in players) {
      player.hidePlayer(plugin, event.player)
      event.player.hidePlayer(plugin, player)
    }
  }
}
