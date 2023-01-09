package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class PlayerDropItemListener(val plugin: AmidstUs) : Listener {
    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (plugin.games.none { it.players.any { it.bukkit == event.player } }) return
        event.isCancelled = true
    }
}
