package ilapin.opengl_research.domain

/**
 * @author raynor on 18.02.20.
 */
class MeshStorage {

    private val storage = HashMap<String, Mesh>()

    fun putMesh(name: String, mesh: Mesh) {
        if (storage.containsKey(name)) {
            error("Mesh $name already stored")
        }

        storage[name] = mesh
    }

    fun findMesh(name: String): Mesh {
        return storage[name] ?: error("Mesh $name not found")
    }

    fun removeMesh(name: String) {
        storage.remove(name) ?: error("Mesh $name not found")
    }

    fun removeAllMeshes() {
        storage.clear()
    }
}