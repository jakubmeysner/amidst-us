package me.jakubmeysner.amidstus.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.*

object LocationSerializer : KSerializer<Location> {
  override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Location", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: Location) {
    return encoder.encodeString(
      "${value.world?.uid};${value.x};${value.y};${value.z};${value.yaw};${value.pitch}"
    )
  }

  override fun deserialize(decoder: Decoder): Location {
    val args = decoder.decodeString().split(";")

    if (args.size != 6) {
      error("Invalid location")
    }

    return Location(
      Bukkit.getWorld(UUID.fromString(args[0])),
      args[1].toDouble(),
      args[2].toDouble(),
      args[3].toDouble(),
      args[4].toFloat(),
      args[5].toFloat()
    )
  }
}
