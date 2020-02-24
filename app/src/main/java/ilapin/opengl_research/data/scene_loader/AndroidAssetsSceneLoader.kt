package ilapin.opengl_research.data.scene_loader

import android.content.Context
import com.google.common.collect.HashMultimap
import com.google.gson.Gson
import ilapin.common.kotlin.safeLet
import ilapin.engine3d.GameObject
import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.meshloader.MeshLoadingRepository
import ilapin.opengl_research.*
import ilapin.opengl_research.data.assets_management.OpenGLGeometryManager
import ilapin.opengl_research.data.assets_management.OpenGLTexturesManager
import ilapin.opengl_research.domain.DisplayMetricsRepository
import ilapin.opengl_research.domain.MeshStorage
import ilapin.opengl_research.domain.engine.*
import ilapin.opengl_research.domain.scene_loader.SceneData
import ilapin.opengl_research.domain.scene_loader.SceneLoader
import ilapin.opengl_research.domain.sound.SoundClipsRepository
import ilapin.opengl_research.domain.sound.SoundScene
import ilapin.opengl_research.domain.sound_2d.SoundScene2D
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc
import org.joml.Vector4f
import java.io.BufferedReader
import kotlin.collections.set

/**
 * @author raynor on 21.01.20.
 */
class AndroidAssetsSceneLoader(
    private val context: Context,
    private val gson: Gson,
    private val meshLoadingRepository: MeshLoadingRepository,
    private val texturesManager: OpenGLTexturesManager,
    private val geometryManager: OpenGLGeometryManager,
    private val meshStorage: MeshStorage,
    private val vectorsPool: ObjectsPool<Vector3f>,
    displayMetricsRepository: DisplayMetricsRepository,
    private val openGLErrorDetector: OpenGLErrorDetector,
    private val gesturesDispatcher: GesturesDispatcher,
    private val soundClipsRepository: SoundClipsRepository,
    private val soundScene: SoundScene,
    private val soundScene2D: SoundScene2D
) : SceneLoader {

    private val pixelDensityFactor = displayMetricsRepository.getPixelDensityFactor()

    override fun loadScene(path: String): SceneData {
        var rootGameObject: GameObject? = null
        val gameObjectsMap = HashMap<String, GameObject>()

        val materialsMap = HashMap<String, MaterialComponent>()

        val layerLights = HashMultimap.create<String, GameObjectComponent>()
        val layerRenderers = HashMultimap.create<String, MeshRendererComponent>()
        val camerasMap = HashMap<String, CameraComponent>()
        val cameraAmbientLights = HashMap<CameraComponent, Vector3fc>()

        val sceneInfoDto = gson.fromJson(
            context.assets.open(path).bufferedReader().use(BufferedReader::readText),
            SceneInfoDto::class.java
        )
        val sceneScripts = (sceneInfoDto.scene?.scriptPaths ?: error("No script paths found")).map { scriptPath ->
            context
                .assets
                .open(scriptPath)
                .bufferedReader()
                .use(BufferedReader::readText)
        }

        sceneInfoDto.soundClips?.forEach { soundClip ->
            soundClipsRepository.loadSoundClip(
                soundClip.name ?: error("No sound clip name"),
                soundClip.path ?: error("No sound clip path")
            )
        }

        sceneInfoDto.textures?.forEach {
            safeLet(it.id, it.path) { id, path -> texturesManager.createTexture(id, path) }
            safeLet(it.id, it.color) { id, colorRGBA ->
                val colorARGB = IntArray(1)
                colorARGB[0] = colorRGBA.toArgb()
                texturesManager.createTexture(id, 1, 1, colorARGB)
            }
            safeLet(it.id, it.width, it.height) { id, width, height ->
                texturesManager.createTexture(id, width, height)
            }
        }

        sceneInfoDto.materials?.forEach { materialDto ->
            safeLet(materialDto.id, materialDto.diffuseColor) { id, diffuseColor ->
                materialsMap[id] = MaterialComponent(
                    materialDto.textureName,
                    Vector4f().apply { diffuseColor.toRgba(this) },
                    isDoubleSided = materialDto.isDoubleSided ?: false,
                    isWireframe = materialDto.isWireframe ?: false,
                    isUnlit = materialDto.isUnlit ?: false,
                    isTranslucent = materialDto.isTranslucent ?: false,
                    castShadows = materialDto.castShadows ?: true,
                    receiveShadows = materialDto.receiveShadows ?: true
                )
            }
        }

        sceneInfoDto.meshes?.forEach {
            safeLet(it.id, it.path) { id, path ->
                val mesh = meshLoadingRepository.loadMesh(path).toMesh()
                geometryManager.createStaticVertexBuffer(id, mesh.verticesAsArray())
                geometryManager.createStaticIndexBuffer(id, mesh.indices.toShortArray())
                if (it.keepInStorage == true) {
                    meshStorage.putMesh(id, mesh)
                }
            }
        }

        sceneInfoDto.scene.gameObjects?.forEach { gameObjectDto ->
            val gameObjectName = gameObjectDto.name ?: throw IllegalArgumentException("No game object name")
            val gameObject = GameObject(gameObjectName)

            val positionDto = gameObjectDto.position ?: throw IllegalArgumentException("Position not found for game object $gameObjectName")
            val rotationDto = gameObjectDto.rotation ?: throw IllegalArgumentException("Rotation not found for game object $gameObjectName")
            val scaleDto = gameObjectDto.scale ?: throw IllegalArgumentException("Scale not found for game object $gameObjectName")
            gameObject.addComponent(TransformationComponent(
                Vector3f(positionDto[0], positionDto[1], positionDto[2]),
                Quaternionf().identity().rotationXYZ(
                    Math.toRadians(rotationDto[0].toDouble()).toFloat(),
                    Math.toRadians(rotationDto[1].toDouble()).toFloat(),
                    Math.toRadians(rotationDto[2].toDouble()).toFloat()
                ),
                Vector3f(scaleDto[0], scaleDto[1], scaleDto[2])
            ))

            gameObjectDto.components?.forEach {
                when (it) {
                    is ComponentDto.DirectionalLightDto -> {
                        it.color ?: error("No directional light color found for game object $gameObjectName")
                        it.layerNames ?: error("No directional light layer names found for game object $gameObjectName")
                        val lightComponent =
                            DirectionalLightComponent(Vector3f().apply {
                                it.color.toRgb(this)
                            })

                        val halfShadowSize = GLOBAL_DIRECTIONAL_LIGHT_SHADOW_SIZE / 2
                        val cameraComponent =
                            DirectionalLightShadowMapCameraComponent(
                                vectorsPool,
                                GLOBAL_DIRECTIONAL_LIGHT_DISTANCE_FROM_VIEWER,
                                -halfShadowSize,
                                halfShadowSize,
                                -halfShadowSize,
                                halfShadowSize
                            )
                        cameraComponent.zNear = 1f
                        cameraComponent.zFar = 2 * GLOBAL_DIRECTIONAL_LIGHT_DISTANCE_FROM_VIEWER

                        gameObject.addComponent(cameraComponent)
                        gameObject.addComponent(lightComponent)

                        it.layerNames.forEach { layerName -> layerLights[layerName] += lightComponent }
                    }

                    is ComponentDto.MeshDto -> {
                        it.layerNames ?: error("No layer names")
                        it.materialName ?: error("No material name")
                        it.meshName ?: error("No mesh name")
                        val renderer = MeshRendererComponent(
                            pixelDensityFactor,
                            texturesManager,
                            geometryManager,
                            openGLErrorDetector
                        )
                        it.layerNames.forEach { layerName -> layerRenderers[layerName] += renderer }
                        gameObject.addComponent(renderer)
                        gameObject.addComponent(materialsMap[it.materialName] ?: error("Material ${it.materialName} not found"))
                        gameObject.addComponent(MeshComponent(it.meshName))
                    }

                    is ComponentDto.PerspectiveCameraDto -> {
                        it.fov ?: error("No FOV")
                        it.layerNames ?: error("No layer names")
                        it.ambientLight ?: error("No camera ambient light")
                        val cameraComponent =
                            PerspectiveCameraComponent(
                                vectorsPool,
                                it.fov,
                                it.layerNames
                            ).apply {
                                zNear = it.zNear ?: Z_NEAR
                                zFar = it.zFar ?: Z_FAR
                                viewportX = it.viewportX ?: 0f
                                viewportY = it.viewportY ?: 0f
                                viewportWidth = it.viewportWidth ?: 1f
                                viewportHeight = it.viewportHeight ?: 1f
                            }
                        gameObject.addComponent(cameraComponent)
                        camerasMap[gameObjectName] = cameraComponent

                        cameraAmbientLights[cameraComponent] = Vector3f().apply { it.ambientLight.toRgb(this) }
                    }

                    is ComponentDto.OrthoCameraDto -> {
                        it.layerNames ?: error("No layer names")
                        it.left ?: error("No left camera boundary")
                        it.right ?: error("No right camera boundary")
                        it.bottom ?: error("No bottom camera boundary")
                        it.top ?: error("No top camera boundary")
                        it.ambientLight ?: error("No camera ambient light")
                        val cameraComponent =
                            OrthoCameraComponent(
                                vectorsPool,
                                it.left,
                                it.right,
                                it.bottom,
                                it.top,
                                it.layerNames
                            ).apply {
                                zNear = it.zNear ?: Z_NEAR
                                zFar = it.zFar ?: Z_FAR
                                viewportX = it.viewportX ?: 0f
                                viewportY = it.viewportY ?: 0f
                                viewportWidth = it.viewportWidth ?: 1f
                                viewportHeight = it.viewportHeight ?: 1f
                            }
                        gameObject.addComponent(cameraComponent)
                        camerasMap[gameObjectName] = cameraComponent

                        cameraAmbientLights[cameraComponent] = Vector3f().apply { it.ambientLight.toRgb(this) }
                    }

                    is ComponentDto.GestureConsumerDto -> {
                        it.priority ?: error("No gesture consumer priority value")
                        it.left ?: error("No gesture consumer left value")
                        it.right ?: error("No gesture consumer right value")
                        it.bottom ?: error("No gesture consumer bottom value")
                        it.top ?: error("No gesture consumer top value")
                        val component = GestureConsumerComponent(
                            it.priority,
                            it.left,
                            it.top,
                            it.right,
                            it.bottom
                        )
                        gesturesDispatcher.addGestureConsumer(component)
                        gameObject.addComponent(component)
                    }

                    is ComponentDto.SoundPlayer2DDto -> {
                        gameObject.addComponent(SoundPlayer2DComponent(
                            soundScene2D,
                            it.playerName ?: error("No player name"),
                            it.soundClipName ?: error("No sound clip name"),
                            it.duration ?: error("No duration"),
                            it.volume ?: error("No volume")
                        ))
                    }

                    is ComponentDto.SoundPlayer3DDto -> {
                        gameObject.addComponent(SoundPlayer3DComponent(
                            soundScene,
                            it.playerName ?: error("No player name"),
                            it.soundClipName ?: error("No sound clip name"),
                            it.duration ?: error("No duration"),
                            it.maxVolumeDistance ?: error("No max volume distance"),
                            it.minVolumeDistance ?: error("No min volume distance"),
                            it.volume ?: error("No volume")
                        ))
                    }

                    is ComponentDto.SoundListenerDto -> gameObject.addComponent(SoundListenerComponent(soundScene))
                }
            }

            if (gameObjectName == ROOT_GAME_OBJECT_NAME) {
                rootGameObject = gameObject
            } else {
                val parentName = gameObjectDto.parent ?: throw IllegalArgumentException("No parent game object name for game object $gameObjectName")
                val parentGameObject = gameObjectsMap[parentName] ?: throw IllegalArgumentException("Unknown parent game object $parentName")
                parentGameObject.addChild(gameObject)
            }

            if (gameObjectsMap.containsKey(gameObjectName)) {
                throw IllegalArgumentException("Duplicate game object $gameObjectName")
            }
            gameObjectsMap[gameObjectName] = gameObject
        }

        sceneInfoDto.scene.activeCameras ?: error("No active camera names found")

        return rootGameObject?.let {
            // To mark all transformation components dirty
            it.getComponent(TransformationComponent::class.java)?.position = Vector3f()

            SceneData(
                sceneScripts,
                it,
                camerasMap.filter { entry -> sceneInfoDto.scene.activeCameras.contains(entry.key) }.values.toList(),
                layerRenderers,
                layerLights,
                cameraAmbientLights,
                emptyList()
            )
        } ?: error("No root game object")
    }

    companion object {

        private const val ROOT_GAME_OBJECT_NAME = "root"
    }
}