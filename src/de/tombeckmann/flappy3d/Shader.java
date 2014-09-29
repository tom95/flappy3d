package de.tombeckmann.flappy3d;

import java.nio.IntBuffer;
import java.util.HashMap;

import android.opengl.GLES30;
import android.util.Log;

public class Shader {

	public int mProgram;

	public HashMap<String,Integer> attributes;
	public HashMap<String,Integer> uniforms;

	public Shader(String vertexSource, String fragmentSource) {
		int vertex = loadShader(GLES30.GL_VERTEX_SHADER, vertexSource);
		int fragment = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource);

		attributes = new HashMap<String,Integer>();
		uniforms = new HashMap<String,Integer>();

		mProgram = GLES30.glCreateProgram();
		GLES30.glAttachShader(mProgram, vertex);
		GLES30.glAttachShader(mProgram, fragment);
		GLES30.glLinkProgram(mProgram);

		IntBuffer buffer = IntBuffer.allocate(1);
		GLES30.glGetProgramiv(mProgram, GLES30.GL_LINK_STATUS, buffer);
		if (buffer.get(0) != GLES30.GL_TRUE) {
			Log.e("SHADER FLAPPY 3D PROGRAM", GLES30.glGetProgramInfoLog(mProgram));
		}
	}

	protected void fetchAttribute(String name) {
		attributes.put(name, GLES30.glGetAttribLocation(mProgram, name));
	}

	protected void fetchUniform(String name) {
		uniforms.put(name, GLES30.glGetUniformLocation(mProgram, name));
	}

	private int loadShader(int type, String code) {
		int shader = GLES30.glCreateShader(type);

		GLES30.glShaderSource(shader, code);
		GLES30.glCompileShader(shader);

		IntBuffer buffer = IntBuffer.allocate(1);
		GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, buffer);

		if (buffer.get(0) != GLES30.GL_TRUE) {
			Log.e("SHADER FLAPPY 3D", GLES30.glGetShaderInfoLog(shader));
		}

		return shader;
	}
}
