package me.jakubmeysner.amidstus.models

import me.jakubmeysner.amidstus.AmidstUs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.scheduler.BukkitTask
import org.bukkit.ChatColor as BukkitChatColor

class Game(var map: Map, val type: Type) {
  enum class Type {
    PUBLIC, PRIVATE
  }

  enum class Status {
    NOT_STARTED, IN_PROGRESS, ENDED
  }

  var status = Status.NOT_STARTED
  val players = mutableListOf<Player>()
  var autoStartTask: BukkitTask? = null
  var autoStartSecondsLeft: Int? = null

  var killCooldownSeconds = map.killCooldownSeconds
  var maxNumberOfImpostors = map.maxNumberOfImpostors

  fun start(plugin: AmidstUs) {
    status = Status.IN_PROGRESS

    val numberOfImpostors = minOf(map.maxNumberOfImpostors, players.size / 4, maxNumberOfImpostors)
    val shuffledPlayersForRoles = players.shuffled()
    val impostors = shuffledPlayersForRoles.take(numberOfImpostors)
    val crewmates = shuffledPlayersForRoles.takeLast(shuffledPlayersForRoles.size - numberOfImpostors)

    for (player in players) {
      player.bukkit.inventory.clear()
      player.bukkit.inventory.heldItemSlot = 0

      plugin.server.scoreboardManager?.mainScoreboard?.getTeam("nametagVisNever")
        ?.addEntry(player.bukkit.name)
    }

    for (impostor in impostors) {
      impostor.impostor = true

      impostor.bukkit.sendTitle(
        "${BukkitChatColor.DARK_RED}Impostor",
        null, 0, 5 * 20, 0
      )

      impostor.bukkit.spigot().sendMessage(
        *ComponentBuilder("You are ${if (impostors.size > 1) "an" else "the"} impostor!\n")
          .color(ChatColor.DARK_RED)
          .append(
            "Your objective is to kill crewmates and sabotage the ship.",
            ComponentBuilder.FormatRetention.NONE
          )
          .append(
            when (impostors.size) {
              1 -> ""
              2 -> "\nThe other impostor is ${impostors.filter { it != impostor }[0]}."
              else -> "\nThe other impostors are ${
                impostors.filter { it != impostor }
                  .map { it.bukkit.name }.joinToString(", ")
              }."
            }
          ).create()
      )

      impostor.bukkit.inventory.setItem(1, Player.ImpostorSwordItemStack)
    }

    for (crewmate in crewmates) {
      crewmate.bukkit.sendTitle(
        "${ChatColor.DARK_GREEN}Crewmate",
        if (impostors.size > 1) "There are ${impostors.size} impostors amidst us!"
        else "There is 1 impostor amidst us!",
        0, 5 * 20, 0
      )

      crewmate.bukkit.spigot().sendMessage(
        *ComponentBuilder("You are a crewmate!\n").color(ChatColor.DARK_GREEN)
          .append(
            "Your objective is to complete tasks and uncover the " +
              "${if (impostors.size > 1) "identities of the impostors" else "identity of the impostor"}.",
            ComponentBuilder.FormatRetention.NONE
          ).create()
      )
    }

    players.shuffled().zip(map.seats.shuffled())
      .forEach { (player, location) -> player.bukkit.teleport(location) }
  }

  fun end(plugin: AmidstUs) {
    status = Status.ENDED
    val impostorsWon = players.count { !it.dead && it.impostor } >= players.count { !it.dead && !it.impostor }

    for (player in players) {
      player.bukkit.sendTitle(
        if (impostorsWon)
          "${BukkitChatColor.RED}Impostors win"
        else
          "${BukkitChatColor.GREEN}Crewmates win",
        null,
        0,
        5 * 20,
        0
      )
    }

    plugin.server.scheduler.runTaskLater(plugin, Runnable {
      if (type == Type.PUBLIC) {
        for (player in players) {
          plugin.server.scoreboardManager?.mainScoreboard?.getTeam("nametagVisNever")
            ?.removeEntry(player.bukkit.name)
          Player.playPublicGame(plugin, null, player.bukkit)
        }

        plugin.games.remove(this)
      } else {
        status = Status.NOT_STARTED

        for (player in players) {
          plugin.server.scoreboardManager?.mainScoreboard?.getTeam("nametagVisNever")
            ?.removeEntry(player.bukkit.name)
          player.impostor = false
          player.dead = false
          player.joinGame(this, plugin)
        }
      }
    }, 5 * 20)
  }
}
