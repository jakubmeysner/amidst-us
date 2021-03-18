package me.jakubmeysner.amidstus

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.jakubmeysner.amidstus.commands.games.*
import me.jakubmeysner.amidstus.commands.maps.*
import me.jakubmeysner.amidstus.listeners.*
import me.jakubmeysner.amidstus.models.Game
import me.jakubmeysner.amidstus.models.Map
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Team
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class AmidstUs : JavaPlugin() {
  lateinit var protocolManager: ProtocolManager

  val maps = mutableListOf<Map>()
  val games = mutableListOf<Game>()

  private val mapsFile = File(dataFolder, "maps.json")

  override fun onEnable() {
    protocolManager = ProtocolLibrary.getProtocolManager()

    val commands = listOf(
      CreateMapCommand(this),
      DeleteMapCommand(this),
      LoadMapsCommand(this),
      MapCommand(this),
      MapsCommand(this),
      SaveMapsCommand(this),
      PlayCommand(this),
      CreateGameCommand(this),
      LeaveGameCommand(this),
      InviteCommand(this),
      AcceptInviteCommand(this),
      DenyInviteCommand(this),
      AddMapSeatCommand(this),
      RemoveMapSeatCommand(this),
      MapSeatsCommand(this),
      SwitchMapCommand(this),
      StartGameCommand(this),
      MapOptionsCommand(this),
      GameOptionsCommand(this),
      VoteCommand(this),
    )

    for (command in commands) {
      this.getCommand(command.name)?.setExecutor(command)
        ?: error("Could not register ${command::class.simpleName}!")
    }

    val listeners = listOf(
      PlayerJoinListener(this),
      PlayerQuitListener(this),
      EntityDamageByEntityListener(this),
      AsyncPlayerChatListener(this),
      InventoryClickListener(this),
      PlayerDropItemListener(this),
      PlayerInteractListener(this),
      PlayerSwapHandItemsListener(this),
      PlayerMoveListener(this),
    )

    for (listener in listeners) {
      this.server.pluginManager.registerEvents(listener, this)
    }

    if (server.scoreboardManager?.mainScoreboard?.getTeam("nametagVisNever") == null) {
      server.scoreboardManager?.mainScoreboard?.registerNewTeam("nametagVisNever").let {
        it?.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER)
      }
    }

    if (!dataFolder.exists()) {
      Files.createDirectory(Paths.get(dataFolder.path))
    }

    if (!mapsFile.exists()) {
      mapsFile.createNewFile()
      mapsFile.writeText("[]")
    }

    loadMaps()
  }

  override fun onDisable() {
    for (game in games) {
      for (player in game.players) {
        player.bukkit.inventory.clear()
        player.bukkit.resetPlayerTime()
        server.scoreboardManager?.mainScoreboard?.getTeam("nametagVisNever")?.removeEntry(player.bukkit.name)
        player.bukkit.teleport(game.map.postGameLocation ?: return)
      }
    }

    saveMaps()
  }

  fun loadMaps() {
    maps.clear()
    maps.addAll(Json.decodeFromString<List<Map>>(mapsFile.readText()))
  }

  fun saveMaps() {
    mapsFile.writeText(Json.encodeToString(maps))
  }
}
