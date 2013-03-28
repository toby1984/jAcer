package de.codesourcery.engine.raytracer;


public class Material {

	public Vector4 diffuseColor;
	public Vector4 specularColor;
	private double reflectivity;
	public double shininess;
	public Texture texture;
	public double refractionIndex=1.0;
	public double glossiness;
	
	public double reflectivity() {
		return reflectivity;
	}
	
	public void reflectivity(double ref) {
		this.reflectivity = ref;
	}
	
	public Material() 
	{
		this( new Vector4(1,1,1), new Vector4(1,1,1)); 
	}

	public Material(Vector4 diffuseColor,Vector4 specularColor) 
	{
		this(diffuseColor , 0,specularColor , 128 );
	}

	public Material(Vector4 diffuseColor, double reflectivity,Vector4 specularColor,double shininess) 
	{
		this.reflectivity = reflectivity;
		this.diffuseColor = diffuseColor;
		this.specularColor = specularColor;
		this.shininess = shininess;
	}
}
