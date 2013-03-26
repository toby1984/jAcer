package de.codesourcery.engine.raytracer;


public abstract class Raytracable {

	public Material material;
	
	public Matrix matrix = null;
	
	public abstract IntersectionInfo intersect(Ray ray);
	
	public abstract Vector4 normalVectorAt(Vector4 point);

	public final String name;
	
	public Raytracable(String name) 
	{
		this( name , new Material( 
				new Vector4(1,1,1), // diffuseColor
				new Vector4(1,1,1) // specularColor
				) ); // shininess
	}
	
	public Raytracable(String name,Material material) {
	    this.name = name;
		this.material = material;
	}
	
	public final Ray reflect(Ray incomingRay , Vector4 pointOnSurface) 
	{
		final Vector4 normalVector = normalVectorAt( pointOnSurface );
		final Vector4 newDirection = reflect( incomingRay.direction , normalVector );
		return new Ray( pointOnSurface , newDirection , incomingRay.bounceCount+1 );
	}
	
	public static final Vector4 reflect(Vector4 incoming, Vector4 normalVectorAtReflectionPoint) {
		
		/* n: normal vector at point P
		 * d: incoming ray
		 * 
		 * r=d−(d⋅n)n*2
         *
         * where d⋅n is the dot product, and n must be normalized.
		 */
		return incoming.minus( normalVectorAtReflectionPoint.multiply( 2 * incoming.dotProduct( normalVectorAtReflectionPoint ) ) );
	}
}
