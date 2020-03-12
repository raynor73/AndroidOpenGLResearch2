package ilapin.opengl_research.app

import dagger.Component
import ilapin.opengl_research.ui.MainScreenComponent
import ilapin.opengl_research.ui.MainScreenModule
import ilapin.opengl_research.ui.skeletal_animation_debug.SkeletalAnimationDebugScreenComponent
import ilapin.opengl_research.ui.skeletal_animation_debug.SkeletalAnimationDebugScreenModule
import javax.inject.Singleton

/**
 * @author raynor on 17.02.20.
 */
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun mainScreenComponent(mainScreenModule: MainScreenModule): MainScreenComponent

    fun skeletalAnimationDebugScreenComponent(
        skeletalAnimationDebugScreenModule: SkeletalAnimationDebugScreenModule
    ): SkeletalAnimationDebugScreenComponent
}