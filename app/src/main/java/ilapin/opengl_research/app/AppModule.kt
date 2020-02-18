package ilapin.opengl_research.app

import android.content.Context
import dagger.Module
import dagger.Provides
import ilapin.opengl_research.ObjectsPool
import org.joml.Quaternionf
import org.joml.Vector3f
import javax.inject.Named
import javax.inject.Singleton

/**
 * @author raynor on 17.02.20.
 */
@Module
class AppModule(private val context: Context) {

    @Provides
    @Singleton
    fun provideVectorsPool(): ObjectsPool<Vector3f> {
        return ObjectsPool { Vector3f() }
    }

    @Provides
    @Singleton
    fun provideQuaternionsPool(): ObjectsPool<Quaternionf> {
        return ObjectsPool { Quaternionf() }
    }

    @Provides
    @Named("Application")
    fun provideAppContext(): Context {
        return context
    }
}