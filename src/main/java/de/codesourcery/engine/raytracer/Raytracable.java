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
		
		/*
Rr = Ri - 2 N (Ri . N)

Some obvious things to note at extreme cases. 
* If Ri is parallel to the normal then Rr = N, that is, the reflected ray is in the opposite direction to the incident ray. 
* If the incident ray is perpendicular to the normal then it is unaffected, Rr = Ri, this is a glancing ray. 		 
		 */
		
		/* n: normal vector at point P
		 * d: incoming ray
		 * 
		 * r = d − ( d ⋅n ) n *2
         *
         * where d⋅n is the dot product, and n must be normalized.
		 */
//		return incoming.minus( normalVectorAtReflectionPoint.multiply( incoming.dotProduct( normalVectorAtReflectionPoint ) ).multiply( 2 ) );
		
		final double factor = 2 * incoming.dotProduct( normalVectorAtReflectionPoint );
		double dx = incoming.x - normalVectorAtReflectionPoint.x * factor;
        double dy = incoming.y - normalVectorAtReflectionPoint.y * factor;
        double dz = incoming.z - normalVectorAtReflectionPoint.z * factor;        
        return new Vector4(dx,dy,dz);
	}
	
	public Vector4 sampleTextureColorAtPoint(Vector4 pointOnSurface) 
	{
	    throw new UnsupportedOperationException("getColorAtPoint() not implemented");
	}
	
	private static final ThreadLocal<MersenneTwisterFast> twister = new ThreadLocal<MersenneTwisterFast>() 
			{
		protected MersenneTwisterFast initialValue() {
			return new MersenneTwisterFast( System.currentTimeMillis() );
		}
	};
	
	/**
	 * Perturbs a normal vector by construction an imaginary view plane orthogonal to a normal vector in a certain distance from
	 * a point of origin and then picking a random point on it.
	 *    
	 * @param origin point of origin, view plane will be <code>distanceToOrigin</code> apart
	 * @param normalVector the normalalized vector to perturb
	 * @param planeWidth The width of the view plane in distance <code>distanceToOrigin</code>
	 * @param distanceToOrigin distance of the view plane from <code>origin</code> 
	 * @return
	 */
	public static Vector4 perturbNormalVector(Vector4 origin, Vector4 normalVector,double planeWidth,double distanceToOrigin) 
	{
		final Vector4 zAxis = new Vector4(normalVector);
		final Vector4 xAxis = new Vector4();
		final Vector4 yAxis = new Vector4();
		
		OrthonormalBasis.fromZAxis( xAxis , yAxis , zAxis );
		
		final Vector4 centerOfPlane = normalVector.multiplyAdd( distanceToOrigin , origin ); 
		final MersenneTwisterFast rnd = twister.get();
		double u = rnd.nextDouble(false,false)*planeWidth;
		double v = rnd.nextDouble(false,false)*planeWidth;		
		centerOfPlane.plusInPlace( xAxis.multiply( u ) , yAxis.multiply( v ) );
		centerOfPlane.minusInPlace( origin );
		return centerOfPlane.normalize(); 
	}
}