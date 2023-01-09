package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import me.jakubmeysner.amidstus.models.Game
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class StartGameCommand(val plugin: AmidstUs) : TabExecutor, Named {
    override val name = "startgame"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            sender.spigot().sendMessage(
                *ComponentBuilder("Usage: /startgame").color(ChatColor.RED).create()
            )
        } else if (sender !is Player) {
            sender.spigot().sendMessage(
                *ComponentBuilder("This command may only be used by players!").color(ChatColor.RED).create()
            )
        } else {
            val game = plugin.games.find { it.players.any { it.bukkit == sender } }

            if (game == null) {
                sender.spigot().sendMessage(
                    *ComponentBuilder("You are not in game!").color(ChatColor.RED).create()
                )
            } else if (game.status == Game.Status.IN_PROGRESS) {
                sender.spigot().sendMessage(
                    *ComponentBuilder("The game has already started!").color(ChatColor.RED).create()
                )
            } else {
                val player = game.players.find { it.bukkit == sender }!!

                if (!player.promoted) {
                    sender.spigot().sendMessage(
                        *ComponentBuilder("You must be promoted to use this command!").color(ChatColor.RED).create()
                    )
                } else {
                    sender.spigot().sendMessage(
                        *ComponentBuilder("Starting the game...").color(ChatColor.GREEN).create()
                    )
                    game.start(plugin)
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
        return listOf()
    }
}
