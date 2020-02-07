package ilapin.opengl_research.domain

/**
 * @author raynor on 07.02.20.
 */
interface Joystick {

    val position: Position

    class Position(val x: Float, val y: Float)
}