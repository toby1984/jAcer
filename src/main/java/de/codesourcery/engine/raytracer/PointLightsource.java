package de.codesourcery.engine.raytracer;

import java.awt.Color;


public class PointLightsource extends Lightsource {

	public PointLightsource(Vector4 position) 
	{
		super(position);
	}
	
	public PointLightsource(Vector4 position,Color color) 
	{
		super(position,color);
	}	
}
