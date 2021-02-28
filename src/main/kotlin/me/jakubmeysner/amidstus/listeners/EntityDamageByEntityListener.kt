package me.jakubmeysner.amidstus.listeners

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.*
import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.models.Game
import me.jakubmeysner.amidstus.models.Player
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import kotlin.random.Random
import org.bukkit.entity.Player as BukkitPlayer

class EntityDamageByEntityListener(val plugin: AmidstUs) : Listener {
  @EventHandler
  fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
    if (event.entity !is BukkitPlayer) return

    val game = plugin.games.find {
      it.players.any { it.bukkit == event.entity } &&
        it.players.any { it.bukkit == event.damager }
    } ?: return

    event.isCancelled = true

    if (game.status != Game.Status.IN_PROGRESS) return

    val damagee = game.players.find { it.bukkit == event.entity }!!
    val damager = game.players.find { it.bukkit == event.damager }!!

    if (damagee.dead || damagee.impostor || damager.dead || !damager.impostor || damager.killCooldownActive) return
    if (event.cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return
    if (damager.bukkit.inventory.itemInMainHand != Player.ImpostorSwordItemStack) return

    damagee.dead = true

    val entityId = Random.nextInt()

    val playerInfoData = PlayerInfoData(
      WrappedGameProfile.fromPlayer(damagee.bukkit),
      0,
      EnumWrappers.NativeGameMode.ADVENTURE,
      WrappedChatComponent.fromText(damagee.bukkit.displayName)
    )

    val playerInfoPacket = PacketContainer(PacketType.Play.Server.PLAYER_INFO).apply {
      modifier.writeDefaults()
      playerInfoDataLists.write(0, listOf(playerInfoData))
    }

    val namedEntitySpawnPacket = PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN).apply {
      integers.write(0, entityId)
      uuiDs.write(0, playerInfoData.profile.uuid)
      doubles.write(0, damagee.bukkit.location.x)
      doubles.write(1, damagee.bukkit.location.y)
      doubles.write(2, damagee.bukkit.location.z)
    }

    val entityMetadataPacket = PacketContainer(PacketType.Play.Server.ENTITY_METADATA).apply {
      integers.write(0, entityId)
      watchableCollectionModifier.write(0, WrappedDataWatcher.getEntityWatcher(damagee.bukkit).apply {
        setObject(
          6,
          WrappedDataWatcher.Registry.get(EnumWrappers.getEntityPoseClass()),
          EnumWrappers.EntityPose.SLEEPING.toNms()
        )
      }.watchableObjects)
    }

    for (player in game.players) {
      if (!player.dead) {
        player.bukkit.hidePlayer(plugin, damagee.bukkit)
      }

      plugin.protocolManager.sendServerPacket(player.bukkit, playerInfoPacket)
      plugin.protocolManager.sendServerPacket(player.bukkit, namedEntitySpawnPacket)
      plugin.protocolManager.sendServerPacket(player.bukkit, entityMetadataPacket)
    }

    if (game.players.count { !it.dead && it.impostor } >= game.players.count { !it.dead && !it.impostor }) {
      game.end(plugin)
      return
    }

    damager.killCooldownActive = true
    damager.killCooldownSecondsLeft = game.killCooldownSeconds

    damager.killCooldownTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
      if (damager.killCooldownSecondsLeft == 0) {
        damager.killCooldownActive = false
        damager.killCooldownSecondsLeft = null
        damager.killCooldownTask?.cancel()
        damager.killCooldownTask = null
        damager.bukkit.inventory.setItem(1, Player.ImpostorSwordItemStack)
      } else {
        damager.bukkit.inventory.setItem(1, Player.ImpostorSwordItemStack.clone().apply {
          itemMeta = itemMeta?.clone()?.apply {
            setDisplayName("${ChatColor.DARK_RED}Impostor sword (${damager.killCooldownSecondsLeft}s cooldown)")
          }
        })

        damager.killCooldownSecondsLeft = damager.killCooldownSecondsLeft!! - 1
      }
    }, 0, 20)
  }
}
