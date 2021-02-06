package me.jakubmeysner.amidstus

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.jakubmeysner.amidstus.commands.games.*
import me.jakubmeysner.amidstus.commands.maps.*
import me.jakubmeysner.amidstus.listeners.InventoryListener
import me.jakubmeysner.amidstus.listeners.PlayerJoinListener
import me.jakubmeysner.amidstus.listeners.PlayerQuitListener
import me.jakubmeysner.amidstus.models.Game
import me.jakubmeysner.amidstus.models.Map
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Team
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class AmidstUs : JavaPlugin() {
  val maps = mutableListOf<Map>()
  val games = mutableListOf<Game>()

  private val mapsFile = File(dataFolder, "maps.json")

  override fun onEnable() {
    val commands = listOf(
      CreateMapCommand(this),
      DeleteMapCommand(this),
      LoadMapsCommand(this),
      MapCommand(this),
      MapsCommand(this),
      RenameMapCommand(this),
      SaveMapsCommand(this),
      SetMapDisplayNameCommand(this),
      SetMapPostGameLocationCommand(this),
      SetMapPreGameLocationCommand(this),
      PlayCommand(this),
      CreateGameCommand(this),
      LeaveGameCommand(this),
      InviteCommand(this),
      AcceptInviteCommand(this),
      DenyInviteCommand(this),
      SetMapMinPlayersCommand(this),
      SetMapMaxPlayersCommand(this),
      SetMapMaxImpostorsCommand(this),
      AddMapSeatCommand(this),
      RemoveMapSeatCommand(this),
      MapSeatsCommand(this),
      SetMapAutoStartPlayers(this),
      SwitchMapCommand(this),
      StartGameCommand(this),
    )

    for (command in commands) {
      this.getCommand(command.name)?.setExecutor(command)
        ?: error("Could not register ${command::class.simpleName}!")
    }

    val listeners = listOf(
      PlayerJoinListener(this),
      PlayerQuitListener(this),
      InventoryListener(this),
    )

    for (listener in listeners) {
      this.server.pluginManager.registerEvents(listener, this)
    }

    if (server.scoreboardManager?.mainScoreboard?.getTeam("nametagVisibilityNever") == null) {
      server.scoreboardManager?.mainScoreboard?.registerNewTeam("nametagVisibilityNever").let {
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
