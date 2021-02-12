package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import me.jakubmeysner.amidstus.models.Game
import me.jakubmeysner.amidstus.models.Player
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player as BukkitPlayer

class InviteCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "invite"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /invite <player name>").color(ChatColor.RED).create()
      )
    } else if (sender !is BukkitPlayer) {
      sender.spigot().sendMessage(
        *ComponentBuilder("This command can only be used by players!").color(ChatColor.RED).create()
      )
    } else {
      val bukkitPlayer = plugin.server.onlinePlayers.find { it.name == args[0] }

      if (bukkitPlayer == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any online player with the provided name!")
            .color(ChatColor.RED).create()
        )
      } else if (bukkitPlayer == sender) {
        sender.spigot().sendMessage(
          *ComponentBuilder("You can not invite yourself to a game!").color(ChatColor.RED).create()
        )
      } else if (plugin.games.any { it.players.any { it.bukkit == bukkitPlayer && !it.pending } }) {
        sender.spigot().sendMessage(
          *ComponentBuilder("This player is currently in game!").color(ChatColor.RED).create()
        )
      } else {
        val game = plugin.games.find { it.players.any { it.bukkit == sender } }

        if (game == null) {
          sender.spigot().sendMessage(
            *ComponentBuilder("You are not in game at the moment!").color(ChatColor.RED).create()
          )
        } else if (game.status == Game.Status.IN_PROGRESS) {
          sender.spigot().sendMessage(
            *ComponentBuilder("The game has already started!").color(ChatColor.RED).create()
          )
        } else if (game.players.any { it.bukkit == bukkitPlayer }) {
          sender.spigot().sendMessage(
            *ComponentBuilder("This player has already been invited to this game!").color(ChatColor.RED).create()
          )
        } else {
          val senderPlayer = game.players.find { it.bukkit == sender }!!

          if (!senderPlayer.promoted) {
            sender.spigot().sendMessage(
              *ComponentBuilder("To be able to invite players you need to be promoted!")
                .color(ChatColor.RED).create()
            )
          } else if (game.players.size == game.map.maxNumberOfPlayers) {
            sender.spigot().sendMessage(
              *ComponentBuilder("Can't invite any more players to this game!").color(ChatColor.RED).create()
            )
          } else {
            val player = Player(bukkitPlayer, true)
            game.players.add(player)

            sender.spigot().sendMessage(
              *ComponentBuilder("You have invited ${bukkitPlayer.name} to the game!")
                .color(ChatColor.GREEN).create()
            )

            bukkitPlayer.spigot().sendMessage(
              *ComponentBuilder(
                "You have been invited by ${sender.name} to join a game on map ${game.map.displayName}!\n"
              )
                .append("[Accept]").color(ChatColor.GREEN)
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/acceptinvite ${sender.name}"))
                .append(" ")
                .append("[Deny]").color(ChatColor.RED)
                .event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/denyinvite ${sender.name}"))
                .create()
            )
          }
        }
      }
    }

    return true
  }

  override fun onTabComplete(
    sender: CommandSender,
    command: Command,
    alias: String,
    args: Array<out String>
  ): List<String> {
    return when (args.size) {
      1 -> plugin.server.onlinePlayers.filter {
        it != sender &&
          plugin.games.none { game -> game.players.any { player -> player.bukkit == it && !player.pending } }
      }.map { it.name }.filter { it.startsWith(args[0]) }
      else -> listOf()
    }
  }
}
