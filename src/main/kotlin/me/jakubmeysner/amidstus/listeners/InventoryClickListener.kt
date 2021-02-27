package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.commands.games.GameOptionsCommand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener(val plugin: AmidstUs) : Listener {
  @EventHandler
  fun onInventoryClick(event: InventoryClickEvent) {
    if (event.whoClicked !is Player) return
    val game = plugin.games.find { it.players.any { it.bukkit == event.whoClicked } } ?: return
    event.isCancelled = true

    when (event.currentItem) {
      GameOptionsCommand.PreviousMapItemStack -> {
        val previousMap = plugin.maps.indexOf(game.map).let {
          if (it == 0) plugin.maps.last() else plugin.maps[it - 1]
        }

        (event.whoClicked as Player).performCommand("gameoptions map ${previousMap.name}")
        (event.whoClicked as Player).performCommand("gameoptions")
      }

      GameOptionsCommand.NextMapItemStack -> {
        val nextMap = plugin.maps.indexOf(game.map).let {
          if (it == plugin.maps.lastIndex) plugin.maps.first() else plugin.maps[it + 1]
        }

        (event.whoClicked as Player).performCommand("gameoptions map ${nextMap.name}")
        (event.whoClicked as Player).performCommand("gameoptions")
      }

      GameOptionsCommand.DecreaseMaxNumberOfImpostorsItemStack -> {
        (event.whoClicked as Player).performCommand("gameoptions maxnoimpostors ${game.maxNumberOfImpostors - 1}")
        (event.whoClicked as Player).performCommand("gameoptions")
      }

      GameOptionsCommand.IncreaseMaxNumberOfImporsItemStack -> {
        (event.whoClicked as Player).performCommand("gameoptions maxnoimpostors ${game.maxNumberOfImpostors + 1}")
        (event.whoClicked as Player).performCommand("gameoptions")
      }

      GameOptionsCommand.DecreaseKillCooldownSecondsItemStack -> {
        val newValue = (10..60 step 5).filter { it < game.killCooldownSeconds }.maxOrNull()
        (event.whoClicked as Player).performCommand("gameoptions killcooldownseconds ${newValue}")
        (event.whoClicked as Player).performCommand("gameoptions")
      }

      GameOptionsCommand.IncreaseKillCooldownSecondsItemStack -> {
        val NewValue = (10..60 step 5).filter { it > game.killCooldownSeconds }.minOrNull()
        (event.whoClicked as Player).performCommand("gameoptions killcooldownseconds ${NewValue}")
        (event.whoClicked as Player).performCommand("gameoptions")
      }
    }
  }
}
