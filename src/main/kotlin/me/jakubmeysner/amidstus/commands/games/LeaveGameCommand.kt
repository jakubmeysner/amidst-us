package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player as BukkitPlayer

class LeaveGameCommand(val plugin: AmidstUs) : TabExecutor, Named {
  override val name = "leavegame"

  override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
    if (sender !is BukkitPlayer) {
      sender.spigot().sendMessage(
        *ComponentBuilder("This command can only be used by players!").color(ChatColor.RED).create()
      )
    } else {
      val game = plugin.games.find { it.players.any { it.bukkit == sender } }

      if (game == null) {
        sender.spigot().sendMessage(
          *ComponentBuilder("You aren't in game!").color(ChatColor.RED).create()
        )
      } else {
        val player = game.players.find { it.bukkit == sender }!!
        player.leaveGame(game, plugin)

        sender.spigot().sendMessage(
          *ComponentBuilder("Left the game!").color(ChatColor.GREEN).create()
        )
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
    return listOf()
  }
}
