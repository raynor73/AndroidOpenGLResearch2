package ilapin.opengl_research

import android.opengl.GLES20
import ilapin.common.kotlin.safeLet
import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.data.assets_management.OpenGLGeometryManager
import ilapin.opengl_research.data.assets_management.OpenGLTexturesManager
import ilapin.opengl_research.domain.skeletal_animation.SkeletalAnimatorComponent
import org.joml.Matrix4f
import org.joml.Matrix4fc

/**
 * @author raynor on 05.02.20.
 */
class MeshRendererComponent(
    private val lineWidth: Float,
    private val texturesManager: OpenGLTexturesManager,
    private val geometryManager: OpenGLGeometryManager,
    private val openGLErrorDetector: OpenGLErrorDetector
) : GameObjectComponent() {

    private val tmpFloatArray = FloatArray(MATRIX_COMPONENTS)
    private val tmpMatrix = Matrix4f()
    private val tmpJointTransformsFloatArray = FloatArray(MATRIX_COMPONENTS * MAX_JOINTS)

    fun render(
        shaderProgram: ShaderProgramInfo,
        isTranslucentRendering: Boolean,
        isShadowMapRendering: Boolean,
        modelMatrix: Matrix4fc,
        viewMatrix: Matrix4fc,
        projectionMatrix: Matrix4fc,
        lightModelMatrix: Matrix4fc? = null,
        lightViewMatrix: Matrix4fc? = null,
        lightProjectionMatrix: Matrix4fc? = null,
        shadowMapTextureInfo: TextureInfo? = null
    ) {
        if (!isEnabled) {
            return
        }

        val mesh = gameObject?.getComponent(MeshComponent::class.java) ?: return
        val vbo = geometryManager.findVbo(mesh.name) ?: error("VBO ${mesh.name} not found")
        val iboInfo = geometryManager.findIbo(mesh.name) ?: error("IBO ${mesh.name} not found")
        val material = gameObject?.getComponent(MaterialComponent::class.java) ?: return

        if (isShadowMapRendering && !material.castShadows) {
            return
        }

        if (
            (material.isUnlit && shaderProgram !is ShaderProgramInfo.UnlitShaderProgram) or
            (!material.isUnlit && shaderProgram is ShaderProgramInfo.UnlitShaderProgram)
        ) {
            return
        }

        if (material.isTranslucent && !isTranslucentRendering) {
            return
        }

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, iboInfo.ibo)

        shaderProgram.vertexCoordinateAttribute.takeIf { it >= 0 }?.let { vertexCoordinateAttribute ->
            GLES20.glVertexAttribPointer(
                    vertexCoordinateAttribute,
                    VERTEX_COORDINATE_COMPONENTS,
                    GLES20.GL_FLOAT,
                    false,
                    VERTEX_COMPONENTS * BYTES_IN_FLOAT,
                    0
            )
            GLES20.glEnableVertexAttribArray(vertexCoordinateAttribute)
        }

        shaderProgram.normalAttribute.takeIf { it >= 0 }?.let { normalAttribute ->
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

        shaderProgram.uvAttribute.takeIf { it >= 0 }?.let { uvAttribute ->
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

        shaderProgram.jointIndicesAttribute.takeIf { it >= 0 }?.let { jointIndicesAttribute ->
            GLES20.glVertexAttribPointer(
                jointIndicesAttribute,
                NUMBER_OF_JOINT_INDICES,
                GLES20.GL_FLOAT,
                false,
                VERTEX_COMPONENTS * BYTES_IN_FLOAT,
                (VERTEX_COORDINATE_COMPONENTS +
                        NORMAL_COMPONENTS +
                        TEXTURE_COORDINATE_COMPONENTS) * BYTES_IN_FLOAT
            )
            GLES20.glEnableVertexAttribArray(jointIndicesAttribute)
        }

        shaderProgram.jointWeightsAttribute.takeIf { it >= 0 }?.let { jointWeightsAttribute ->
            GLES20.glVertexAttribPointer(
                jointWeightsAttribute,
                NUMBER_OF_JOINT_WEIGHTS,
                GLES20.GL_FLOAT,
                false,
                VERTEX_COMPONENTS * BYTES_IN_FLOAT,
                (VERTEX_COORDINATE_COMPONENTS +
                        NORMAL_COMPONENTS +
                        TEXTURE_COORDINATE_COMPONENTS +
                        NUMBER_OF_JOINT_INDICES) * BYTES_IN_FLOAT
            )
            GLES20.glEnableVertexAttribArray(jointWeightsAttribute)
        }

        shaderProgram.mvpMatrixUniform.takeIf { it >= 0 }?.let { mvpMatrixUniform ->
            tmpMatrix.set(projectionMatrix)
            tmpMatrix.mul(viewMatrix)
            tmpMatrix.mul(modelMatrix)
            tmpMatrix.get(tmpFloatArray)
            GLES20.glUniformMatrix4fv(mvpMatrixUniform, 1, false, tmpFloatArray, 0)
        }

        shaderProgram.lightMvpMatrixUniform.takeIf { it >= 0 }?.let { lightMvpMatrixUniform ->
            tmpMatrix.set(lightProjectionMatrix)
            tmpMatrix.mul(lightViewMatrix)
            tmpMatrix.mul(lightModelMatrix)
            tmpMatrix.get(tmpFloatArray)
            GLES20.glUniformMatrix4fv(lightMvpMatrixUniform, 1, false, tmpFloatArray, 0)
        }

        shaderProgram.modelMatrixUniform.takeIf { it >= 0 }?.let { modelMatrixUniform ->
            modelMatrix.get(tmpFloatArray)
            GLES20.glUniformMatrix4fv(modelMatrixUniform, 1, false, tmpFloatArray, 0)
        }

        shaderProgram.biasMatrixUniform.takeIf { it >= 0 }?.let { biasMatrixUniform ->
            BIAS_MATRIX.get(tmpFloatArray)
            GLES20.glUniformMatrix4fv(biasMatrixUniform, 1, false, tmpFloatArray, 0)
        }

        shaderProgram.diffuseColorUniform.takeIf { it >= 0 }?.let { diffuseColorUniform ->
            GLES20.glUniform4f(
                    diffuseColorUniform,
                    material.diffuseColor.x,
                    material.diffuseColor.y,
                    material.diffuseColor.z,
                    material.diffuseColor.w
            )
        }

        val textureName = material.textureName
        if (textureName != null) {
            val textureInfo = texturesManager.findTexture(textureName)
                ?: error("Texture not found for ${gameObject?.name}")

            shaderProgram.textureUniform.takeIf { it >= 0 }?.let { textureUniform ->
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.texture)
                GLES20.glUniform1i(textureUniform, 0)
            }

            shaderProgram.useDiffuseColorUniform.glUniform1i(GLES20.GL_FALSE)
        } else {
            shaderProgram.useDiffuseColorUniform.glUniform1i(GLES20.GL_TRUE)
        }

        shaderProgram.receiveShadows.glUniform1i(material.receiveShadows.toGLBoolean())
        safeLet(shadowMapTextureInfo, shaderProgram.shadowMapUniform.takeIf { it >= 0 }) { textureInfo, shadowMapUniform ->
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.texture)
            GLES20.glUniform1i(shadowMapUniform, 1)
        }

        val hasSkeletalAnimation = safeLet(
            shaderProgram.jointTransformsUniform.takeIf { it >= 0 },
            gameObject?.getComponent(SkeletalAnimatorComponent::class.java)?.jointTransforms
        ) { jointTransformsUniform, jointTransforms ->
            jointTransforms.forEachIndexed { i, jointTransform ->
                jointTransform?.get(tmpJointTransformsFloatArray, i * MATRIX_COMPONENTS)
            }
            GLES20.glUniformMatrix4fv(
                jointTransformsUniform,
                MAX_JOINTS,
                false,
                tmpJointTransformsFloatArray,
                0
            )
            true
        } ?: false
        shaderProgram.hasSkeletalAnimationUniform.glUniform1i(hasSkeletalAnimation.toGLBoolean())

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
                iboInfo.numberOfIndices,
                GLES20.GL_UNSIGNED_SHORT,
                0
            )

            GLES20.glCullFace(GLES20.GL_BACK)
            GLES20.glDrawElements(
                mode,
                iboInfo.numberOfIndices,
                GLES20.GL_UNSIGNED_SHORT,
                0
            )
        } else {
            GLES20.glDrawElements(
                mode,
                iboInfo.numberOfIndices,
                GLES20.GL_UNSIGNED_SHORT,
                0
            )
        }

        shaderProgram.vertexCoordinateAttribute.takeIf { it >= 0 }?.let { vertexCoordinateAttribute ->
            GLES20.glDisableVertexAttribArray(vertexCoordinateAttribute)
        }
        shaderProgram.normalAttribute.takeIf { it >= 0 }?.let { normalAttribute ->
            GLES20.glDisableVertexAttribArray(normalAttribute)
        }
        shaderProgram.uvAttribute.takeIf { it >= 0 }?.let { uvAttribute ->
            GLES20.glDisableVertexAttribArray(uvAttribute)
        }
        shaderProgram.jointIndicesAttribute.takeIf { it >= 0 }?.let { jointIndicesAttribute ->
            GLES20.glDisableVertexAttribArray(jointIndicesAttribute)
        }
        shaderProgram.jointWeightsAttribute.takeIf { it >= 0 }?.let { jointWeightsAttribute ->
            GLES20.glDisableVertexAttribArray(jointWeightsAttribute)
        }
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0)

        openGLErrorDetector.dispatchOpenGLErrors("MeshRendererComponent.render")
    }

    companion object {

        private val BIAS_MATRIX: Matrix4fc = Matrix4f(
                0.5f, 0.0f, 0.0f, 0.0f,
                0.0f, 0.5f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.5f, 0.0f,
                0.5f, 0.5f, 0.5f, 1.0f
        )
    }
}