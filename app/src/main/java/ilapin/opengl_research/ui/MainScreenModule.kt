package ilapin.opengl_research.ui

import android.content.Context
import android.content.res.Configuration
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import ilapin.common.android.time.LocalTimeRepository
import ilapin.common.messagequeue.MessageQueue
import ilapin.common.time.TimeRepository
import ilapin.meshloader.MeshLoadingRepository
import ilapin.meshloader.android.ObjMeshLoadingRepository
import ilapin.opengl_research.AndroidDisplayMetricsRepository
import ilapin.opengl_research.AndroidTouchEventsRepository
import ilapin.opengl_research.ObjectsPool
import ilapin.opengl_research.OpenGLErrorDetector
import ilapin.opengl_research.data.assets_management.FrameBuffersManager
import ilapin.opengl_research.data.assets_management.OpenGLGeometryManager
import ilapin.opengl_research.data.assets_management.OpenGLTexturesManager
import ilapin.opengl_research.data.assets_management.ShadersManager
import ilapin.opengl_research.data.scene_loader.AndroidAssetsSceneLoader
import ilapin.opengl_research.data.scene_loader.ComponentDeserializer
import ilapin.opengl_research.data.scene_loader.ComponentDto
import ilapin.opengl_research.data.scripting_engine.RhinoScriptingEngine
import ilapin.opengl_research.data.sound.SoundPoolSoundClipsRepository
import ilapin.opengl_research.domain.engine.GesturesDispatcher
import ilapin.opengl_research.domain.MeshStorage
import ilapin.opengl_research.domain.TouchEventsRepository
import ilapin.opengl_research.domain.physics_engine.PhysicsEngine
import ilapin.opengl_research.domain.scene_loader.SceneLoader
import ilapin.opengl_research.domain.sound.SoundClipsRepository
import ilapin.opengl_research.domain.sound.SoundScene
import ilapin.opengl_research.domain.sound_2d.SoundScene2D
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
    fun provideOpenGLErrorDetector(): OpenGLErrorDetector {
        return OpenGLErrorDetector()
    }

    @Provides
    @ActivityScope
    fun provideOpenGLTexturesManager(
        @Named("Activity") context: Context,
        openGLErrorDetector: OpenGLErrorDetector
    ): OpenGLTexturesManager {
        return OpenGLTexturesManager(context, openGLErrorDetector)
    }

    @Provides
    @ActivityScope
    fun provideFrameBuffersManager(
        texturesManager: OpenGLTexturesManager,
        openGLErrorDetector: OpenGLErrorDetector
    ): FrameBuffersManager {
        return FrameBuffersManager(texturesManager, openGLErrorDetector)
    }

    @Provides
    @ActivityScope
    fun provideOpenGLGeometryManager(openGLErrorDetector: OpenGLErrorDetector): OpenGLGeometryManager {
        return OpenGLGeometryManager(openGLErrorDetector)
    }

    @Provides
    @ActivityScope
    fun provideShadersManager(openGLErrorDetector: OpenGLErrorDetector): ShadersManager {
        return ShadersManager(openGLErrorDetector)
    }

    @Provides
    @ActivityScope
    fun provideMeshStorage(): MeshStorage {
        return MeshStorage()
    }

    @Provides
    @ActivityScope
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(ComponentDto::class.java, ComponentDeserializer())
            .setLenient()
            .create()
    }

    @Provides
    @ActivityScope
    fun provideMeshLoadingRepository(
        @Named("Activity") context: Context
    ): MeshLoadingRepository {
        return ObjMeshLoadingRepository(context)
    }

    @Provides
    @ActivityScope
    fun provideDisplayMetricsRepository(
        @Named("Activity") context: Context
    ): AndroidDisplayMetricsRepository {
        return AndroidDisplayMetricsRepository(context)
    }

    @Provides
    fun physicsEngine(): PhysicsEngine {
        return PhysicsEngine()
    }

    @Provides
    fun scriptingEngine(): RhinoScriptingEngine {
        return RhinoScriptingEngine()
    }

    @Provides
    @ActivityScope
    fun provideTimeRepository(): TimeRepository {
        return LocalTimeRepository()
    }

    @Provides
    @ActivityScope
    fun provideGesturesDispatcher(): GesturesDispatcher {
        return GesturesDispatcher()
    }

    @Provides
    @ActivityScope
    fun provideSoundClipsRepository(@Named("Activity") context: Context): SoundClipsRepository {
        return SoundPoolSoundClipsRepository(context)
    }

    @Provides
    @ActivityScope
    fun provideSoundScene(
        vectorsPool: ObjectsPool<Vector3f>,
        timeRepository: TimeRepository,
        soundClipsRepository: SoundClipsRepository
    ): SoundScene {
        return SoundScene(vectorsPool, timeRepository, soundClipsRepository)
    }

    @Provides
    @ActivityScope
    fun provideSoundScene2D(
        timeRepository: TimeRepository,
        soundClipsRepository: SoundClipsRepository
    ): SoundScene2D {
        return SoundScene2D(soundClipsRepository, timeRepository)
    }

    @Provides
    @ActivityScope
    fun provideSceneLoader(
        @Named("Activity") context: Context,
        gson: Gson,
        meshLoadingRepository: MeshLoadingRepository,
        texturesManager: OpenGLTexturesManager,
        geometryManager: OpenGLGeometryManager,
        meshStorage: MeshStorage,
        vectorsPool: ObjectsPool<Vector3f>,
        displayMetricsRepository: AndroidDisplayMetricsRepository,
        openGLErrorDetector: OpenGLErrorDetector,
        gesturesDispatcher: GesturesDispatcher,
        soundClipsRepository: SoundClipsRepository,
        soundScene: SoundScene,
        soundScene2D: SoundScene2D
    ): SceneLoader {
        return AndroidAssetsSceneLoader(
            context,
            gson,
            meshLoadingRepository,
            texturesManager,
            geometryManager,
            meshStorage,
            vectorsPool,
            displayMetricsRepository,
            openGLErrorDetector,
            gesturesDispatcher,
            soundClipsRepository,
            soundScene,
            soundScene2D
        )
    }

    @Provides
    @ActivityScope
    fun provideRenderer(
        @Named("Activity") context: Context,
        messageQueue: MessageQueue,
        androidTouchEventsRepository: AndroidTouchEventsRepository,
        openGLErrorDetector: OpenGLErrorDetector,
        frameBuffersManager: FrameBuffersManager,
        geometryManager: OpenGLGeometryManager,
        texturesManager: OpenGLTexturesManager,
        shadersManager: ShadersManager,
        meshStorage: MeshStorage,
        sceneLoader: SceneLoader,
        scriptingEngine: RhinoScriptingEngine,
        timeRepository: TimeRepository,
        displayMetricsRepository: AndroidDisplayMetricsRepository,
        vectorsPool: ObjectsPool<Vector3f>,
        quaternionsPool: ObjectsPool<Quaternionf>,
        gesturesDispatcher: GesturesDispatcher,
        soundScene: SoundScene,
        soundScene2D: SoundScene2D,
        soundClipsRepository: SoundClipsRepository
    ): GLSurfaceViewRenderer? {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            GLSurfaceViewRenderer(
                context,
                messageQueue,
                openGLErrorDetector,
                frameBuffersManager,
                geometryManager,
                texturesManager,
                shadersManager,
                meshStorage,
                androidTouchEventsRepository,
                sceneLoader,
                scriptingEngine,
                timeRepository,
                displayMetricsRepository,
                vectorsPool,
                quaternionsPool,
                gesturesDispatcher,
                soundScene,
                soundScene2D,
                soundClipsRepository
            )
        } else {
            null
        }
    }
}