package de.tombeckmann.flappy3d;

import java.util.Random;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.content.Context;
import android.content.res.Resources;

import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

public class SurfaceView extends GLSurfaceView {

	private static float[] PLANE_VERTICES = { -0.5f, 0.0f, -0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0.0f, 0.5f, -0.5f, 0.0f, 0.5f };
	private static float[] PLANE_NORMALS = { 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f };
	private static short[] PLANE_INDICES = { 2, 1, 0, 3, 2, 0 };

	private Resources mResources;

	public Object mPipe;
	public Object mPlane;

	private float[] mProjection = new float[16];
	private float[] mView = new float[16];
	private float[] mModel = new float[16];

	private float[] mShadowProjection = new float[16];
	private float[] mShadowView = new float[16];
	private float[] mShadowMatrix = new float[16];

	private float mPipeLocation = 0;

	// private long mLastTime;
	private long mStartTime;

	private int mWidth;
	private int mHeight;

	private boolean mShowLightBuffer = false;

	public SurfaceView(Context context, Resources resources) {
		super(context);

		mResources = resources;

		setEGLContextClientVersion(3);
		//setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		setRenderer(new GLSurfaceView.Renderer() {
			final int SHADOW_MAP_RESOLUTION = 1024;
			final int SHADOW_WINDOW_SIZE = 20;

			public float[] lightDirection = { -1, -1, -1 };

			public int mDepthTexture;
			public int mShadowBuffer;

			/**
			 * Basic configuration for the project, initializes data and sets constant GL pipeline states
			 */
			public void onSurfaceCreated(GL10 unused, EGLConfig config) {
				GLES30.glClearColor(0.262745f, 0.823529f, 0.980392f, 1.0f);
				GLES30.glEnable(GLES30.GL_DEPTH_TEST);
				GLES30.glEnable(GLES30.GL_CULL_FACE);

				// create buffers for shadow rendering
				int[] textures = new int[1];
				int[] framebuffers = new int[1];
				GLES30.glGenTextures(1, textures, 0);
				GLES30.glGenFramebuffers(1, framebuffers, 0);

				mDepthTexture = textures[0];
				mShadowBuffer = framebuffers[0];

				GLES30.glActiveTexture(GLES30.GL_TEXTURE5);
				GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mDepthTexture);
				GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
				GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
				GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
				GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
				GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT,
					SHADOW_MAP_RESOLUTION, SHADOW_MAP_RESOLUTION, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_UNSIGNED_INT, null);

				GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mShadowBuffer);
				GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT,
						GLES30.GL_TEXTURE_2D, mDepthTexture, 0);

				if (GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER) != GLES30.GL_FRAMEBUFFER_COMPLETE)
					Log.e("VERTICES", "Failed to assemble framebuffer!");

				GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
				GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

				// setup shadow/light matrices
				Matrix.orthoM(mShadowProjection, 0, -SHADOW_WINDOW_SIZE, SHADOW_WINDOW_SIZE, -SHADOW_WINDOW_SIZE, SHADOW_WINDOW_SIZE,
					-SHADOW_WINDOW_SIZE, SHADOW_WINDOW_SIZE);
				Matrix.setLookAtM(mShadowView, 0, 0, 0, 0, lightDirection[0], lightDirection[1], lightDirection[2], 0, 1, 0);

				// load default objects, like the pipe and a generic plane
				mPipe = new Object(mResources.openRawResource(R.raw.pipe));
				mPipe.setColor(1.0f, 0.415686f, 0.0f, 1.0f);

				mPlane = new Object(PLANE_VERTICES, PLANE_NORMALS, PLANE_INDICES);
				//mPlane = new Object(mResources.openRawResource(R.raw.plane));
				mPlane.setColor(0.717647f, 1.0f, 0.262745f, 1.0f);

				// our own view matrix
				Matrix.setLookAtM(mView, 0,
					0, 5, -9,
					0, 5, 0,
					0, 1, 0);

				mStartTime = System.nanoTime();
				// mLastTime = mStartTime;
			}

			/**
			 * Main function for drawing, each frame is drawn here and logic is updated
			 * TODO: move logic out into a separate function
			 */
			public void onDrawFrame(GL10 unused) {
				long now = System.nanoTime();
				// float deltaTime = mLastTime - now;
				// mLastTime = now;

				float offsetX = (now - mStartTime) % 2000000000L / 1000000000.0f * 6.0f - 6.0f;

				// shadow pass
				if (!mShowLightBuffer) {
					GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, mShadowBuffer);
					GLES30.glColorMask(false, false, false, false);
				}

				GLES30.glCullFace(GLES30.GL_FRONT);

				GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
				GLES30.glViewport(0, 0, SHADOW_MAP_RESOLUTION, SHADOW_MAP_RESOLUTION);
				drawScene(ShadowShader.getDefault(), mShadowProjection, mShadowView, null, offsetX);

				if (mShowLightBuffer)
					return;

				GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);

				// setup shadow matrix
				mShadowMatrix[0] = 0.5f; mShadowMatrix[1] = 0.0f; mShadowMatrix[2] = 0.0f; mShadowMatrix[3] = 0.0f;
				mShadowMatrix[4] = 0.0f; mShadowMatrix[5] = 0.5f; mShadowMatrix[6] = 0.0f; mShadowMatrix[7] = 0.0f;
				mShadowMatrix[8] = 0.0f; mShadowMatrix[9] = 0.0f; mShadowMatrix[10] = 0.5f; mShadowMatrix[11] = 0.0f;
				mShadowMatrix[12] = 0.5f; mShadowMatrix[13] = 0.5f; mShadowMatrix[14] = 0.5f; mShadowMatrix[15] = 1.0f;

				Matrix.multiplyMM(mShadowMatrix, 0, mShadowMatrix, 0, mShadowProjection, 0);
				Matrix.multiplyMM(mShadowMatrix, 0, mShadowMatrix, 0, mShadowView, 0);

				GLES30.glActiveTexture(GLES30.GL_TEXTURE5);
				GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mDepthTexture);

				// render main scene
				GLES30.glColorMask(true, true, true, true);
				GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
				GLES30.glViewport(0, 0, mWidth, mHeight);
				GLES30.glCullFace(GLES30.GL_BACK);

				drawScene(ObjectShader.getDefault(), mProjection, mView, mShadowMatrix, offsetX);
			}

			void drawScene(Shader shader, float[] projection, float[] view, float[] shadow, float offsetX) {
				Matrix.setIdentityM(mModel, 0);
				Matrix.scaleM(mModel, 0, 20.0f, 20.0f, 20.0f);

				mPlane.draw(shader, projection, view, mModel, shadow);

				if (offsetX > 5.99f) {
					mPipeLocation = new Random().nextFloat();
				}

				Matrix.setIdentityM(mModel, 0);
				Matrix.translateM(mModel, 0, offsetX, mPipeLocation * 3.0f + 5f, 0);

				mPipe.draw(shader, projection, view, mModel, shadow);

				Matrix.setIdentityM(mModel, 0);
				Matrix.setRotateM(mModel, 0, 180, 1, 0, 0);
				Matrix.translateM(mModel, 0, offsetX, (1 - mPipeLocation) * 3.0f - 5f, 0);

				mPipe.draw(shader, projection, view, mModel, shadow);
			}

			public void onSurfaceChanged(GL10 unused, int width, int height) {
				mWidth = width;
				mHeight = height;

				float ratio = (float) width / height;

				Matrix.perspectiveM(mProjection, 0, 75.0f, ratio, 0.1f, 100.0f);
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (e.getAction() == MotionEvent.ACTION_UP)
			mShowLightBuffer = !mShowLightBuffer;

		return true;
	}
}

