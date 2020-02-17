package ilapin.opengl_research.data.scripting_engine

import ilapin.common.android.log.L
import ilapin.opengl_research.app.App.Companion.LOG_TAG
import ilapin.opengl_research.domain.scripting_engine.ScriptingEngine
import org.mozilla.javascript.Context
import org.mozilla.javascript.ErrorReporter
import org.mozilla.javascript.EvaluatorException

/**
 * @author ilapin on 17.02.20.
 */
class RhinoScriptingEngine : ScriptingEngine {

    private val context = Context.enter()
    private val scope = context.initStandardObjects()

    init {
        context.optimizationLevel = -1
        context.errorReporter = object : ErrorReporter {

            override fun warning(
                message: String,
                sourceName: String,
                line: Int,
                lineSource: String,
                lineOffset: Int
            ) {
                L.e(LOG_TAG, "JavaScript warning: $message in $sourceName at $line:$lineOffset ($lineSource)")
            }

            override fun runtimeError(
                message: String,
                sourceName: String,
                line: Int,
                lineSource: String,
                lineOffset: Int
            ): EvaluatorException {
                return EvaluatorException("JavaScript runtime error: $message in $sourceName at $line:$lineOffset ($lineSource)")
            }

            override fun error(
                message: String,
                sourceName: String,
                line: Int,
                lineSource: String,
                lineOffset: Int
            ) {
                L.e(LOG_TAG, "JavaScript error: $message in $sourceName at $line:$lineOffset ($lineSource)")
            }
        }

        val script = "var someValue = 1; function update() { java.lang.System.out.println('Hello world ' + someValue); someValue++; }"
        context.evaluateString(scope, script, "SceneScript", 1, null)
    }

    override fun update(dt: Float) {
        val updateFunction = scope.get("update", scope) as org.mozilla.javascript.Function
        updateFunction.call(context, scope, scope, emptyArray())
        /*println(Context.jsToJava(result, Int::class.javaPrimitiveType))
        val result: Any = updateFunction.call(
            context, scope, scope, arrayOf<Any>(2, 3)
        )
        println(Context.jsToJava(result, Int::class.javaPrimitiveType))*/
    }

    override fun deinit() {
        Context.exit()
    }
}