package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.models.Player
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.entity.Player as BukkitPlayer

class EntityDamageByEntityListener(val plugin: AmidstUs) : Listener {
  @EventHandler
  fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
    if (event.entity !is BukkitPlayer) return

    val game = plugin.games.find {
      it.players.any { it.bukkitPlayer == event.entity } &&
        it.players.any { it.bukkitPlayer == event.damager }
    } ?: return

    event.isCancelled = true

    val damagee = game.players.find { it.bukkitPlayer == event.entity }!!
    val damager = game.players.find { it.bukkitPlayer == event.damager }!!

    if (damagee.dead || damagee.impostor || damager.dead || !damager.impostor || damager.killCooldownActive) return
    if (event.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return
    if (damager.bukkitPlayer.inventory.itemInMainHand != Player.ImpostorSwordItemStack) return

    damagee.dead = true

    for (player in game.players) {
      if (!player.dead) {
        player.bukkitPlayer.hidePlayer(plugin, damagee.bukkitPlayer)
      }
    }

    if (game.players.count { !it.dead && it.impostor } >= game.players.count { !it.dead && !it.impostor }) {
      game.end(plugin)
      return
    }

    damager.killCooldownActive = true
    damager.killCooldownSecondsLeft = game.killCooldownSeconds

    damager.killCooldownTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
      if (damager.killCooldownActive == false) {
        damager.killCooldownSecondsLeft = null
        damager.killCooldownTask?.cancel()
        damager.killCooldownTask = null
      } else if (damager.killCooldownSecondsLeft == 0) {
        damager.killCooldownActive = false
        damager.killCooldownSecondsLeft = null
        damager.killCooldownTask?.cancel()
        damager.killCooldownTask = null

        damager.bukkitPlayer.inventory.setItem(1, Player.ImpostorSwordItemStack)
      } else {
        damager.bukkitPlayer.inventory.setItem(1, Player.ImpostorSwordItemStack.apply {
          itemMeta = itemMeta?.apply {
            setDisplayName("${ChatColor.DARK_RED}Impostor sword (${damager.killCooldownSecondsLeft}s cooldown)")
          }
        })
      }
    }, 0, 20)
  }
}
