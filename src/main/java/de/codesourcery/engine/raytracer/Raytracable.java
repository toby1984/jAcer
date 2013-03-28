package de.codesourcery.engine.raytracer;


public abstract class Raytracable {

	public Material material;
	
	public abstract IntersectionInfo intersect(Ray ray);
	
	public abstract Vector4 normalVectorAt(Vector4 point,Camera camera);

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
	
	public static final Vector4 reflect(Vector4 incoming, Vector4 normalVectorAtReflectionPoint) {
		
		/* n: normal vector at point P
		 * d: incoming ray
		 * 
		 * r = d − ( d ⋅n ) n *2
         *
         * where d⋅n is the dot product, and n must be normalized.
		 */
		final double factor = 2 * incoming.dotProduct( normalVectorAtReflectionPoint );
		double dx = incoming.x - normalVectorAtReflectionPoint.x * factor;
        double dy = incoming.y - normalVectorAtReflectionPoint.y * factor;
        double dz = incoming.z - normalVectorAtReflectionPoint.z * factor;        
        return new Vector4(dx,dy,dz);
	}
	
	public Vector4 getColorAtPoint(Vector4 pointOnSurface) 
	{
	    throw new UnsupportedOperationException("getColorAtPoint() not implemented");
	}
}