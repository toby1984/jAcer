package de.codesourcery.engine.raytracer;

import java.awt.Color;

public abstract class Lightsource {

	public Vector4 diffuseColor;
	public Vector4 ambientColor;
	public Vector4 specularColor;
	public Vector4 position;
	
	public Lightsource(Vector4 position) 
	{
		this.position = position;
		diffuseColor = new Vector4(1,0,0);
		ambientColor = new Vector4(0,0,0);
		specularColor = new Vector4(1,1,1);
	}
	
	public Lightsource(Vector4 position,Color color) 
	{
		this.position = position;
		double r = color.getRed() / 255.0d;
		double g = color.getGreen() /255.0d;
		double b = color.getBlue() /255.0d;
		diffuseColor = new Vector4(r,g,b);
		ambientColor = new Vector4(0,0,0);
		specularColor = new Vector4(1,1,1);
	}
	
	public Lightsource(Vector4 position,Vector4 diffuseColor, Vector4 ambientColor,Vector4 specularColor) 
	{
		this.position = position;
		this.diffuseColor = diffuseColor;
		this.ambientColor = ambientColor;
		this.specularColor = specularColor;
	}
	
	@Override
	public String toString() {
		return "PointLightsource[ "+position+" ]";
	}
}
