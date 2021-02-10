package me.jakubmeysner.amidstus.models

import me.jakubmeysner.amidstus.AmidstUs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.ChatColor as BukkitChatColor

class Player(val bukkitPlayer: Player, var pending: Boolean = false) {
  companion object {
    val LeaveGameItemStack = ItemStack(Material.OAK_DOOR).let {
      val meta = it.itemMeta
      meta?.setDisplayName("${BukkitChatColor.RED}Leave game")
      it.itemMeta = meta
      it
    }
  }

  var promoted = false
  var impostor = false
  var dead = false

  fun joinGame(game: Game, plugin: AmidstUs) {
    if (!game.players.contains(this)) {
      game.players.add(this)
    }

    bukkitPlayer.teleport(game.map.preGameLocation ?: return)

    for (player in plugin.server.onlinePlayers) {
      if (game.players.any { it.bukkitPlayer == player }) {
        bukkitPlayer.showPlayer(plugin, player)
        player.showPlayer(plugin, bukkitPlayer)
      } else {
        bukkitPlayer.hidePlayer(plugin, player)
        player.hidePlayer(plugin, bukkitPlayer)
      }
    }

    bukkitPlayer.inventory.clear()
    bukkitPlayer.inventory.heldItemSlot = 0
    bukkitPlayer.inventory.setItem(8, LeaveGameItemStack)
  }

  fun leaveGame(game: Game, plugin: AmidstUs) {
    game.players.remove(this)

    if (game.players.size == 0) {
      plugin.games.remove(game)
    } else if (game.type == Game.Type.PRIVATE && game.players.none { it.promoted }) {
      val randomPlayer = game.players.random()
      randomPlayer.promoted = true

      randomPlayer.bukkitPlayer.spigot().sendMessage(
        *ComponentBuilder("Because all promoted players have left the game, you have been randomly promoted.")
          .color(ChatColor.GREEN).create()
      )
    }

    bukkitPlayer.inventory.clear()

    if (pending) return

    bukkitPlayer.teleport(game.map.postGameLocation ?: return)

    for (player in plugin.server.onlinePlayers) {
      if (plugin.games.any { it.players.any { it.bukkitPlayer == player } }) {
        bukkitPlayer.hidePlayer(plugin, player)
        player.hidePlayer(plugin, bukkitPlayer)
      } else {
        bukkitPlayer.showPlayer(plugin, player)
        player.showPlayer(plugin, bukkitPlayer)
      }
    }

    plugin.server.scoreboardManager?.mainScoreboard?.getTeam("nametagVisNever")
      ?.removeEntry(bukkitPlayer.name)
  }
}
