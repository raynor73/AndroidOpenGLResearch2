package ilapin.opengl_research

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.domain.DirectionalLightScene
import ilapin.opengl_research.domain.Scene2
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3fc
import java.nio.charset.Charset
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceViewRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val openGLErrorDetector = OpenGLErrorDetector()
    private val openGLObjectsRepository = OpenGLObjectsRepository(openGLErrorDetector)

    private var displayWidth: Int? = null
    private var displayHeight: Int? = null

    private var scene: Scene2? = null
    private var shadowMapFrameBufferInfo: FrameBufferInfo.DepthFrameBufferInfo? = null

    private val matrixPool = ObjectsPool { Matrix4f() }

    override fun onDrawFrame(gl: GL10) {
        if (openGLErrorDetector.isOpenGLErrorDetected) {
            return
        }

        render()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        displayWidth = width
        displayHeight = height

        GLES20.glFrontFace(GLES20.GL_CCW)
        GLES20.glCullFace(GLES20.GL_BACK)

        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_CULL_FACE)

        setupShaders()

        val scene = DirectionalLightScene(
            context,
            width,
            height,
            openGLObjectsRepository,
            openGLErrorDetector
        )
        this.scene = scene

        openGLObjectsRepository.createDepthOnlyFramebuffer("shadow_map", width, height)
        shadowMapFrameBufferInfo =
                openGLObjectsRepository.findFrameBuffer("shadow_map") as FrameBufferInfo.DepthFrameBufferInfo

        openGLErrorDetector.dispatchOpenGLErrors("onSurfaceChanged")
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig) {
        // do nothing
    }

    private fun render() {
        val scene = this.scene ?: return

        val width = displayWidth ?: return
        val height = displayHeight ?: return
        val displayAspect = width.toFloat() / height

        GLES20.glViewport(0, 0, width, height)
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        /*val lightModelMatrix = matrixPool.obtain()
        val lightViewMatrix = matrixPool.obtain()
        val lightProjectionMatrix = matrixPool.obtain()*/

        // Opaque rendering
        (scene.renderTargets + FrameBufferInfo.DisplayFrameBufferInfo).forEach { renderTarget ->
            renderUnlitObjects(scene, renderTarget, false, displayAspect)
            renderAmbientLight(scene, renderTarget, false, displayAspect)
            scene.lights.forEach { light ->
                when (light) {
                    is DirectionalLightComponent -> renderDirectionalLight(
                            scene,
                            light,
                            renderTarget,
                            false,
                            displayAspect
                    )
                }
            }
        }

        // Translucent rendering
        (scene.renderTargets + FrameBufferInfo.DisplayFrameBufferInfo).forEach { renderTarget ->
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
        }

        /*scene.cameras.forEach { camera ->
            //renderShadowMaps(scene)
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)
            camera.layerNames.forEach { layerName ->
                scene.layerRenderers[layerName].forEach { renderer ->
                    when (renderer) {
                        is DepthVisualizationRendererComponent -> {

                        }

                        is DirectionalLightRenderer -> {
                            val meshName = renderer.gameObject?.getComponent(MeshComponent::class.java)!!.name
                            val transform = renderer.gameObject?.getComponent(TransformationComponent::class.java)!!

                            when (camera) {
                                is PerspectiveCameraComponent -> {
                                    camera.calculateViewMatrix(viewMatrix)
                                    camera.calculateProjectionMatrix(displayAspect, projectionMatrix)
                                }

                                is OrthoCameraComponent -> {
                                    camera.calculateViewMatrix(viewMatrix)
                                    camera.calculateProjectionMatrix(projectionMatrix)
                                }
                            }

                            val lightCamera = directionalLightShadowMapCamera?.getComponent(
                                DirectionalLightShadowMapCameraComponent::class.java
                            )!!
                            lightCamera.calculateViewMatrix(lightViewMatrix)
                            lightCamera.calculateProjectionMatrix(lightProjectionMatrix)
                            renderer.render(
                                meshName,
                                meshName,
                                "shadowMap",
                                modelMatrix.identity()
                                    .translate(transform.position)
                                    .rotate(transform.rotation)
                                    .scale(transform.scale),
                                viewMatrix,
                                projectionMatrix,
                                lightModelMatrix.identity()
                                    .translate(transform.position)
                                    .rotate(transform.rotation)
                                    .scale(transform.scale),
                                lightViewMatrix,
                                lightProjectionMatrix
                            )
                        }

                        is ShadowMapVisualizationRenderer -> {
                            val meshName = renderer.gameObject?.getComponent(MeshComponent::class.java)!!.name
                            val transform = renderer.gameObject?.getComponent(TransformationComponent::class.java)!!
                            when (camera) {
                                is PerspectiveCameraComponent -> {
                                    camera.calculateViewMatrix(viewMatrix)
                                    camera.calculateProjectionMatrix(displayAspect, projectionMatrix)
                                }

                                is OrthoCameraComponent -> {
                                    camera.calculateViewMatrix(viewMatrix)
                                    camera.calculateProjectionMatrix(projectionMatrix)
                                }
                            }
                            renderer.render(
                                meshName,
                                meshName,
                                modelMatrix.identity()
                                    .translate(transform.position)
                                    .rotate(transform.rotation)
                                    .scale(transform.scale),
                                viewMatrix,
                                projectionMatrix
                            )
                        }

                        is ShadowMapRendererComponent -> {
                            val meshName = renderer.gameObject?.getComponent(MeshComponent::class.java)!!.name
                            val transform = renderer.gameObject?.getComponent(TransformationComponent::class.java)!!
                            when (camera) {
                                is DirectionalLightShadowMapCameraComponent -> {
                                    camera.calculateViewMatrix(viewMatrix)
                                    camera.calculateProjectionMatrix(projectionMatrix)
                                }

                                is PerspectiveCameraComponent -> {
                                    camera.calculateViewMatrix(viewMatrix)
                                    camera.calculateProjectionMatrix(displayAspect, projectionMatrix)
                                }

                                is OrthoCameraComponent -> {
                                    camera.calculateViewMatrix(viewMatrix)
                                    camera.calculateProjectionMatrix(projectionMatrix)
                                }
                            }
                            renderer.render(
                                meshName,
                                meshName,
                                "shadowMap",
                                modelMatrix.identity()
                                    .translate(transform.position)
                                    .rotate(transform.rotation)
                                    .scale(transform.scale),
                                viewMatrix,
                                projectionMatrix
                            )
                        }

                        is UnlitRendererComponent -> {
                            val meshName = renderer.gameObject?.getComponent(MeshComponent::class.java)!!.name
                            val transform = renderer.gameObject?.getComponent(TransformationComponent::class.java)!!
                            when (camera) {
                                is PerspectiveCameraComponent -> {
                                    camera.calculateViewMatrix(viewMatrix)
                                    camera.calculateProjectionMatrix(displayAspect, projectionMatrix)
                                }

                                is OrthoCameraComponent -> {
                                    camera.calculateViewMatrix(viewMatrix)
                                    camera.calculateProjectionMatrix(projectionMatrix)
                                }
                            }
                            renderer.render(
                                meshName,
                                meshName,
                                modelMatrix.identity()
                                    .translate(transform.position)
                                    .rotate(transform.rotation)
                                    .scale(transform.scale),
                                viewMatrix,
                                projectionMatrix
                            )
                        }
                    }
                }
            }
        }*/

        /*matrixPool.recycle(lightModelMatrix)
        matrixPool.recycle(lightViewMatrix)
        matrixPool.recycle(lightProjectionMatrix)*/

        openGLErrorDetector.dispatchOpenGLErrors("render")
    }

    private fun renderDirectionalLight(
        scene: Scene2,
        light: DirectionalLightComponent,
        renderTarget: FrameBufferInfo,
        isTranslucentRendering: Boolean,
        viewportAspect: Float
    ) {
        val modelMatrix = matrixPool.obtain()
        val viewMatrix = matrixPool.obtain()
        val projectionMatrix = matrixPool.obtain()
        val lightViewMatrix = matrixPool.obtain()
        val lightProjectionMatrix = matrixPool.obtain()

        scene.cameras.forEach { camera ->
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

            camera.layerNames.forEach { layerName ->
                val viewerTransform = camera.gameObject?.getComponent(TransformationComponent::class.java)
                        ?: error("Transform not found for camera ${camera.gameObject?.name}")
                val lightCamera = light.gameObject?.getComponent(DirectionalLightShadowMapCameraComponent::class.java)
                        ?: error("Shadow map camera not found for directional light ${light.gameObject?.name}")
                lightCamera.calculateViewMatrix(viewerTransform.position, lightViewMatrix)
                lightCamera.calculateProjectionMatrix(lightProjectionMatrix)

                renderShadowMap(scene, layerName, lightViewMatrix, lightProjectionMatrix)

                val shaderProgram = openGLObjectsRepository
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
                        renderTarget,
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
            }
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
        val viewMatrix = matrixPool.obtain()
        val projectionMatrix = matrixPool.obtain()

        val shaderProgram = openGLObjectsRepository
                .findShaderProgram("shadow_map_shader_program") as ShaderProgramInfo.ShadowMapShaderProgram

        GLES20.glUseProgram(shaderProgram.shaderProgram)

        scene.layerRenderers[layerName].forEach { renderer ->
            val transform = renderer.gameObject?.getComponent(TransformationComponent::class.java)
                    ?: error("Not transform found for game object ${renderer.gameObject?.name}")
            renderer.render(
                    shaderProgram,
                    shadowMapFrameBufferInfo,
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
        matrixPool.recycle(viewMatrix)
        matrixPool.recycle(projectionMatrix)

        openGLErrorDetector.dispatchOpenGLErrors("renderShadowMap")
    }

    private fun renderUnlitObjects(
        scene: Scene2,
        renderTarget: FrameBufferInfo,
        isTranslucentRendering: Boolean,
        viewportAspect: Float
    ) {
        val modelMatrix = matrixPool.obtain()
        val viewMatrix = matrixPool.obtain()
        val projectionMatrix = matrixPool.obtain()

        scene.cameras.forEach { camera ->
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)

            camera.layerNames.forEach { layerName ->
                val shaderProgram = openGLObjectsRepository
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
                        renderTarget,
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
            }
        }

        matrixPool.recycle(modelMatrix)
        matrixPool.recycle(viewMatrix)
        matrixPool.recycle(projectionMatrix)

        openGLErrorDetector.dispatchOpenGLErrors("renderAmbientLight")
    }

    private fun renderAmbientLight(
        scene: Scene2,
        renderTarget: FrameBufferInfo,
        isTranslucentRendering: Boolean,
        viewportAspect: Float
    ) {
        val modelMatrix = matrixPool.obtain()
        val viewMatrix = matrixPool.obtain()
        val projectionMatrix = matrixPool.obtain()

        scene.cameras.forEach { camera ->
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT)
            val ambientLightColor = scene.cameraAmbientLights[camera]
                ?: error("No ambient light found for camera ${camera.gameObject?.name}")
            camera.layerNames.forEach { layerName ->
                val ambientShaderProgram = openGLObjectsRepository
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
                        renderTarget,
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
            }
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
        /*openGLObjectsRepository.createVertexShader(
                "depth_visualizer_vertex_shader",
                context.assets.open("depthVisualization/vertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
                "depth_visualizer_fragment_shader",
                context.assets.open("depthVisualization/fragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createShaderProgram(
                "depth_visualizer_shader_program",
                openGLObjectsRepository.findVertexShader("depth_visualizer_vertex_shader")!!,
                openGLObjectsRepository.findFragmentShader("depth_visualizer_fragment_shader")!!
        )*/

        openGLObjectsRepository.createVertexShader(
                "ambient_light_vertex_shader",
                context.assets.open("ambient/ambientVertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
                "ambient_light_fragment_shader",
                context.assets.open("ambient/ambientFragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createAmbientLightShaderProgram(
                "ambient_shader_program",
                openGLObjectsRepository.findVertexShader("ambient_vertex_shader")!!,
                openGLObjectsRepository.findFragmentShader("ambient_fragment_shader")!!
        )

        openGLObjectsRepository.createVertexShader(
                "unlit_vertex_shader",
                context.assets.open("unlit/unlitVertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
                "unlit_fragment_shader",
                context.assets.open("unlit/unlitFragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createUnlitShaderProgram(
                "unlit_shader_program",
                openGLObjectsRepository.findVertexShader("unlit_vertex_shader")!!,
                openGLObjectsRepository.findFragmentShader("unlit_fragment_shader")!!
        )

        openGLObjectsRepository.createVertexShader(
                "shadow_map_vertex_shader",
                context.assets.open("shadowMap/shadowMapVertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
                "shadow_map_fragment_shader",
                context.assets.open("shadowMap/shadowMapFragmentShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createShadowMapShaderProgram(
                "shadow_map_shader_program",
                openGLObjectsRepository.findVertexShader("shadow_map_vertex_shader")!!,
                openGLObjectsRepository.findFragmentShader("shadow_map_fragment_shader")!!
        )

        /*openGLObjectsRepository.createVertexShader(
                "shadow_map_visualization_vertex_shader",
                context.assets.open("unlit/unlitVertexShader.glsl").readBytes().toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
                "shadow_map_visualization_fragment_shader",
                context
                        .assets
                        .open("depthVisualization/depthTextureFragmentShader.glsl")
                        .readBytes()
                        .toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createShaderProgram(
                "shadow_map_visualization_shader_program",
                openGLObjectsRepository.findVertexShader("shadow_map_visualization_vertex_shader")!!,
                openGLObjectsRepository.findFragmentShader("shadow_map_visualization_fragment_shader")!!
        )*/

        openGLObjectsRepository.createVertexShader(
                "directional_light_vertex_shader",
                context
                        .assets
                        .open("directionalLight/directionalLightVertexShader.glsl")
                        .readBytes()
                        .toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createFragmentShader(
                "directional_light_fragment_shader",
                context
                        .assets
                        .open("directionalLight/directionalLightFragmentShader.glsl")
                        .readBytes()
                        .toString(Charset.defaultCharset())
        )
        openGLObjectsRepository.createDirectionalLightShaderProgram(
                "directional_light_shader_program",
                openGLObjectsRepository.findVertexShader("directional_light_vertex_shader")!!,
                openGLObjectsRepository.findFragmentShader("directional_light_fragment_shader")!!
        )
    }
}