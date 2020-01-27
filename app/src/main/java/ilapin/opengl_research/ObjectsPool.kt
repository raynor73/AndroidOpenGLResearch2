package ilapin.opengl_research

import java.util.*

/**
 * @author raynor on 27.01.20.
 */
class ObjectsPool<T>(private val createObject: () -> T) {

    private val pool = LinkedList<T>()

    fun obtain(): T {
        return if (pool.isEmpty()) {
            createObject()
        } else {
            pool.removeFirst()
        }
    }

    fun recycle(o: T) {
        pool += o
    }
}