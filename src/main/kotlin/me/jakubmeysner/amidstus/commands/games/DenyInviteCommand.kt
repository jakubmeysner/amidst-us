package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class DenyInviteCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "denyinvite"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (args.size != 1) {
      sender.spigot().sendMessage(
        *ComponentBuilder("Usage: /acceptinvite <player name>").color(ChatColor.RED).create()
      )
    } else if (sender !is Player) {
      sender.spigot().sendMessage(
        *ComponentBuilder("This command can only be used by players!").color(ChatColor.RED).create()
      )
    } else if (plugin.games.any { it.players.any { it.bukkitPlayer == sender && !it.pending } }) {
      sender.spigot().sendMessage(
        *ComponentBuilder("You are already in game!").color(ChatColor.RED).create()
      )
    } else {
      val inviter = plugin.server.onlinePlayers.find { it.name == args[0] }

      if (inviter == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("Could not find any player with this name!").color(ChatColor.RED).create()
        )
      } else {
        val game = plugin.games.find { it.players.any { it.bukkitPlayer == inviter && it.promoted } }

        if (game == null) {
          sender.spigot().sendMessage(
            *ComponentBuilder("This player is not currently promoted in any game!").color(ChatColor.RED).create()
          )
        } else {
          val player = game.players.find { it.bukkitPlayer == sender && it.pending }

          if (player == null) {
            sender.spigot().sendMessage(
              *ComponentBuilder("You have not been invited by this player!").color(ChatColor.RED).create()
            )
          } else {
            player.leaveGame(game, plugin)

            sender.spigot().sendMessage(
              *ComponentBuilder("You have denied ${inviter.name}'s request to join their game.")
                .color(ChatColor.DARK_RED).create()
            )

            inviter.spigot().sendMessage(
              *ComponentBuilder("Player ${sender.name} has denied your invite!").color(ChatColor.DARK_RED).create()
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
      1 -> plugin.games.filter { it.players.any { it.bukkitPlayer == sender && it.pending } }.map { it.players }
        .flatten().filter { it.promoted }.map { it.bukkitPlayer.name }
      else -> listOf()
    }
  }
}
