package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent
import ilapin.opengl_research.Z_FAR
import ilapin.opengl_research.Z_NEAR

abstract class CameraComponent(
    layerNames: List<String>
) : GameObjectComponent() {

    private val _layerNames = ArrayList<String>()

    var zNear = Z_NEAR
    var zFar = Z_FAR

    var viewportX = 0f
    var viewportY = 0f
    var viewportWidth = 1f
    var viewportHeight = 1f

    var layerNames: List<String>
        get() = _layerNames
        set(value) {
            _layerNames.clear()
            _layerNames += value
        }

    init {
        _layerNames += layerNames
    }
}