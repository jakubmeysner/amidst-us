package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener(val plugin: AmidstUs) : Listener {
  @EventHandler
  fun onInventoryClick(event: InventoryClickEvent) {
    if (plugin.games.none { it.players.any { it.bukkit == event.whoClicked } }) return
    event.isCancelled = true
  }
}
