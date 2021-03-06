package ilapin.opengl_research.domain.scripting_engine

/**
 * @author ilapin on 17.02.20.
 */
interface ScriptingEngine {

    fun loadScripts(scripts: List<String>)

    fun update(dt: Float)

    fun deinit()
}