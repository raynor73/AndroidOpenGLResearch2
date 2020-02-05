package ilapin.opengl_research

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import ilapin.engine3d.GameObject
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.domain.DirectionalLightScene
import ilapin.opengl_research.domain.Scene2
import org.joml.Matrix4f
import org.joml.Vector3fc
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class GLSurfaceViewRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private val openGLErrorDetector = OpenGLErrorDetector()
    private val openGLObjectsRepository = OpenGLObjectsRepository(openGLErrorDetector)

    private var displayWidth: Int? = null
    private var displayHeight: Int? = null

    private var scene: Scene2? = null
    private var directionalLightShadowMapCamera: GameObject? = null

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

        val scene = DirectionalLightScene(
            context,
            width,
            height,
            openGLObjectsRepository,
            openGLErrorDetector
        )
        this.scene = scene
        directionalLightShadowMapCamera = scene.directionalLightShadowMapCamera

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

        val lightModelMatrix = matrixPool.obtain()
        val lightViewMatrix = matrixPool.obtain()
        val lightProjectionMatrix = matrixPool.obtain()

        (scene.renderTargets + FrameBufferInfo.DisplayFrameBufferInfo).forEach { renderTarget ->
            renderUnlitObjects(scene, renderTarget, displayAspect)
            renderAmbientLight(scene, renderTarget, displayAspect)
            renderDirectionalLights(scene, renderTarget, displayAspect)
        }


        scene.cameras.forEach { camera ->
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
        }

        matrixPool.recycle(lightModelMatrix)
        matrixPool.recycle(lightViewMatrix)
        matrixPool.recycle(lightProjectionMatrix)

        openGLErrorDetector.dispatchOpenGLErrors("render")
    }

    private fun renderDirectionalLights(scene: Scene2, renderTarget: FrameBufferInfo, displayAspect: Float) {

    }

    private fun renderUnlitObjects(scene: Scene2, renderTarget: FrameBufferInfo, viewportAspect: Float) {
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

    private fun renderAmbientLight(scene: Scene2, renderTarget: FrameBufferInfo, viewportAspect: Float) {
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
    }

    /*private fun renderShadowMaps(scene: Scene2) {
        val modelMatrix = matrixPool.obtain()
        val viewMatrix = matrixPool.obtain()
        val projectionMatrix = matrixPool.obtain()

        scene.shadowMapCameras.forEach { camera ->
            camera.layerNames.forEach { layerName ->
                scene.shadowLayerRenderers[layerName].forEach { renderer ->
                    val meshName = renderer.gameObject?.getComponent(MeshComponent::class.java)!!.name
                    val transform = renderer.gameObject?.getComponent(TransformationComponent::class.java)!!
                    when (camera) {
                        is DirectionalLightShadowMapCameraComponent -> {
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
            }
        }

        matrixPool.recycle(modelMatrix)
        matrixPool.recycle(viewMatrix)
        matrixPool.recycle(projectionMatrix)

        openGLErrorDetector.dispatchOpenGLErrors("renderShadowMaps")
    }*/
}