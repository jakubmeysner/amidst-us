@file:UseSerializers(LocationSerializer::class)
package me.jakubmeysner.amidstus.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import me.jakubmeysner.amidstus.serializers.LocationSerializer
import org.bukkit.Location
import java.util.*

@Serializable
class Vent(val location: Location) {
  val id = UUID.randomUUID().toString()
  var linkedVents = mutableListOf<Vent>()
}
