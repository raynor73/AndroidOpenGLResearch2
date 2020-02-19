package ilapin.opengl_research.ui

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import ilapin.common.kotlin.safeLet
import ilapin.common.messagequeue.MessageQueue
import ilapin.common.time.TimeRepository
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.*
import ilapin.opengl_research.data.assets_management.FrameBuffersManager
import ilapin.opengl_research.data.assets_management.OpenGLGeometryManager
import ilapin.opengl_research.data.assets_management.OpenGLTexturesManager
import ilapin.opengl_research.data.assets_management.ShadersManager
import ilapin.opengl_research.data.scripting_engine.RhinoScriptingEngine
import ilapin.opengl_research.domain.*
import ilapin.opengl_research.domain.scene_loader.SceneLoader
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import org.joml.*
import java.nio.charset.Charset
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceViewRenderer(
    private val context: Context,
    private val messageQueue: MessageQueue,
    private val openGLErrorDetector: OpenGLErrorDetector,
    private val frameBuffersManager: FrameBuffersManager,
    private val geometryManager: OpenGLGeometryManager,
    private val texturesManager: OpenGLTexturesManager,
    private val shadersManager: ShadersManager,
    private val meshStorage: MeshStorage,
    private val touchEventsRepository: AndroidTouchEventsRepository,
    private val sceneLoader: SceneLoader,
    private val scriptingEngine: RhinoScriptingEngine,
    private val timeRepository: TimeRepository,
    private val displayMetricsRepository: AndroidDisplayMetricsRepository,
    private val vectorsPool: ObjectsPool<Vector3f>,
    private val quaternionsPool: ObjectsPool<Quaternionf>
) : GLSurfaceView.Renderer, SceneManager {

    private val messageQueueSubscription: Disposable

    private var displayWidth: Int? = null
    private var displayHeight: Int? = null

    private var scene: Scene2? = null
    private var shadowMapFrameBufferInfo: FrameBufferInfo.DepthFrameBufferInfo? = null

    private var isAlreadyInitialized = false

    private val matrixPool = ObjectsPool { Matrix4f() }

    private val _isLoadingSubject = BehaviorSubject.createDefault(false)

    val isLoading: Observable<Boolean> = _isLoadingSubject.observeOn(AndroidSchedulers.mainThread())

    init {
        messageQueueSubscription = messageQueue.messages().subscribe {
            when (it) {
                is LifecycleMessage.DeinitMessage -> scene?.deinit()
                is LifecycleMessage.GoingToForegroundMessage -> scene?.onGoingToForeground()
                is LifecycleMessage.GoingToBackgroundMessage -> scene?.onGoingToBackground()
                is LoadAndStartSceneMessage -> loadAndStartScene(it.path)
            }
        }
    }

    override fun onDrawFrame(gl: GL10) {
        if (openGLErrorDetector.isOpenGLErrorDetected) {
            return
        }

        touchEventsRepository.clearPrevEvents()
        messageQueue.update()

        render()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        if (isAlreadyInitialized) {
            return
        }

        _isLoadingSubject.onNext(true)

        displayWidth = width
        displayHeight = height

        displayMetricsRepository.onDisplaySizeChanged(width, height)

        GLES20.glFrontFace(GLES20.GL_CCW)
        GLES20.glCullFace(GLES20.GL_BACK)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        setupShaders()

        openGLErrorDetector.dispatchOpenGLErrors("onSurfaceChanged")

        _isLoadingSubject.onNext(false)
        isAlreadyInitialized = true
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig) {
        if (isAlreadyInitialized) {
            _isLoadingSubject.onNext(true)

            shadersManager.restoreShaders()
            texturesManager.restoreTextures()
            geometryManager.restoreBuffers()
            frameBuffersManager.restoreFrameBuffers()

            _isLoadingSubject.onNext(false)
        }
    }

    override fun loadAndStartScene(path: String) {
        scene?.deinit()
        shadowMapFrameBufferInfo = null

        scene = ScriptedScene(
            sceneLoader.loadScene(path),
            scriptingEngine,
            texturesManager,
            geometryManager,
            frameBuffersManager,
            meshStorage,
            timeRepository,
            touchEventsRepository,
            displayMetricsRepository,
            vectorsPool,
            quaternionsPool
        )

        safeLet(displayWidth, displayHeight) { width, height ->
            frameBuffersManager.createDepthOnlyFramebuffer(SHADOW_MAP_TEXTURE_NAME, width, height)
            shadowMapFrameBufferInfo =
                frameBuffersManager.findFrameBuffer(SHADOW_MAP_TEXTURE_NAME) as FrameBufferInfo.DepthFrameBufferInfo
        }
    }

    fun putMessage(message: Any) {
        messageQueue.putMessage(message)
    }

    fun putMessageAndWaitForExecution(message: Any) {
        messageQueue.putMessageAndWaitForExecution(message)
    }

    private fun render() {
        val scene = this.scene ?: return

        scene.update()

        val width = displayWidth ?: return
        val height = displayHeight ?: return
        val displayAspect = width.toFloat() / height

        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        // Clearing all render targets
        scene.renderTargets.forEach { renderTarget ->
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, renderTarget.frameBuffer)
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        }
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // Opaque rendering
        scene.renderTargets.forEach { renderTarget -> render(scene, renderTarget, false, displayAspect) }
        render(scene, FrameBufferInfo.DisplayFrameBufferInfo, false, displayAspect)

        // Translucent rendering
        render(scene, FrameBufferInfo.DisplayFrameBufferInfo, true, displayAspect)
        /*(scene.renderTargets + FrameBufferInfo.DisplayFrameBufferInfo).forEach { renderTarget ->
            renderUnlitObjects(scene, renderTarget, true, displayAspect)
            renderAmbientLight(scene, renderTarget, true, displayAspect)
            scene.lights.forEach { light ->
                when (light) {
                    is DirectionalLightComponent -> renderDirectionalLight(
                            scene,
                            light,
                            renderTarget,
                            true,
                            displayAspect
                    )
                }
            }
        }*/

        openGLErrorDetector.dispatchOpenGLErrors("render")
    }

    private fun renderDirectionalLight(
        scene: Scene2,
        renderTarget: FrameBufferInfo,
        light: DirectionalLightComponent,
        camera: CameraComponent,
        layerName: String,
        isTranslucentRendering: Boolean,
        viewportAspect: Float
    ) {
        val modelMatrix = matrixPool.obtain()
        val viewMatrix = matrixPool.obtain()
        val projectionMatrix = matrixPool.obtain()
        val lightViewMatrix = matrixPool.obtain()
        val lightProjectionMatrix = matrixPool.obtain()

        val viewerTransform = camera.gameObject?.getComponent(TransformationComponent::class.java)
                ?: error("Transform not found for camera ${camera.gameObject?.name}")
        val lightCamera = light.gameObject?.getComponent(DirectionalLightShadowMapCameraComponent::class.java)
                ?: error("Shadow map camera not found for directional light ${light.gameObject?.name}")
        lightCamera.calculateViewMatrix(viewerTransform.position, lightViewMatrix)
        lightCamera.calculateProjectionMatrix(lightProjectionMatrix)

        renderShadowMap(scene, layerName, lightViewMatrix, lightProjectionMatrix)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, renderTarget.frameBuffer)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)
        GLES20.glDepthMask(false)
        GLES20.glDepthFunc(GLES20.GL_EQUAL)

        val shaderProgram = shadersManager
            .findShaderProgram("directional_light_shader_program") as ShaderProgramInfo.DirectionalLightShaderProgram
        GLES20.glUseProgram(shaderProgram.shaderProgram)

        fillDirectionalLightShaderUniforms(shaderProgram, light)

        scene.layerRenderers[layerName].forEach { renderer ->
            val transform = renderer.gameObject?.getComponent(TransformationComponent::class.java)
                ?: error("Not transform found for game object ${renderer.gameObject?.name}")
            when (camera) {
                is PerspectiveCameraComponent -> {
                    camera.calculateViewMatrix(viewMatrix)
                    camera.calculateProjectionMatrix(viewportAspect, projectionMatrix)
                }

                is OrthoCameraComponent -> {
                    camera.calculateViewMatrix(viewMatrix)
                    camera.calculateProjectionMatrix(projectionMatrix)
                }
            }
            modelMatrix.identity()
                    .translate(transform.position)
                    .rotate(transform.rotation)
                    .scale(transform.scale)
            renderer.render(
                shaderProgram,
                isTranslucentRendering,
                false,
                modelMatrix,
                viewMatrix,
                projectionMatrix,
                modelMatrix,
                lightViewMatrix,
                lightProjectionMatrix,
                shadowMapFrameBufferInfo?.depthTextureInfo
            )
        }

        matrixPool.recycle(modelMatrix)
        matrixPool.recycle(viewMatrix)
        matrixPool.recycle(projectionMatrix)
        matrixPool.recycle(lightViewMatrix)
        matrixPool.recycle(lightProjectionMatrix)

        openGLErrorDetector.dispatchOpenGLErrors("renderDirectionalLights")
    }

    private fun renderShadowMap(
            scene: Scene2,
            layerName: String,
            lightViewMatrix: Matrix4fc,
            lightProjectionMatrix: Matrix4fc
    ) {
        val modelMatrix = matrixPool.obtain()

        val shaderProgram = shadersManager
                .findShaderProgram("shadow_map_shader_program") as ShaderProgramInfo.ShadowMapShaderProgram

        GLES20.glUseProgram(shaderProgram.shaderProgram)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, shadowMapFrameBufferInfo?.frameBuffer ?: 0)
        GLES20.glDepthMask(true)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glDepthFunc(GLES20.GL_LESS)

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        scene.layerRenderers[layerName].forEach { renderer ->
            val transform = renderer.gameObject?.getComponent(TransformationComponent::class.java)
                    ?: error("Not transform found for game object ${renderer.gameObject?.name}")
            renderer.render(
                    shaderProgram,
                    isTranslucentRendering = false,
                    isShadowMapRendering = true,
                    modelMatrix = modelMatrix.identity()
                            .translate(transform.position)
                            .rotate(transform.rotation)
                            .scale(transform.scale),
                    viewMatrix = lightViewMatrix,
                    projectionMatrix = lightProjectionMatrix
            )
        }

        matrixPool.recycle(modelMatrix)

        openGLErrorDetector.dispatchOpenGLErrors("renderShadowMap")
    }

    private fun render(
        scene: Scene2,
        renderTarget: FrameBufferInfo,
        isTranslucentRendering: Boolean,
        viewportAspect: Float
    ) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, renderTarget.frameBuffer)

        scene.activeCameras.forEach { camera ->
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

            camera.layerNames.forEach { layerName ->
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

                renderUnlitObjects(scene, camera, layerName, isTranslucentRendering, viewportAspect)
                renderAmbientLight(scene, camera, layerName, isTranslucentRendering, viewportAspect)

                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE)
                GLES20.glDepthMask(false)
                GLES20.glDepthFunc(GLES20.GL_EQUAL)

                scene.lights.forEach { light ->
                    when (light) {
                        is DirectionalLightComponent -> renderDirectionalLight(
                            scene,
                            renderTarget,
                            light,
                            camera,
                            layerName,
                            false,
                            viewportAspect
                        )
                    }
                }

                GLES20.glDepthMask(true)
                GLES20.glDisable(GLES20.GL_BLEND)
                GLES20.glDepthFunc(GLES20.GL_LESS)
            }
        }

        openGLErrorDetector.dispatchOpenGLErrors("render")
    }

    private fun renderUnlitObjects(
        scene: Scene2,
        camera: CameraComponent,
        layerName: String,
        isTranslucentRendering: Boolean,
        viewportAspect: Float
    ) {
        val modelMatrix = matrixPool.obtain()
        val viewMatrix = matrixPool.obtain()
        val projectionMatrix = matrixPool.obtain()

        val shaderProgram = shadersManager
            .findShaderProgram("unlit_shader_program") as ShaderProgramInfo.UnlitShaderProgram

        GLES20.glUseProgram(shaderProgram.shaderProgram)

        scene.layerRenderers[layerName].forEach { renderer ->
            val transform = renderer.gameObject?.getComponent(TransformationComponent::class.java)
                ?: error("Not transform found for game object ${renderer.gameObject?.name}")
            when (camera) {
                is PerspectiveCameraComponent -> {
                    camera.calculateViewMatrix(viewMatrix)
                    camera.calculateProjectionMatrix(viewportAspect, projectionMatrix)
                }

                is OrthoCameraComponent -> {
                    camera.calculateViewMatrix(viewMatrix)
                    camera.calculateProjectionMatrix(projectionMatrix)
                }
            }
            renderer.render(
                shaderProgram,
                isTranslucentRendering,
                false,
                modelMatrix.identity()
                    .translate(transform.position)
                    .rotate(transform.rotation)
                    .scale(transform.scale),
                viewMatrix,
                projectionMatrix
            )
        }

        matrixPool.recycle(modelMatrix)
        matrixPool.recycle(viewMatrix)
        matrixPool.recycle(projectionMatrix)

        openGLErrorDetector.dispatchOpenGLErrors("renderUnlitObjects")
    }

    private fun renderAmbientLight(
        scene: Scene2,
        camera: CameraComponent,
        layerName: String,
        isTranslucentRendering: Boolean,
        viewportAspect: Float
    ) {
        val modelMatrix = matrixPool.obtain()
        val viewMatrix = matrixPool.obtain()
        val projectionMatrix = matrixPool.obtain()

        val ambientLightColor = scene.cameraAmbientLights[camera]
            ?: error("No ambient light found for camera ${camera.gameObject?.name}")

        val ambientShaderProgram = shadersManager
            .findShaderProgram("ambient_shader_program") as ShaderProgramInfo.AmbientLightShaderProgram
        GLES20.glUseProgram(ambientShaderProgram.shaderProgram)

        fillAmbientShaderUniforms(ambientShaderProgram, ambientLightColor)

        scene.layerRenderers[layerName].forEach { renderer ->
            val transform = renderer.gameObject?.getComponent(TransformationComponent::class.java)
                ?: error("Not transform found for game object ${renderer.gameObject?.name}")
            when (camera) {
                is PerspectiveCameraComponent -> {
                    camera.calculateViewMatrix(viewMatrix)
                    camera.calculateProjectionMatrix(viewportAspect, projectionMatrix)
                }

                is OrthoCameraComponent -> {
                    camera.calculateViewMatrix(viewMatrix)
                    camera.calculateProjectionMatrix(projectionMatrix)
                }
            }
            renderer.render(
                ambientShaderProgram,
                isTranslucentRendering,
                false,
                modelMatrix.identity()
                    .translate(transform.position)
                    .rotate(transform.rotation)
                    .scale(transform.scale),
                viewMatrix,
                projectionMatrix
            )
        }

        matrixPool.recycle(modelMatrix)
        matrixPool.recycle(viewMatrix)
        matrixPool.recycle(projectionMatrix)

        openGLErrorDetector.dispatchOpenGLErrors("renderAmbientLight")
    }

    private fun fillAmbientShaderUniforms(
        shaderInfo: ShaderProgramInfo.AmbientLightShaderProgram,
        ambientLightColor: Vector3fc
    ) {
        GLES20.glUniform3f(
            shaderInfo.ambientColorUniform,
            ambientLightColor.x(),
            ambientLightColor.y(),
            ambientLightColor.z()
        )

        openGLErrorDetector.dispatchOpenGLErrors("fillAmbientShaderUniforms")
    }

    private fun fillDirectionalLightShaderUniforms(
        shaderInfo: ShaderProgramInfo.DirectionalLightShaderProgram,
        light: DirectionalLightComponent
    ) {
        GLES20.glUniform3f(
                shaderInfo.directionalLightColorUniform,
                light.color.x,
                light.color.y,
                light.color.z
        )

        val direction = light.direction
        GLES20.glUniform3f(
                shaderInfo.directionalLightDirectionUniform,
                direction.x(),
                direction.y(),
                direction.z()
        )

        openGLErrorDetector.dispatchOpenGLErrors("fillDirectionalLightShaderUniforms")
    }

    private fun setupShaders() {
        shadersManager.createVertexShader(
                "ambient_vertex_shader",
                context.assets.open("ambient/ambientVertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        shadersManager.createFragmentShader(
                "ambient_fragment_shader",
                context.assets.open("ambient/ambientFragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        shadersManager.createAmbientLightShaderProgram(
                "ambient_shader_program",
                shadersManager.findVertexShader("ambient_vertex_shader") ?: error("Ambient vertex shader not found"),
                shadersManager.findFragmentShader("ambient_fragment_shader")
                    ?: error("Ambient fragment shader not found")
        )

        shadersManager.createVertexShader(
                "unlit_vertex_shader",
                context.assets.open("unlit/unlitVertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        shadersManager.createFragmentShader(
                "unlit_fragment_shader",
                context.assets.open("unlit/unlitFragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        shadersManager.createUnlitShaderProgram(
            "unlit_shader_program",
            shadersManager.findVertexShader("unlit_vertex_shader") ?: error("Unlit vertex shader not found"),
            shadersManager.findFragmentShader("unlit_fragment_shader") ?: error("Unlit fragment shader not found")
        )

        shadersManager.createVertexShader(
                "shadow_map_vertex_shader",
                context.assets.open("shadowMap/shadowMapVertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        shadersManager.createFragmentShader(
                "shadow_map_fragment_shader",
                context.assets.open("shadowMap/shadowMapFragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        shadersManager.createShadowMapShaderProgram(
            "shadow_map_shader_program",
            shadersManager.findVertexShader("shadow_map_vertex_shader") ?: error("Shadow map vertex shader not found"),
            shadersManager.findFragmentShader("shadow_map_fragment_shader")
                ?: error("Shadow map fragment shader not found")
        )

        shadersManager.createVertexShader(
                "directional_light_vertex_shader",
                context
                        .assets
                        .open("directionalLight/directionalLightVertexShader.glsl")
                        .readBytes()
                        .toString(Charset.defaultCharset())
        )
        shadersManager.createFragmentShader(
                "directional_light_fragment_shader",
                context
                        .assets
                        .open("directionalLight/directionalLightFragmentShader.glsl")
                        .readBytes()
                        .toString(Charset.defaultCharset())
        )
        shadersManager.createDirectionalLightShaderProgram(
            "directional_light_shader_program",
            shadersManager.findVertexShader("directional_light_vertex_shader")
                ?: error("Directional light vertex shader not found"),
            shadersManager.findFragmentShader("directional_light_fragment_shader")
                ?: error("Directional light fragment shader not found")
        )
    }

    sealed class LifecycleMessage {
        object DeinitMessage : LifecycleMessage()
        object GoingToBackgroundMessage: LifecycleMessage()
        object GoingToForegroundMessage: LifecycleMessage()
    }

    class LoadAndStartSceneMessage(val path: String)
}