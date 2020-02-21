package ilapin.opengl_research.domain

/**
 * @author raynor on 21.02.20.
 */
interface AppPriorityReporter {

    val state: AppState

    enum class AppState {
        FOREGROUND, BACKGROUND
    }
}