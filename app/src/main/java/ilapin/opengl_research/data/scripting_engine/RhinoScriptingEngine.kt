package ilapin.opengl_research.data.scripting_engine

import ilapin.common.android.log.L
import ilapin.opengl_research.ObjectsPool
import ilapin.opengl_research.app.App.Companion.LOG_TAG
import ilapin.opengl_research.domain.AppPriorityReporter
import ilapin.opengl_research.domain.DisplayMetricsRepository
import ilapin.opengl_research.domain.Scene2
import ilapin.opengl_research.domain.TouchEventsRepository
import ilapin.opengl_research.domain.physics_engine.PhysicsEngine
import ilapin.opengl_research.domain.scripting_engine.ScriptingEngine
import ilapin.opengl_research.domain.sound.SoundClipsRepository
import ilapin.opengl_research.domain.sound.SoundScene
import ilapin.opengl_research.domain.sound_2d.SoundScene2D
import org.joml.Quaternionf
import org.joml.Vector3f
import org.mozilla.javascript.Context
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException
import org.mozilla.javascript.ScriptableObject

/**
 * @author ilapin on 17.02.20.
 */
class RhinoScriptingEngine : ScriptingEngine {

    private val context: Context by lazy {
        val context = Context.enter()

        context.optimizationLevel = -1
        context.errorReporter = object : ErrorReporter {

            override fun warning(
                message: String,
                sourceName: String,
                line: Int,
                lineSource: String?,
                lineOffset: Int
            ) {
                L.e(LOG_TAG, "JavaScript warning: $message in $sourceName at $line:$lineOffset ($lineSource)")
            }

            override fun runtimeError(
                message: String,
                sourceName: String,
                line: Int,
                lineSource: String?,
                lineOffset: Int
            ): EvaluatorException {
                return EvaluatorException("JavaScript runtime error: $message in $sourceName at $line:$lineOffset ($lineSource)")
            }

            override fun error(
                message: String,
                sourceName: String,
                line: Int,
                lineSource: String?,
                lineOffset: Int
            ) {
                L.e(LOG_TAG, "JavaScript error: $message in $sourceName at $line:$lineOffset ($lineSource)")
            }
        }

        context
    }

    private val scope: ScriptableObject by lazy { context.initStandardObjects() }

    private var updateJsFunction: org.mozilla.javascript.Function? = null

    var appPriorityReporter: AppPriorityReporter? = null
    var touchEventsRepository: TouchEventsRepository? = null
    var scene: Scene2? = null
    var displayMetricsRepository: DisplayMetricsRepository? = null
    var vectorsPool: ObjectsPool<Vector3f>? = null
    var quaternionsPool: ObjectsPool<Quaternionf>? = null
    var soundClipsRepository: SoundClipsRepository? = null
    var soundScene: SoundScene? = null
    var soundScene2D: SoundScene2D? = null
    var physicsEngine: PhysicsEngine? = null

    override fun loadScripts(scripts: List<String>) {
        scripts.forEachIndexed { i, script -> context.evaluateString(scope, script, "SceneScript #$i", 1, null) }

        ScriptableObject.putProperty(
            scope,
            "appPriorityReporter",
            Context.javaToJS(appPriorityReporter, scope)
        )

        ScriptableObject.putProperty(
            scope,
            "touchEventsRepository",
            Context.javaToJS(touchEventsRepository, scope)
        )

        ScriptableObject.putProperty(
            scope,
            "scene",
            Context.javaToJS(scene, scope)
        )

        ScriptableObject.putProperty(
            scope,
            "displayMetricsRepository",
            Context.javaToJS(displayMetricsRepository, scope)
        )

        ScriptableObject.putProperty(
            scope,
            "vectorsPool",
            Context.javaToJS(vectorsPool, scope)
        )

        ScriptableObject.putProperty(
            scope,
            "quaternionsPool",
            Context.javaToJS(quaternionsPool, scope)
        )

        ScriptableObject.putProperty(
            scope,
            "soundClipsRepository",
            Context.javaToJS(soundClipsRepository, scope)
        )

        ScriptableObject.putProperty(
            scope,
            "soundScene",
            Context.javaToJS(soundScene, scope)
        )

        ScriptableObject.putProperty(
            scope,
            "soundScene2D",
            Context.javaToJS(soundScene2D, scope)
        )

        ScriptableObject.putProperty(
            scope,
            "physicsEngine",
            Context.javaToJS(physicsEngine, scope)
        )

        val startFunction = scope
            .get("start", scope)
            .takeIf { it is org.mozilla.javascript.Function } as org.mozilla.javascript.Function?
        startFunction?.call(context, scope, scope, emptyArray())

        updateJsFunction = scope
            .get("update", scope)
            .takeIf { it is org.mozilla.javascript.Function } as org.mozilla.javascript.Function?
    }

    override fun update(dt: Float) {
        updateJsFunction?.call(context, scope, scope, arrayOf(dt))
    }

    override fun deinit() {
        Context.exit()
    }
}