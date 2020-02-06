package ilapin.opengl_research

import android.opengl.GLES20
import android.util.DisplayMetrics
import ilapin.engine3d.GameObjectComponent
import org.joml.Matrix4f
import org.joml.Matrix4fc
import kotlin.math.ceil

/**
 * @author raynor on 05.02.20.
 */
class MeshRendererComponent(
    displayMetrics: DisplayMetrics,
    private val openGLObjectsRepository: OpenGLObjectsRepository,
    private val openGLErrorDetector: OpenGLErrorDetector
) : GameObjectComponent() {

    private val lineWidth = ceil(displayMetrics.density)

    private val tmpFloatArray = FloatArray(16)
    private val tmpMatrix = Matrix4f()

    fun render(
        shaderProgram: ShaderProgramInfo,
        renderTargetFrameBufferInfo: FrameBufferInfo?,
        isTranslucentRendering: Boolean,
        modelMatrix: Matrix4fc,
        viewMatrix: Matrix4fc,
        projectionMatrix: Matrix4fc,
        lightModelMatrix: Matrix4fc? = null,
        lightViewMatrix: Matrix4fc? = null,
        lightProjectionMatrix: Matrix4fc? = null
    ) {
        if (!isEnabled) {
            return
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, renderTargetFrameBufferInfo?.frameBuffer ?: 0)

        val mesh = gameObject?.getComponent(MeshComponent::class.java) ?: return
        val material = gameObject?.getComponent(MaterialComponent::class.java) ?: return

        if (material.isUnlit && shaderProgram !is ShaderProgramInfo.UnlitShaderProgram) {
            return
        }

        if (material.isTranslucent && !isTranslucentRendering) {
            return
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mesh.vbo)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, mesh.iboInfo.ibo)

        GLES20.glVertexAttribPointer(
            shaderProgram.vertexCoordinateAttribute,
            VERTEX_COORDINATE_COMPONENTS,
            GLES20.GL_FLOAT,
            false,
            VERTEX_COMPONENTS * BYTES_IN_FLOAT,
            0
        )
        GLES20.glEnableVertexAttribArray(shaderProgram.vertexCoordinateAttribute)

        shaderProgram.normalAttribute.takeIf { it > 0 }?.let { normalAttribute ->
            GLES20.glVertexAttribPointer(
                normalAttribute,
                NORMAL_COMPONENTS,
                GLES20.GL_FLOAT,
                false,
                VERTEX_COMPONENTS * BYTES_IN_FLOAT,
                VERTEX_COORDINATE_COMPONENTS * BYTES_IN_FLOAT
            )
            GLES20.glEnableVertexAttribArray(normalAttribute)
        }

        shaderProgram.uvAttribute.takeIf { it > 0 }?.let { uvAttribute ->
            GLES20.glVertexAttribPointer(
                uvAttribute,
                TEXTURE_COORDINATE_COMPONENTS,
                GLES20.GL_FLOAT,
                false,
                VERTEX_COMPONENTS * BYTES_IN_FLOAT,
                (VERTEX_COORDINATE_COMPONENTS + NORMAL_COMPONENTS) * BYTES_IN_FLOAT
            )
            GLES20.glEnableVertexAttribArray(uvAttribute)
        }

        tmpMatrix.set(projectionMatrix)
        tmpMatrix.mul(viewMatrix)
        tmpMatrix.mul(modelMatrix)
        tmpMatrix.get(tmpFloatArray)
        GLES20.glUniformMatrix4fv(shaderProgram.mvpMatrixUniform, 1, false, tmpFloatArray, 0)

        shaderProgram.modelMatrixUniform.takeIf { it > 0 }?.let { modelMatrixUniform ->
            modelMatrix.get(tmpFloatArray)
            GLES20.glUniformMatrix4fv(modelMatrixUniform, 1, false, tmpFloatArray, 0)
        }

        GLES20.glUniform4f(
            shaderProgram.diffuseColorUniform,
            material.diffuseColor.x,
            material.diffuseColor.y,
            material.diffuseColor.z,
            material.diffuseColor.w
        )

        val textureName = material.textureName
        if (textureName != null) {
            val textureInfo = openGLObjectsRepository.findTexture(textureName)
                ?: error("Texture not found for ${gameObject?.name}")
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.texture)
            GLES20.glUniform1i(shaderProgram.textureUniform, 0)
            GLES20.glUniform1i(shaderProgram.shouldUseDiffuseColorUniform, GLES20.GL_FALSE)
        } else {
            GLES20.glUniform1i(shaderProgram.shouldUseDiffuseColorUniform, GLES20.GL_TRUE)
        }

        if (material.isDoubleSided) {
            GLES20.glDisable(GLES20.GL_CULL_FACE)
        } else {
            GLES20.glEnable(GLES20.GL_CULL_FACE)
        }

        val mode = if (material.isWireframe) {
            GLES20.GL_LINES
        } else {
            GLES20.GL_TRIANGLES
        }
        GLES20.glLineWidth(lineWidth)

        if (material.isTranslucent) {
            GLES20.glEnable(GLES20.GL_CULL_FACE)

            GLES20.glCullFace(GLES20.GL_FRONT)
            GLES20.glDrawElements(
                mode,
                mesh.iboInfo.numberOfIndices,
                GLES20.GL_UNSIGNED_SHORT,
                0
            )

            GLES20.glCullFace(GLES20.GL_BACK)
            GLES20.glDrawElements(
                mode,
                mesh.iboInfo.numberOfIndices,
                GLES20.GL_UNSIGNED_SHORT,
                0
            )
        } else {
            GLES20.glDrawElements(
                mode,
                mesh.iboInfo.numberOfIndices,
                GLES20.GL_UNSIGNED_SHORT,
                0
            )
        }

        GLES20.glDisableVertexAttribArray(shaderProgram.vertexCoordinateAttribute)
        GLES20.glDisableVertexAttribArray(shaderProgram.uvAttribute)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        openGLErrorDetector.dispatchOpenGLErrors("MeshRendererComponent.render")
    }
}