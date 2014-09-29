package de.tombeckmann.flappy3d;

public class ObjectShader extends Shader {

	private static final String DEFAULT_VERTEX =
		"#version 300 es\n" +

		"in vec3 position;" +
		"in vec3 normal;" +

		"uniform mat4 projection;" +
		"uniform mat4 view;" +
		"uniform mat4 shadowMatrix;" +

		"out vec3 vNormal;" +
		"out vec4 shadowCoord;" +

		"void main() {" +
		"	vNormal = normalize(mat3(view) * normal);" +
		"	shadowCoord = shadowMatrix * vec4(position, 1.0);" +
		"	gl_Position = projection * view * vec4(position, 1.0);" +
		"}";

	private static final String DEFAULT_FRAGMENT =
		"#version 300 es\n" +

		"precision mediump float;" +

		"uniform vec4 color;" +
		"uniform sampler2D shadowTexture;" +

		"in vec3 vNormal;" +
		"in vec4 shadowCoord;" +

		"out vec4 finalColor;" +

		"void main() {" +
		"	vec3 shadowCoordDiv = shadowCoord.xyz / shadowCoord.w;" +
		"	shadowCoordDiv.z += 0.0005;" +

		"	float distanceLight = texture(shadowTexture, shadowCoordDiv.st).z;" +

		"	float shadow = 1.0;" +
		"	if (shadowCoord.w > 0.0)" +
		"		shadow = distanceLight < shadowCoordDiv.z ? 0.5 : 1.0;" +

		"	float intensity = max(0.2, dot(normalize(vNormal), vec3(1.0, 1.0, 1.0)));" +
		"	finalColor = vec4(shadow * intensity * color.rgb, 1.0);" +
		//"	finalColor = vec4(vec3(distanceLight), 1.0);" +
		"}";

	static ObjectShader instance = null;

	public static ObjectShader getDefault() {
		if (instance == null)
			instance = new ObjectShader();

		return instance;
	}

	ObjectShader() {
		super(DEFAULT_VERTEX, DEFAULT_FRAGMENT);

		fetchAttribute("position");
		fetchAttribute("normal");

		fetchUniform("shadowTexture");
		fetchUniform("color");
		fetchUniform("projection");
		fetchUniform("view");
		fetchUniform("shadowMatrix");
	}
}

