package me.jakubmeysner.amidstus.commands.games

import me.jakubmeysner.amidstus.AmidstUs
import me.jakubmeysner.amidstus.interfaces.Named
import me.jakubmeysner.amidstus.models.EmergencyMeeting
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.ChatColor as BukkitChatColor

class VoteCommand(val plugin: AmidstUs) : TabExecutor, Named {
    companion object {
        val voteInventoryTitle = "${BukkitChatColor.YELLOW}Vote"
    }

    override val name = "vote"

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size > 1) {
            sender.spigot().sendMessage(
                *ComponentBuilder("Usage: /vote [player name]").color(ChatColor.RED).create()
            )
        } else if (sender !is Player) {
            sender.spigot().sendMessage(
                *ComponentBuilder("This command can only be used by players!").color(ChatColor.RED).create()
            )
        } else {
            val game = plugin.games.find { it.players.any { it.bukkit == sender } }

            if (game == null) {
                sender.spigot().sendMessage(
                    *ComponentBuilder("You are not in game!").color(ChatColor.RED).create()
                )
            } else {
                val player = game.players.find { it.bukkit == sender }!!
                val emergencyMeeting = game.emergencyMeetings.lastOrNull()

                if (player.dead) {
                    sender.spigot().sendMessage(
                        *ComponentBuilder("You can't vote because you are dead!").color(ChatColor.RED).create()
                    )
                } else if (emergencyMeeting == null || emergencyMeeting.phase != EmergencyMeeting.Phase.VOTING) {
                    sender.spigot().sendMessage(
                        *ComponentBuilder("No emergency meeting is currently in voting phase!").color(ChatColor.RED)
                            .create()
                    )
                } else {
                    if (args.isEmpty()) {
                        player.bukkit.openInventory(
                            Bukkit.createInventory(null, 27, voteInventoryTitle).apply {
                                for ((itPlayer, index) in game.players.zip(0..24)) {
                                    setItem(index, ItemStack(Material.PLAYER_HEAD).apply {
                                        if (itemMeta is SkullMeta) {
                                            itemMeta = (itemMeta as SkullMeta).apply {
                                                setDisplayName(
                                                    "" + BukkitChatColor.RESET +
                                                        if (emergencyMeeting.votes[player] == itPlayer) BukkitChatColor.GREEN else {
                                                            BukkitChatColor.WHITE
                                                        } +
                                                        when {
                                                            emergencyMeeting.votes.containsKey(itPlayer) -> BukkitChatColor.ITALIC
                                                            itPlayer.dead -> BukkitChatColor.STRIKETHROUGH
                                                            else -> ""
                                                        } + itPlayer.bukkit.name
                                                )
                                                owningPlayer = itPlayer.bukkit
                                            }
                                        }
                                    })
                                }

                                setItem(26, ItemStack(Material.BEDROCK).apply {
                                    itemMeta = itemMeta?.apply {
                                        setDisplayName(
                                            "" + if (
                                                emergencyMeeting.votes.containsKey(player)
                                                && emergencyMeeting.votes[player] == null
                                            )
                                                BukkitChatColor.GREEN
                                            else {
                                                BukkitChatColor.GRAY
                                            } +
                                                "Skip"
                                        )
                                    }
                                })
                            }
                        )
                    } else {
                        if (emergencyMeeting.votes.containsKey(player)) {
                            sender.spigot().sendMessage(
                                *ComponentBuilder("You have already voted!").color(ChatColor.RED).create()
                            )
                        } else if (args[0].toLowerCase() == "skip") {
                            emergencyMeeting.votes[player] = null

                            sender.spigot().sendMessage(
                                *ComponentBuilder("You have voted to skip.").color(ChatColor.GREEN).create()
                            )
                        } else {
                            val votePlayer = game.players.find { it.bukkit.name == args[0] }

                            if (votePlayer == null) {
                                sender.spigot().sendMessage(
                                    *ComponentBuilder("There is no player with given name!").color(ChatColor.RED)
                                        .create()
                                )
                            } else if (votePlayer.dead) {
                                sender.spigot().sendMessage(
                                    *ComponentBuilder("This player is already dead!").color(ChatColor.RED).create()
                                )
                            } else {
                                emergencyMeeting.votes[player] = votePlayer

                                sender.spigot().sendMessage(
                                    *ComponentBuilder("You have voted to eject ${votePlayer.bukkit.name}.")
                                        .color(ChatColor.GREEN).create()
                                )
                            }
                        }
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
            1 -> plugin.games.find {
                it.players.any { it.bukkit == sender && !it.dead } &&
                    it.emergencyMeetings.isNotEmpty() &&
                    it.emergencyMeetings.last().phase == EmergencyMeeting.Phase.VOTING &&
                    !it.emergencyMeetings.last().votes.containsKey(it.players.find { it.bukkit == sender })
            }?.players?.filter { !it.dead }?.map { it.bukkit.name } ?: listOf()

            else -> listOf()
        }
    }
}
