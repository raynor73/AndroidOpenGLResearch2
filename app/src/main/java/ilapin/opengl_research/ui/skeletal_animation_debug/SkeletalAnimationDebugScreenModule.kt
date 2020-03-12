package ilapin.opengl_research.ui.skeletal_animation_debug

import android.content.Context
import android.content.res.Configuration
import dagger.Module
import dagger.Provides
import ilapin.meshloader.MeshLoadingRepository
import ilapin.meshloader.android.ObjMeshLoadingRepository
import ilapin.opengl_research.AndroidDisplayMetricsRepository
import ilapin.opengl_research.ObjectsPool
import ilapin.opengl_research.OpenGLErrorDetector
import ilapin.opengl_research.data.assets_management.FrameBuffersManager
import ilapin.opengl_research.data.assets_management.OpenGLGeometryManager
import ilapin.opengl_research.data.assets_management.OpenGLTexturesManager
import ilapin.opengl_research.data.assets_management.ShadersManager
import ilapin.opengl_research.domain.DisplayMetricsRepository
import ilapin.opengl_research.ui.ActivityScope
import org.joml.Matrix4f
import org.joml.Vector3f
import javax.inject.Named

/**
 * @author Игорь on 12.03.2020.
 */
@Module
class SkeletalAnimationDebugScreenModule(private val activity: SkeletalAnimationDebugActivity) {

    @Provides
    @ActivityScope
    @Named("Activity")
    fun provideActivityContext(): Context {
        return activity
    }

    @Provides
    @ActivityScope
    fun provideOpenGLErrorDetector(): OpenGLErrorDetector {
        return OpenGLErrorDetector()
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
    fun provideMeshLoadingRepository(
        @Named("Activity") context: Context
    ): MeshLoadingRepository {
        return ObjMeshLoadingRepository(context)
    }

    @Provides
    @ActivityScope
    fun provideDisplayMetricsRepository(
        @Named("Activity") context: Context
    ): DisplayMetricsRepository {
        return AndroidDisplayMetricsRepository(context)
    }

    @Provides
    @ActivityScope
    fun provideRenderer(
        @Named("Activity") context: Context,
        openGLErrorDetector: OpenGLErrorDetector,
        vectorsPool: ObjectsPool<Vector3f>,
        matrixPool: ObjectsPool<Matrix4f>,
        shadersManager: ShadersManager,
        frameBuffersManager: FrameBuffersManager,
        texturesManager: OpenGLTexturesManager,
        geometryManager: OpenGLGeometryManager,
        displayMetricsRepository: DisplayMetricsRepository,
        meshLoadingRepository: MeshLoadingRepository
    ): SkeletalAnimationDebugRenderer? {
        return if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            SkeletalAnimationDebugRenderer(
                context,
                openGLErrorDetector,
                matrixPool,
                vectorsPool,
                shadersManager,
                frameBuffersManager,
                texturesManager,
                geometryManager,
                displayMetricsRepository,
                meshLoadingRepository
            )
        } else {
            null
        }
    }
}