package ilapin.opengl_research.app

import dagger.Component
import ilapin.opengl_research.ui.MainScreenComponent
import ilapin.opengl_research.ui.MainScreenModule
import javax.inject.Singleton

/**
 * @author raynor on 17.02.20.
 */
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {

    fun mainScreenComponent(mainScreenModule: MainScreenModule): MainScreenComponent
}