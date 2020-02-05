package ilapin.opengl_research.domain

import org.joml.Vector2f
import org.joml.Vector3f

/**
 * @author ilapin on 25.01.2020.
 */
class MeshFactory {

    companion object {

        fun createQuad(): Mesh {
            return Mesh(
                listOf(
                    Mesh.Vertex(
                        Vector3f(-0.5f, -0.5f, 0f),
                        Vector3f(1f, 1f, 1f),
                        Vector2f(0f, 0f)
                    ),
                    Mesh.Vertex(
                        Vector3f(-0.5f, 0.5f, 0f),
                        Vector3f(1f, 1f, 1f),
                        Vector2f(0f, 1f)
                    ),
                    Mesh.Vertex(
                        Vector3f(0.5f, 0.5f, 0f),
                        Vector3f(1f, 1f, 1f),
                        Vector2f(1f, 1f)
                    ),
                    Mesh.Vertex(
                        Vector3f(0.5f, -0.5f, 0f),
                        Vector3f(1f, 1f, 1f),
                        Vector2f(1f, 0f)
                    )
                ),
                listOf(0, 3, 2, 2, 1, 0)
            )
        }

        fun createTriangle(): Mesh {
            return Mesh(
                listOf(
                    Mesh.Vertex(
                        Vector3f(0f, 0.5f, 0f),
                        Vector3f(1f, 1f, 1f),
                        Vector2f(0.5f, 1f)
                    ),
                    Mesh.Vertex(
                        Vector3f(-0.5f, -0.5f, 0f),
                        Vector3f(1f, 1f, 1f),
                        Vector2f(0f, 0f)
                    ),
                    Mesh.Vertex(
                        Vector3f(0.5f, -0.5f, 0f),
                        Vector3f(1f, 1f, 1f),
                        Vector2f(1f, 0f)
                    )
                ),
                listOf(0, 1, 2)
            )
        }
    }
}