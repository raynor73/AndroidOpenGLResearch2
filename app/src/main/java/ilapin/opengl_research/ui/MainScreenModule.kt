package ilapin.opengl_research.ui

import android.content.Context
import android.content.res.Configuration
import dagger.Module
import dagger.Provides
import ilapin.common.messagequeue.MessageQueue
import ilapin.opengl_research.AndroidTouchEventsRepository
import ilapin.opengl_research.JoystickViewJoystick
import ilapin.opengl_research.ObjectsPool
import ilapin.opengl_research.domain.Joystick
import ilapin.opengl_research.domain.PlayerController
import ilapin.opengl_research.domain.ScrollController
import ilapin.opengl_research.domain.TouchEventsRepository
import org.joml.Quaternionf
import org.joml.Vector3f
import javax.inject.Named

/**
 * @author raynor on 18.02.20.
 */
@Module
class MainScreenModule(private val activity: MainActivity) {

    @Provides
    @ActivityScope
    fun provideMessageQueue(): MessageQueue {
        return MessageQueue()
    }

    @Provides
    @ActivityScope
    @Named("Activity")
    fun provideActivityContext(): Context {
        return activity
    }

    @Provides
    @ActivityScope
    fun provideAndroidTouchEventRepository(messageQueue: MessageQueue): AndroidTouchEventsRepository {
        return AndroidTouchEventsRepository(messageQueue)
    }

    @Provides
    @ActivityScope
    fun provideTouchEventsRepository(
        androidTouchEventsRepository: AndroidTouchEventsRepository
    ): TouchEventsRepository {
        return androidTouchEventsRepository
    }

    @Provides
    @ActivityScope
    fun provideScrollController(touchEventsRepository: TouchEventsRepository): ScrollController {
        return ScrollController(touchEventsRepository)
    }

    @Provides
    @ActivityScope
    @Named("LeftJoystick")
    fun provideLeftJoystick(messageQueue: MessageQueue): Joystick {
        return JoystickViewJoystick(messageQueue, MainActivity.LEFT_JOYSTICK_ID)
    }

    @Provides
    @ActivityScope
    @Named("RightJoystick")
    fun provideRightJoystick(messageQueue: MessageQueue): Joystick {
        return JoystickViewJoystick(messageQueue, MainActivity.RIGHT_JOYSTICK_ID)
    }

    @Provides
    @ActivityScope
    fun providePlayerController(
        @Named("LeftJoystick") leftJoystick: Joystick,
        @Named("RightJoystick") rightJoystick: Joystick
    ): PlayerController {
        return PlayerController(leftJoystick, rightJoystick)
    }

    @Provides
    @ActivityScope
    fun provideRenderer(
        @Named("Activity") context: Context,
        messageQueue: MessageQueue,
        verticesPool: ObjectsPool<Vector3f>,
        quaternionsPool: ObjectsPool<Quaternionf>,
        androidTouchEventsRepository: AndroidTouchEventsRepository,
        scrollController: ScrollController,
        playerController: PlayerController
    ): GLSurfaceViewRenderer? {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GLSurfaceViewRenderer(
                context,
                messageQueue,
                verticesPool,
                quaternionsPool,
                androidTouchEventsRepository,
                scrollController,
                playerController
            )
        } else {
            null
        }
    }
}