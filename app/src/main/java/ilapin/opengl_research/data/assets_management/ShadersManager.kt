package ilapin.opengl_research.data.assets_management

import android.opengl.GLES20
import ilapin.opengl_research.OpenGLErrorDetector
import ilapin.opengl_research.ShaderProgramInfo

/**
 * @author raynor on 18.02.20.
 */
class ShadersManager(
    private val openGLErrorDetector: OpenGLErrorDetector
) {
    private val shadersCreationParams = ArrayList<ShaderCreationParams>()
    private val shaderProgramsCreationParams = ArrayList<ShaderProgramCreationParams>()

    private val vertexShaders = HashMap<String, Int>()
    private val fragmentShaders = HashMap<String, Int>()
    private val shaderPrograms = HashMap<String, ShaderProgramInfo>()

    fun findVertexShader(name: String) = vertexShaders[name]
    fun findFragmentShader(name: String) = fragmentShaders[name]
    fun findShaderProgram(name: String) = shaderPrograms[name]

    fun restoreShaders() {
        vertexShaders.clear()
        fragmentShaders.clear()
        shaderPrograms.clear()

        shadersCreationParams.forEach { params ->
            when (params) {
                is ShaderCreationParams.VertexShader -> createVertexShader(params.name, params.source)
                is ShaderCreationParams.FragmentShader -> createFragmentShader(params.name, params.source)
            }
        }

        shaderProgramsCreationParams.forEach { params ->
            when (params) {
                is ShaderProgramCreationParams.Ambient -> createAmbientLightShaderProgram(
                    params.name,
                    findVertexShader(params.name) ?: error("Vertex shader ${params.name} not found"),
                    findFragmentShader(params.name) ?: error("Fragment shader ${params.name} not found")
                )

                is ShaderProgramCreationParams.Unlit -> createUnlitShaderProgram(
                    params.name,
                    findVertexShader(params.name) ?: error("Vertex shader ${params.name} not found"),
                    findFragmentShader(params.name) ?: error("Fragment shader ${params.name} not found")
                )

                is ShaderProgramCreationParams.ShadowMap -> createShadowMapShaderProgram(
                    params.name,
                    findVertexShader(params.name) ?: error("Vertex shader ${params.name} not found"),
                    findFragmentShader(params.name) ?: error("Fragment shader ${params.name} not found")
                )

                is ShaderProgramCreationParams.DirectionalLight -> createDirectionalLightShaderProgram(
                    params.name,
                    findVertexShader(params.name) ?: error("Vertex shader ${params.name} not found"),
                    findFragmentShader(params.name) ?: error("Fragment shader ${params.name} not found")
                )
            }
        }
    }

    fun createVertexShader(name: String, source: String) {
        if (vertexShaders.containsKey(name)) {
            throw IllegalArgumentException("Vertex shader $name already exists")
        }

        shadersCreationParams += ShaderCreationParams.VertexShader(name, source)

        val shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        vertexShaders[name] = shader

        openGLErrorDetector.dispatchShaderCompilationError(shader, "createVertexShader")
    }

    fun createFragmentShader(name: String, source: String) {
        if (fragmentShaders.containsKey(name)) {
            throw IllegalArgumentException("Fragment shader $name already exists")
        }

        shadersCreationParams += ShaderCreationParams.FragmentShader(name, source)

        val shader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)

        fragmentShaders[name] = shader

        openGLErrorDetector.dispatchShaderCompilationError(shader, "createFragmentShader")
    }

    fun createAmbientLightShaderProgram(name: String, vertexShader: Int, fragmentShader: Int) {
        if (shaderPrograms.containsKey(name)) {
            throw IllegalArgumentException("Shader program $name already exists")
        }

        shaderProgramsCreationParams += ShaderProgramCreationParams.Ambient(name)

        val shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)

        shaderPrograms[name] = ShaderProgramInfo.AmbientLightShaderProgram(openGLErrorDetector, shaderProgram)

        openGLErrorDetector.dispatchShaderLinkingError(shaderProgram, "createAmbientLightShaderProgram")
        openGLErrorDetector.dispatchOpenGLErrors("createAmbientLightShaderProgram")
    }

    fun createUnlitShaderProgram(name: String, vertexShader: Int, fragmentShader: Int) {
        if (shaderPrograms.containsKey(name)) {
            throw IllegalArgumentException("Shader program $name already exists")
        }

        shaderProgramsCreationParams += ShaderProgramCreationParams.Unlit(name)

        val shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)

        shaderPrograms[name] = ShaderProgramInfo.UnlitShaderProgram(openGLErrorDetector, shaderProgram)

        openGLErrorDetector.dispatchShaderLinkingError(shaderProgram, "createUnlitShaderProgram")
        openGLErrorDetector.dispatchOpenGLErrors("createUnlitShaderProgram")
    }

    fun createShadowMapShaderProgram(name: String, vertexShader: Int, fragmentShader: Int) {
        if (shaderPrograms.containsKey(name)) {
            throw IllegalArgumentException("Shader program $name already exists")
        }

        shaderProgramsCreationParams += ShaderProgramCreationParams.ShadowMap(name)

        val shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)

        shaderPrograms[name] = ShaderProgramInfo.ShadowMapShaderProgram(openGLErrorDetector, shaderProgram)

        openGLErrorDetector.dispatchShaderLinkingError(shaderProgram, "createUnlitShaderProgram")
        openGLErrorDetector.dispatchOpenGLErrors("createUnlitShaderProgram")
    }

    fun createDirectionalLightShaderProgram(name: String, vertexShader: Int, fragmentShader: Int) {
        if (shaderPrograms.containsKey(name)) {
            throw IllegalArgumentException("Shader program $name already exists")
        }

        shaderProgramsCreationParams += ShaderProgramCreationParams.DirectionalLight(name)

        val shaderProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(shaderProgram, vertexShader)
        GLES20.glAttachShader(shaderProgram, fragmentShader)
        GLES20.glLinkProgram(shaderProgram)

        shaderPrograms[name] = ShaderProgramInfo.DirectionalLightShaderProgram(openGLErrorDetector, shaderProgram)

        openGLErrorDetector.dispatchShaderLinkingError(shaderProgram, "createDirectionalLightShaderProgram")
        openGLErrorDetector.dispatchOpenGLErrors("createDirectionalLightShaderProgram")
    }

    fun removeShaderProgram(name: String) {
        val shaderProgramInfo = shaderPrograms.remove(name) ?: error("Shader program $name not found")
        GLES20.glDeleteProgram(shaderProgramInfo.shaderProgram)
        openGLErrorDetector.dispatchOpenGLErrors("removeShaderProgram")
    }

    fun removeAllPrograms() {
        ArrayList<String>().apply { addAll(shaderPrograms.keys) }.forEach { name -> removeShaderProgram(name) }
    }

    fun removeVertexShader(name: String) {
        val vertexShader = vertexShaders.remove(name) ?: error("Vertex shader $name not found")
        GLES20.glDeleteShader(vertexShader)
        openGLErrorDetector.dispatchOpenGLErrors("removeVertexShader")
    }

    fun removeAllVertexShaders() {
        ArrayList<String>().apply { addAll(vertexShaders.keys) }.forEach { name -> removeVertexShader(name) }
    }

    fun removeFragmentShader(name: String) {
        val fragmentShader = fragmentShaders.remove(name) ?: error("Fragment shader $name not found")
        GLES20.glDeleteShader(fragmentShader)
        openGLErrorDetector.dispatchOpenGLErrors("removeFragmentShader")
    }

    fun removeAllFragmentShaders() {
        ArrayList<String>().apply { addAll(fragmentShaders.keys) }.forEach { name -> removeFragmentShader(name) }
    }

    private sealed class ShaderCreationParams(val name: String, val source: String) {
        class VertexShader(name: String, source: String) : ShaderCreationParams(name, source)
        class FragmentShader(name: String, source: String) : ShaderCreationParams(name, source)
    }

    private sealed class ShaderProgramCreationParams(val name: String) {
        class Ambient(name: String) : ShaderProgramCreationParams(name)
        class Unlit(name: String) : ShaderProgramCreationParams(name)
        class ShadowMap(name: String) : ShaderProgramCreationParams(name)
        class DirectionalLight(name: String) : ShaderProgramCreationParams(name)
    }
}