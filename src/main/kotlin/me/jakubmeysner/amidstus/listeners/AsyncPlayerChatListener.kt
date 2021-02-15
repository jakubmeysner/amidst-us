package me.jakubmeysner.amidstus.listeners

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.models.Game
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class AsyncPlayerChatListener(val plugin: AmidstUs) : Listener {
  @EventHandler
  fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
    val game = plugin.games.find { it.players.any { it.bukkit == event.player } }

    if (game == null) {
      event.recipients.removeAll(plugin.games.flatMap { it.players }.map { it.bukkit })
    } else {
      val player = game.players.find { it.bukkit == event.player }!!
      event.recipients.removeAll(plugin.games.minus(game).flatMap { it.players }.map { it.bukkit })

      if (game.status == Game.Status.IN_PROGRESS) {
        if (player.dead) {
          event.recipients.removeAll(game.players.filter { !it.dead }.map { it.bukkit })
        } else {
          event.isCancelled = true
          player.bukkit.spigot().sendMessage(
            *ComponentBuilder(
              """
                You can't use the chat during the game!
                To talk with other players, call an emergency meeting.
              """.trimIndent()
            ).color(ChatColor.RED).create()
          )
        }
      }
    }
  }
}
