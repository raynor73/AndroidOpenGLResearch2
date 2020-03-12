package ilapin.opengl_research.ui.skeletal_animation_debug

import dagger.Subcomponent
import ilapin.opengl_research.ui.ActivityScope

/**
 * @author Игорь on 12.03.2020.
 */
@ActivityScope
@Subcomponent(modules = [SkeletalAnimationDebugScreenModule::class])
interface SkeletalAnimationDebugScreenComponent {

    fun inject(activity: SkeletalAnimationDebugActivity)
}