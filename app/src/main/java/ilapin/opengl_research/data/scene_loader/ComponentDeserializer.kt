package ilapin.opengl_research.data.scene_loader

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

/**
 * @author raynor on 21.01.20.
 */
class ComponentDeserializer : JsonDeserializer<ComponentDto> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ComponentDto? {
        val jsonObject = json.asJsonObject
        return jsonObject["type"]?.let {
            when (it.asString) {
                "DirectionalLight" -> context.deserialize(jsonObject, ComponentDto.DirectionalLightDto::class.java)
                "Mesh" -> context.deserialize(jsonObject, ComponentDto.MeshDto::class.java)
                "PerspectiveCamera" -> context.deserialize(jsonObject, ComponentDto.PerspectiveCameraDto::class.java)
                "OrthoCamera" -> context.deserialize(jsonObject, ComponentDto.OrthoCameraDto::class.java)
                "GestureConsumer" -> context.deserialize(jsonObject, ComponentDto.GestureConsumerDto::class.java)
                else -> null
            }
        }
    }
}