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
            context.deserialize(
                jsonObject,
                when (it.asString) {
                    "DirectionalLight" -> ComponentDto.DirectionalLightDto::class.java
                    "Mesh" -> ComponentDto.MeshDto::class.java
                    "PerspectiveCamera" -> ComponentDto.PerspectiveCameraDto::class.java
                    "OrthoCamera" -> ComponentDto.OrthoCameraDto::class.java
                    "GestureConsumer" -> ComponentDto.GestureConsumerDto::class.java
                    "SoundPlayer3D" -> ComponentDto.SoundPlayer3DDto::class.java
                    "SoundPlayer2D" -> ComponentDto.SoundPlayer2DDto::class.java
                    "SoundListener" -> ComponentDto.SoundListenerDto::class.java
                    "PlayerCapsuleRigidBody" -> ComponentDto.PlayerCapsuleRigidBodyDto::class.java
                    "TriMeshRigidBody" -> ComponentDto.TriMeshRigidBodyDto::class.java
                    "BoxRigidBody" -> ComponentDto.BoxRigidBodyDto::class.java
                    "Text" -> ComponentDto.TextDto::class.java
                    else -> null
                }
            )
        }
    }
}