package ilapin.opengl_research

import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author ilapin on 26.01.2020.
 */
const val BYTES_IN_FLOAT = 4
const val BYTES_IN_SHORT = 2
const val VERTEX_COORDINATE_COMPONENTS = 3
const val NORMAL_COMPONENTS = 3
const val TEXTURE_COORDINATE_COMPONENTS = 2
const val VERTEX_COMPONENTS = VERTEX_COORDINATE_COMPONENTS + NORMAL_COMPONENTS + TEXTURE_COORDINATE_COMPONENTS
const val Z_NEAR = 0.1f
const val Z_FAR = 1000f
val CAMERA_LOOK_AT_DIRECTION: Vector3fc = Vector3f(0f, 0f, -1f)
val CAMERA_UP_DIRECTION: Vector3fc = Vector3f(0f, 1f, 0f)
const val GLOBAL_DIRECTIONAL_LIGHT_DISTANCE_FROM_VIEWER = 100f
const val GLOBAL_DIRECTIONAL_LIGHT_SHADOW_SIZE = 100f
