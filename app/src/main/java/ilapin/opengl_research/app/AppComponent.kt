package ilapin.opengl_research.app

import dagger.Component
import ilapin.opengl_research.ui.MainActivity
import ilapin.opengl_research.ui.RendererComponent
import javax.inject.Singleton

/**
 * @author raynor on 17.02.20.
 */
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun rendererComponent(): RendererComponent

    fun inject(activity: MainActivity)
}