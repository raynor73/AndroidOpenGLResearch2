package ilapin.opengl_research.ui

import dagger.Subcomponent

/**
 * @author raynor on 18.02.20.
 */
@ActivityScope
@Subcomponent(modules = [MainScreenModule::class])
interface MainScreenComponent {

    fun inject(activity: MainActivity)
}