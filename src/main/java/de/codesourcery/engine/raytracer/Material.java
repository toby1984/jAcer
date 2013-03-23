package de.codesourcery.engine.raytracer;

public class Material {

	public Vector4 diffuseColor;
	public Vector4 ambientColor;
	public Vector4 specularColor;
	public float shininess;
	
	public Material(Vector4 diffuseColor, Vector4 ambientColor, Vector4 specularColor) 
	{
		this(diffuseColor , ambientColor , specularColor , 128 );
	}
	
	public Material(Vector4 diffuseColor, Vector4 ambientColor, Vector4 specularColor,float shininess) 
	{
		this.diffuseColor = diffuseColor;
		this.ambientColor = ambientColor;
		this.specularColor = specularColor;
		this.shininess = shininess;
	}
}
