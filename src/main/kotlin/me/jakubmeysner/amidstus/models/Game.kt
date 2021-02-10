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
    PRE_GAME, GAME
  }

  var status = Status.PRE_GAME
  val players = mutableListOf<Player>()
  var autoStartTask: BukkitTask? = null
  var autoStartSecondsLeft: Int? = null

  fun start(plugin: AmidstUs) {
    status = Status.GAME

    val numberOfImpostors = minOf(map.maxNumberOfImpostors, players.size / 4)
    val shuffledPlayersForRoles = players.shuffled()
    val impostors = shuffledPlayersForRoles.take(numberOfImpostors)
    val crewmates = shuffledPlayersForRoles.takeLast(shuffledPlayersForRoles.size - numberOfImpostors)

    for (player in players) {
      player.bukkitPlayer.inventory.clear()
      player.bukkitPlayer.inventory.heldItemSlot = 0

      plugin.server.scoreboardManager?.mainScoreboard?.getTeam("nametagVisNever")
        ?.addEntry(player.bukkitPlayer.name)
    }

    for (impostor in impostors) {
      impostor.impostor = true

      impostor.bukkitPlayer.sendTitle(
        "${BukkitChatColor.DARK_RED}Impostor",
        null, 0, 5 * 20, 0
      )

      impostor.bukkitPlayer.spigot().sendMessage(
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
                  .map { it.bukkitPlayer.name }.joinToString(", ")
              }."
            }
          ).create()
      )
    }

    for (crewmate in crewmates) {
      crewmate.bukkitPlayer.sendTitle(
        "${ChatColor.DARK_GREEN}Crewmate",
        if (impostors.size > 1) "There are ${impostors.size} impostors amidst us!"
        else "There is 1 impostor amidst us!",
        0, 5 * 20, 0
      )

      crewmate.bukkitPlayer.spigot().sendMessage(
        *ComponentBuilder("You are a crewmate!").color(ChatColor.DARK_GREEN)
          .append(
            "Your objective is to complete tasks and uncover the " +
              "${if (impostors.size > 1) "identities of the impostors" else "identity of the impostor"}.",
            ComponentBuilder.FormatRetention.NONE
          ).create()
      )
    }

    players.shuffled().zip(map.seats.shuffled())
      .forEach { (player, location) -> player.bukkitPlayer.teleport(location) }
  }
}
