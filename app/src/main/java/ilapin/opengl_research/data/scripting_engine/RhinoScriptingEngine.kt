package ilapin.opengl_research.data.scripting_engine

import ilapin.common.android.log.L
import ilapin.opengl_research.ObjectsPool
import ilapin.opengl_research.app.App.Companion.LOG_TAG
import ilapin.opengl_research.domain.DisplayMetricsRepository
import ilapin.opengl_research.domain.Scene2
import ilapin.opengl_research.domain.TouchEventsRepository
import ilapin.opengl_research.domain.scripting_engine.ScriptingEngine
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
    private var onGoingToForegroundJsFunction: org.mozilla.javascript.Function? = null
    private var onGoingToBackgroundJsFunction: org.mozilla.javascript.Function? = null

    var touchEventsRepository: TouchEventsRepository? = null
    var scene: Scene2? = null
    var displayMetricsRepository: DisplayMetricsRepository? = null
    var vectorsPool: ObjectsPool<Vector3f>? = null
    var quaternionsPool: ObjectsPool<Quaternionf>? = null

    override fun evaluateScript(script: String) {
        context.evaluateString(scope, script, "SceneScript", 1, null)

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

    fun onGoingToForeground() {
        onGoingToForegroundJsFunction?.call(context, scope, scope, emptyArray())
    }

    fun onGoingToBackground() {
        onGoingToBackgroundJsFunction?.call(context, scope, scope, emptyArray())
    }

    override fun deinit() {
        Context.exit()
    }
}