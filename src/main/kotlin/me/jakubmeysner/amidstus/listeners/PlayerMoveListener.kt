package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.models.EmergencyMeeting
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerMoveListener(val plugin: AmidstUs) : Listener {
  @EventHandler
  fun onPlayerMove(event: PlayerMoveEvent) {
    val game = plugin.games.find { it.players.any { it.bukkit == event.player } } ?: return

    event.to?.let { to ->
      if (event.from.x != to.x || event.from.y != to.y || event.from.z != to.z) {
        if (game.emergencyMeetings.isNotEmpty() && game.emergencyMeetings.last().phase != EmergencyMeeting.Phase.ENDED) {
          event.isCancelled = true
        }
      }
    }
  }
}
