package ilapin.opengl_research.ui

import dagger.Subcomponent

/**
 * @author raynor on 17.02.20.
 */
@RendererScope
@Subcomponent(modules = [RendererModule::class])
interface RendererComponent {

    fun inject(renderer: GLSurfaceViewRenderer)
}