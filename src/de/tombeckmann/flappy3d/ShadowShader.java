package de.tombeckmann.flappy3d;

public class ShadowShader extends Shader {

	private static final String DEFAULT_VERTEX =
		"attribute vec3 position;" +

		"uniform mat4 projection;" +
		"uniform mat4 view;" +

		"void main() {" +
		"	gl_Position = projection * view * vec4(position, 1.0);" +
		"}";

	private static final String DEFAULT_FRAGMENT =
		"precision mediump float;" +

		"uniform vec4 color; " +

		"void main() {" +
		"	gl_FragColor = vec4(vec3(gl_FragCoord.z), 1.0);" +
		"}";

	private static ShadowShader instance = null;

	public static ShadowShader getDefault() {
		if (instance == null)
			instance = new ShadowShader();

		return instance;
	}

	ShadowShader() {
		super(DEFAULT_VERTEX, DEFAULT_FRAGMENT);

		fetchAttribute("position");

		fetchUniform("projection");
		fetchUniform("view");
	}
}


