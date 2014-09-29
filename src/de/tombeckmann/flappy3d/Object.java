package de.tombeckmann.flappy3d;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import android.opengl.GLES30;
import android.opengl.Matrix;

public class Object {

	private FloatBuffer mVertexBuffer;
	private FloatBuffer mNormalBuffer;
	private ShortBuffer mIndexBuffer;
	private int mNElements;

	private float[] mModelView = new float[16];
	private float[] mShadowMatrix = new float[16];

	private float[] mColor = { 0, 0, 0, 1 };

	public Object(float[] vertices, float[] normals, short[] indices) {
		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.length * 4);
		bb.order(ByteOrder.nativeOrder());

		mVertexBuffer = bb.asFloatBuffer();
		mVertexBuffer.put(vertices);
		mVertexBuffer.position(0);

		ByteBuffer bn = ByteBuffer.allocateDirect(normals.length * 4);
		bn.order(ByteOrder.nativeOrder());

		mNormalBuffer = bn.asFloatBuffer();
		mNormalBuffer.put(normals);
		mNormalBuffer.position(0);

		ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * 2);
		dlb.order(ByteOrder.nativeOrder());
		mIndexBuffer = dlb.asShortBuffer();
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);

		mNElements = indices.length;
	}

	public Object(InputStream buffer) {
		ArrayList<Float> vertices = new ArrayList<Float>();
		ArrayList<Float> normals = new ArrayList<Float>();
		ArrayList<Short> indices = new ArrayList<Short>();
		ArrayList<Float> normalsFormatted = new ArrayList<Float>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(buffer));

		try {
			boolean startFormatted = false;

			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(" ");
				if (parts[0].equals("v")) {
					for (int i = 1; i < 4; i++)
						vertices.add(Float.parseFloat(parts[i]));
				} else if (parts[0].equals("vn")) {
					for (int i = 1; i < 4; i++)
						normals.add(Float.parseFloat(parts[i]));
				} else if (parts[0].equals("f")) {
					if (!startFormatted) {
						startFormatted = true;
						normalsFormatted.ensureCapacity(vertices.size());
						for (int i = 0; i < vertices.size(); i++) {
							normalsFormatted.add(0.0f);
						}
					}

					for (int i = 1; i < 4; i++) {
						String[] idx = parts[i].split("/");
						int index = Integer.parseInt(idx[0]) - 1;
						indices.add((short) index);

						int normalsIndex = Integer.parseInt(idx[2]) - 1;

						for (int j = 0; j < 3; j++) {
							float normal = normals.get(normalsIndex * 3 + j);
							normalsFormatted.set(index * 3 + j, normal);
						}
					}
				}
			}
		} catch (IOException e) {}

		mNElements = indices.size();

		ByteBuffer bb = ByteBuffer.allocateDirect(vertices.size() * 4);
		bb.order(ByteOrder.nativeOrder());

		mVertexBuffer = bb.asFloatBuffer();
		for (int i = 0; i < vertices.size(); i++) {
			mVertexBuffer.put(i, vertices.get(i));
		}
		mVertexBuffer.position(0);

		ByteBuffer bn = ByteBuffer.allocateDirect(normalsFormatted.size() * 4);
		bn.order(ByteOrder.nativeOrder());

		mNormalBuffer = bn.asFloatBuffer();
		for (int i = 0; i < normalsFormatted.size(); i++) {
			mNormalBuffer.put(i, normalsFormatted.get(i));
		}
		mNormalBuffer.position(0);

		ByteBuffer dlb = ByteBuffer.allocateDirect(indices.size() * 2);
		dlb.order(ByteOrder.nativeOrder());

		mIndexBuffer = dlb.asShortBuffer();
		for (int i = 0; i < indices.size(); i++) {
			mIndexBuffer.put(i, indices.get(i));
		}
		mIndexBuffer.position(0);
	}

	public void draw(Shader shader, float[] projection, float[] view, float[] model, float[] shadow) {
		GLES30.glUseProgram(shader.mProgram);
		GLES30.glEnableVertexAttribArray(shader.attributes.get("position"));
		GLES30.glVertexAttribPointer(shader.attributes.get("position"), 3, GLES30.GL_FLOAT, false, 12, mVertexBuffer);

		if (shader.attributes.containsKey("normal")) {
			GLES30.glEnableVertexAttribArray(shader.attributes.get("normal"));
			GLES30.glVertexAttribPointer(shader.attributes.get("normal"), 3, GLES30.GL_FLOAT, false, 12, mNormalBuffer);
		}

		Matrix.multiplyMM(mModelView, 0, view, 0, model, 0);

		if (shader.uniforms.containsKey("color"))
			GLES30.glUniform4f(shader.uniforms.get("color"), mColor[0], mColor[1], mColor[2], mColor[3]);
		if (shadow != null) {
			Matrix.multiplyMM(mShadowMatrix, 0, shadow, 0, model, 0);
			GLES30.glUniformMatrix4fv(shader.uniforms.get("shadowMatrix"), 1, false, mShadowMatrix, 0);
		}

		if (shader.uniforms.containsKey("shadowTexture"))
			GLES30.glUniform1i(shader.uniforms.get("shadowTexture"), 5);

		GLES30.glUniformMatrix4fv(shader.uniforms.get("projection"), 1, false, projection, 0);
		GLES30.glUniformMatrix4fv(shader.uniforms.get("view"), 1, false, mModelView, 0);

		GLES30.glDrawElements(GLES30.GL_TRIANGLES, mNElements, GLES30.GL_UNSIGNED_SHORT, mIndexBuffer);

		GLES30.glDisableVertexAttribArray(shader.attributes.get("position"));
		if (shader.attributes.containsKey("normal"))
			GLES30.glDisableVertexAttribArray(shader.attributes.get("normal"));
	}

	public void setColor(float r, float g, float b, float a) {
		mColor[0] = r;
		mColor[1] = g;
		mColor[2] = b;
		mColor[3] = a;
	}
}

