package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class PlayerSwapHandItemsListener(val plugin: AmidstUs) : Listener {
    @EventHandler
    fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
        if (plugin.games.none { it.players.any { it.bukkit == event.player } }) return
        event.isCancelled = true
    }
}
