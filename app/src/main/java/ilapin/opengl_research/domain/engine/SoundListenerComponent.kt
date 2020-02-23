package ilapin.opengl_research.domain.engine

import ilapin.engine3d.GameObjectComponent
import ilapin.engine3d.TransformationComponent
import ilapin.opengl_research.domain.sound.SoundScene

/**
 * @author raynor on 23.02.20.
 */
class SoundListenerComponent(private val soundScene: SoundScene) : GameObjectComponent() {

    override fun update() {
        super.update()

        val transform = gameObject?.getComponent(TransformationComponent::class.java)
            ?: error("No transformation component")

        soundScene.updateSoundListenerPosition(transform.position)
        soundScene.updateSoundListenerRotation(transform.rotation)
    }
}