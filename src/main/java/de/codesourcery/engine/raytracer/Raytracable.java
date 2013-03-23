package de.codesourcery.engine.raytracer;


public abstract class Raytracable {

	public abstract IntersectionInfo intersect(Ray ray);
	
	public abstract Vector4 normalVectorAt(Vector4 point);
}
