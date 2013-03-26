package de.codesourcery.engine.raytracer;


public class Sphere extends Raytracable {

	public Vector4 center;
	public double radius;
	
	public Sphere(String name,Material material,Vector4 center,double radius) 
	{
	    super(name,material);
		this.center = center;
		this.radius = radius;
	}	
	
	public Sphere(String name,Vector4 center,double radius) 
	{
	    super(name);
		this.center = center;
		this.radius = radius;
	}

	@Override
	public IntersectionInfo intersect(Ray inputRay) 
	{
		final Ray ray = inputRay.transform( center );
		
		// intersection with sphere at center (0,0,0)
		double A = ray.direction.magnitude(); // v^2
		double B = 2*ray.point.dotProduct( ray.direction );
		double C = ray.point.magnitude() - radius*radius; // u^2 - 1
		
		double Bsquared = B*B;
		double ACtimes4 = 4*A*C;
		
		// determinant: sqrt( B^2-4AC )
		if ( Bsquared <= ACtimes4 ) {
			// either no intersection or intersects tangentially
			return null;
		}
		
		final double t1;
		if ( B < 0 ) {
			t1 = (-B + ( Math.sqrt( Bsquared - ACtimes4 ) ) ) / (2*A);
		} else {
			t1 = (-B - ( Math.sqrt( Bsquared - ACtimes4 ) ) ) / (2*A);
		}
		final double t2 = C / ( A * t1 );
		
		final IntersectionInfo result = new IntersectionInfo(this);
		if ( t1 > 0 && t2 > 0 ) 
		{
		    result.nearestIntersectionPoint = ray.evaluateAt( Math.min( t1 ,  t2 ) );
		} else if ( t1 > 0 ) {
            result.nearestIntersectionPoint = ray.evaluateAt( t1 );		    
		} else {
            result.nearestIntersectionPoint = ray.evaluateAt( t2 ); 
		}
		return result;
	}

	@Override
	public Vector4 normalVectorAt(Vector4 pointInViewCoordinates) 
	{
		// the normal vector is just ( point minus center )
//		final Vector4 transformedPoint = pointInViewCoordinates.multiply( modelMatrix );
		return pointInViewCoordinates.minus(center).normalize();		
//		return pointInViewCoordinates.minus(center).normalize();
	}
	
	@Override
	public String toString() 
	{
		return "Sphere[ "+name+" , center: "+center+" , radius: "+radius+"]";
	}
}