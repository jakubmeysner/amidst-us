package me.jakubmeysner.amidstus.models

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import me.jakubmeysner.amidstus.AmidstUs
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Material
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import java.time.Instant
import org.bukkit.ChatColor as BukkitChatColor

class EmergencyMeeting(val plugin: AmidstUs, val game: Game, val source: Source, val calledBy: Player) {
  companion object {
    val VoteItemStack = ItemStack(Material.COMMAND_BLOCK).apply {
      itemMeta = itemMeta?.apply {
        setDisplayName("${BukkitChatColor.GREEN}Vote")
      }
    }
  }

  enum class Source {
    REPORT, CALLED
  }

  enum class Phase {
    STARTED, DISCUSSION, VOTING, RESULT, ENDED
  }

  var phase = Phase.STARTED
  val recentlyKilled = mutableListOf<Player>()
  val votes = mutableMapOf<Player, Player?>()

  var bossBar: BossBar? = null
  var countDownSeconds: Int? = null
  var countDownTask: BukkitTask? = null

  var endedAt: Instant? = null

  init {
    recentlyKilled.addAll(
      game.players.filter { it.dead }.filter {
        game.emergencyMeetings.none { emergencyMeeting ->
          emergencyMeeting.recentlyKilled.any { recentlyKilledPlayer -> recentlyKilledPlayer == it }
        }
      }
    )

    for ((player, seat) in game.players.zip(game.map.seats.shuffled())) {
      player.bukkit.inventory.clear()

      for (itPlayer in game.players) {
        if (itPlayer.fakeEntityId != null) {
          plugin.protocolManager.sendServerPacket(player.bukkit,
            PacketContainer(PacketType.Play.Server.ENTITY_DESTROY).apply {
              integers.write(0, itPlayer.fakeEntityId)
            }
          )
        }

        if (player.dead) {
          player.bukkit.showPlayer(plugin, itPlayer.bukkit)
        } else {
          if (itPlayer.dead) {
            player.bukkit.hidePlayer(plugin, itPlayer.bukkit)
          } else {
            player.bukkit.showPlayer(plugin, itPlayer.bukkit)
          }
        }
      }

      player.bukkit.teleport(seat)

      player.bukkit.sendTitle(
        "${BukkitChatColor.RED}${if (source == Source.REPORT) "Dead body reported" else "Emergency meeting"}",
        "Called by ${calledBy.bukkit.displayName}",
        0,
        3 * 20,
        0
      )

      player.bukkit.spigot().sendMessage(
        *(if (recentlyKilled.isEmpty()) {
          ComponentBuilder("No players were killed since the last emergency meeting.")
            .color(ChatColor.GREEN).create()
        } else {
          ComponentBuilder("The following players have been killed since the last emergency meeting: " +
            recentlyKilled.joinToString { it.bukkit.displayName }).color(ChatColor.RED).create()
        })
      )
    }

    for (player in game.players) {
      player.fakeEntityId = null
    }

    plugin.server.scheduler.runTaskLater(plugin, Runnable {
      phase = Phase.DISCUSSION
      countDownSeconds = game.discussionTimeSeconds

      bossBar = plugin.server.createBossBar(
        "${BukkitChatColor.YELLOW}Discussion time: ${countDownSeconds}s left",
        BarColor.YELLOW,
        BarStyle.SOLID
      ).apply {
        game.players.forEach { addPlayer(it.bukkit) }
        progress = (game.discussionTimeSeconds - countDownSeconds!!).toDouble() / game.discussionTimeSeconds
      }

      countDownTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
        countDownSeconds = countDownSeconds!! - 1

        if (countDownSeconds == 0) {
          bossBar?.removeAll()
          bossBar = null
          countDownTask?.cancel()
          countDownTask = null

          phase = Phase.VOTING

          for (player in game.players.filter { !it.dead }) {
            player.bukkit.inventory.setItem(0, VoteItemStack)
          }

          countDownSeconds = game.votingTimeSeconds

          bossBar = plugin.server.createBossBar(
            "${BukkitChatColor.RED}Voting time: ${countDownSeconds}s left",
            BarColor.RED,
            BarStyle.SOLID
          ).apply {
            game.players.forEach { addPlayer(it.bukkit) }
            progress = (game.votingTimeSeconds - countDownSeconds!!).toDouble() / game.votingTimeSeconds
          }

          countDownTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
            countDownSeconds = countDownSeconds!! - 1

            if (countDownSeconds == 0 || votes.size == game.players.count { !it.dead }) {
              bossBar?.removeAll()
              bossBar = null
              countDownTask?.cancel()
              countDownTask = null

              for (player in game.players) {
                player.bukkit.inventory.clear()
              }

              phase = Phase.RESULT

              val votesCount = votes.values.groupingBy { it }.eachCount().entries.sortedBy { it.value }

              if (votesCount.size > 1 && votesCount[0].value == votesCount[1].value) {
                for (player in game.players) {
                  player.bukkit.sendTitle(
                    "${BukkitChatColor.YELLOW}Tie",
                    null,
                    0,
                    5 * 20,
                    0
                  )
                }
              } else if (votesCount.isEmpty() || votesCount[0].key == null) {
                for (player in game.players) {
                  player.bukkit.sendTitle(
                    "${BukkitChatColor.YELLOW}Skipped",
                    null,
                    0,
                    5 * 20,
                    0
                  )
                }
              } else {
                votesCount[0].key?.let { ejected ->
                  ejected.dead = true
                  recentlyKilled.add(ejected)

                  for (player in game.players) {
                    ejected.bukkit.showPlayer(plugin, player.bukkit)

                    if (!player.dead) {
                      player.bukkit.hidePlayer(plugin, ejected.bukkit)
                    }

                    player.bukkit.sendTitle(
                      "${BukkitChatColor.RED}${ejected.bukkit.name} " + if (game.confirmEjects) {
                        if (game.players.count { it.impostor } == 1) {
                          if (ejected.impostor) {
                            "was the impostor"
                          } else {
                            "was not the impostor"
                          }
                        } else {
                          if (ejected.impostor) {
                            "was an impostor"
                          } else {
                            "was not an impostor"
                          }
                        }
                      } else {
                        "was ejected"
                      },
                      null,
                      0,
                      5 * 20,
                      0
                    )
                  }
                }
              }

              plugin.server.scheduler.runTaskLater(plugin, Runnable {
                phase = Phase.ENDED

                for (player in game.players) {
                  if (player.impostor) {
                    player.killCooldownActive = true
                    player.killCooldownSecondsLeft = game.killCooldownSeconds
                    player.bukkit.inventory.heldItemSlot = 0

                    player.killCooldownTask = plugin.server.scheduler.runTaskTimer(plugin, Runnable {
                      if (player.killCooldownSecondsLeft == 0) {
                        player.killCooldownActive = false
                        player.killCooldownSecondsLeft = null
                        player.killCooldownTask?.cancel()
                        player.killCooldownTask = null
                        player.bukkit.inventory.setItem(1, Player.ImpostorSwordItemStack)
                      } else {
                        player.bukkit.inventory.setItem(1, Player.ImpostorSwordItemStack.clone().apply {
                          itemMeta = itemMeta?.clone()?.apply {
                            setDisplayName("${org.bukkit.ChatColor.DARK_RED}Impostor sword (${player.killCooldownSecondsLeft}s cooldown)")
                          }
                        })

                        player.killCooldownSecondsLeft = player.killCooldownSecondsLeft!! - 1
                      }
                    }, 0, 20)
                  }
                }

                if (
                  game.players.none { !it.dead && it.impostor } ||
                  game.players.count { !it.dead && it.impostor } >= game.players.count { !it.dead && !it.impostor }
                ) {
                  game.end(plugin)
                } else {
                  endedAt = Instant.now()
                }
              }, 5 * 20)
            } else {
              bossBar?.setTitle("${BukkitChatColor.RED}Voting time: ${countDownSeconds}s left")
              bossBar?.progress = (game.votingTimeSeconds - countDownSeconds!!).toDouble() / game.votingTimeSeconds
            }
          }, 20, 20)
        } else {
          bossBar?.setTitle("${BukkitChatColor.YELLOW}Discussion time: ${countDownSeconds}s left")
          bossBar?.progress = (game.discussionTimeSeconds - countDownSeconds!!).toDouble() / game.discussionTimeSeconds
        }
      }, 20, 20)
    }, 3 * 20)
  }
}
