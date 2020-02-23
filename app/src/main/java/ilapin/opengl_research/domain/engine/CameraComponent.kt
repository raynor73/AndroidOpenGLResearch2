package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent

abstract class CameraComponent(
    layerNames: List<String>
) : GameObjectComponent() {

    private val _layerNames = ArrayList<String>()

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