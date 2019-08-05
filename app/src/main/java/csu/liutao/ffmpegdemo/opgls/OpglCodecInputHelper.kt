package csu.liutao.ffmpegdemo.opgls

import android.opengl.*
import android.view.Surface
import android.opengl.EGL14.*
import csu.liutao.ffmpegdemo.opgls.programs.IInputTextureProgram
import java.lang.Exception

class OpglCodecInputHelper(val inputSurface : Surface) {
    private var eglSurface: EGLSurface? = null
    private var eglDisplay: EGLDisplay? = null
    private var eglContext: EGLContext? = null

    init {
        eglDisplay = eglGetDisplay(EGL_DEFAULT_DISPLAY)
        val configs = arrayOfNulls<EGLConfig>(1)
        val conAttr = intArrayOf(
            EGL_RED_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_ALPHA_SIZE, 8,
            EGL_NONE
        )
        val num = IntArray(1)
        if (!eglChooseConfig(eglDisplay,conAttr, 0, configs, 0, configs.size, num, 0)) throw Exception("choose config failed")
        val surfaceAttr = intArrayOf(
            EGL_RENDER_BUFFER, EGL_BACK_BUFFER,
            EGL_NONE
        )
        eglSurface = eglCreateWindowSurface(eglDisplay, configs[0], inputSurface, surfaceAttr, 0)
        if (eglSurface == EGL_NO_SURFACE) throw Exception("create window surface error")

        val contextAttr = intArrayOf(
            EGL_CONTEXT_CLIENT_VERSION, 3,
            EGL_NONE
        )
        eglContext = eglCreateContext(eglDisplay, configs[0], eglGetCurrentContext(), contextAttr, 0)
        if (eglContext == EGL_NO_CONTEXT) throw Exception("create context error")
    }

    fun draw(program : IInputTextureProgram) {
        eglMakeCurrent(eglDisplay,eglSurface, eglSurface, eglContext)
        program.draw()
        eglSwapBuffers(eglDisplay, eglSurface)
    }

    fun destory() {
        eglMakeCurrent(eglDisplay, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT)
        eglSurface = null
        eglContext = null
        eglTerminate(eglDisplay)
        eglDisplay = null
    }
}