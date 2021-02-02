package me.jakubmeysner.amidstus

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.jakubmeysner.amidstus.commands.games.*
import me.jakubmeysner.amidstus.commands.maps.*
import me.jakubmeysner.amidstus.listeners.PlayerQuitListener
import me.jakubmeysner.amidstus.models.Game
import me.jakubmeysner.amidstus.models.Map
import org.bukkit.plugin.java.JavaPlugin
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
    )

    for (command in commands) {
      this.getCommand(command.name)?.setExecutor(command)
        ?: error("Could not register ${command::class.simpleName}!")
    }

    val listeners = listOf(
      PlayerQuitListener(this),
    )

    for (listener in listeners) {
      this.server.pluginManager.registerEvents(listener, this)
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
