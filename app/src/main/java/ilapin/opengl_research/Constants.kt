package ilapin.opengl_research

import org.joml.Vector3f
import org.joml.Vector3fc

/**
 * @author ilapin on 26.01.2020.
 */
const val BYTES_IN_FLOAT = 4
const val BYTES_IN_SHORT = 2
const val VERTICES_IN_TRIANGLE = 3
const val VERTEX_COORDINATE_COMPONENTS = 3
const val NORMAL_COMPONENTS = 3
const val TEXTURE_COORDINATE_COMPONENTS = 2
const val MAX_JOINTS = 50
const val NUMBER_OF_JOINT_INDICES = 3
const val NUMBER_OF_JOINT_WEIGHTS = 3
const val VERTEX_COMPONENTS =
    VERTEX_COORDINATE_COMPONENTS +
            NORMAL_COMPONENTS +
            TEXTURE_COORDINATE_COMPONENTS +
            NUMBER_OF_JOINT_INDICES +
            NUMBER_OF_JOINT_WEIGHTS
const val Z_NEAR = 0.1f
const val Z_FAR = 1000f
val CAMERA_LOOK_AT_DIRECTION: Vector3fc = Vector3f(0f, 0f, -1f)
val CAMERA_UP_DIRECTION: Vector3fc = Vector3f(0f, 1f, 0f)
const val GLOBAL_DIRECTIONAL_LIGHT_DISTANCE_FROM_VIEWER = 100f
const val GLOBAL_DIRECTIONAL_LIGHT_SHADOW_SIZE = 15f
const val NANOS_IN_SECOND = 1e9f
const val NANOS_IN_MILLISECOND = 1000
