package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.models.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent

class InventoryListener(val plugin: AmidstUs) : Listener {
  @EventHandler
  fun onPlayerInteract(event: PlayerInteractEvent) {
    if (plugin.games.none { it.players.any { it.bukkit == event.player } }) return
    event.isCancelled = true

    when (event.item) {
      Player.LeaveGameItemStack -> {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        event.player.performCommand("leavegame")
      }
    }
  }

  @EventHandler
  fun onInventoryClick(event: InventoryClickEvent) {
    if (plugin.games.none { it.players.any { it.bukkit == event.whoClicked } }) return
    event.isCancelled = true
  }

  @EventHandler
  fun onPlayerDropItem(event: PlayerDropItemEvent) {
    if (plugin.games.none { it.players.any { it.bukkit == event.player } }) return
    event.isCancelled = true
  }

  @EventHandler
  fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
    if (plugin.games.none { it.players.any { it.bukkit == event.player } }) return
    event.isCancelled = true
  }
}
