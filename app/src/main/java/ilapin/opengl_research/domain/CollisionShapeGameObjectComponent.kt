package ilapin.opengl_research.domain

import ilapin.engine3d.GameObjectComponent
import org.ode4j.ode.DGeom

/**
 * @author raynor on 10.02.20.
 */
class CollisionShapeGameObjectComponent(
    val collisionShape: DGeom
) : GameObjectComponent()