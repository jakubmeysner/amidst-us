@file:UseSerializers(LocationSerializer::class)

package me.jakubmeysner.amidstus.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.jakubmeysner.amidstus.serializers.LocationSerializer
import org.bukkit.Location

@Serializable
class Map(var name: String) {
  var displayName: String = name
  var postGameLocation: Location? = null
  var preGameLocation: Location? = null
  val maxNumberOfPlayers: Int = 10

  val playable: Boolean
    get() = listOf(
      postGameLocation != null,
      preGameLocation != null,
      maxNumberOfPlayers >= 3,
    ).all { it }
}
