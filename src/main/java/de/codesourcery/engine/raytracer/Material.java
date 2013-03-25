package de.codesourcery.engine.raytracer;

public class Material {

	public Vector4 diffuseColor;
	public Vector4 specularColor;
	public double reflectivity;
	public double shininess;
	
	public Material(Vector4 diffuseColor,Vector4 specularColor) 
	{
		this(diffuseColor , specularColor , 128 );
	}
	
	public Material(Vector4 diffuseColor, Vector4 specularColor,float shininess) 
	{
		this.diffuseColor = diffuseColor;
		this.specularColor = specularColor;
		this.shininess = shininess;
	}
}
