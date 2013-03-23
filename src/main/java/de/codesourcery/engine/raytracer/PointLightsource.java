package de.codesourcery.engine.raytracer;

import java.awt.Color;

public class PointLightsource extends Lightsource {

	public Color color;
	public Vector4 position;
	
	public PointLightsource(Vector4 position, Color color) {
		this.position = position;
		this.color = color;
	}
}
