package de.codesourcery.engine.raytracer;


public class Sphere extends Raytracable {

	private Transformation transform;
	public double radius;
	
	public Sphere(String name,Material material,Vector4 center,double radius) 
	{
	    super(name,material);
	    this.transform = new Transformation( AffineTransform.translate( center.x , center.y , center.z ) );
		this.radius = radius;
	}	
	
	public Sphere(String name,Vector4 center,double radius) 
	{
	    super(name);
	    this.transform = new Transformation( AffineTransform.translate( center.x , center.y , center.z ) );	    
		this.radius = radius;
	}
	
	@Override
	public IntersectionInfo intersect(Ray inputRay) 
	{
		final Ray ray = inputRay.transform( transform );
		
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
		
		final Vector4 p1 = transform.transformInverse( ray.evaluateAt( t1 ) );
		final Vector4 p2 = transform.transformInverse( ray.evaluateAt( t2 ) );
		return new IntersectionInfo(this).addSolutions( inputRay.solutionAt( p1 ), inputRay.solutionAt( p2 ) );
//		return new IntersectionInfo(this).addSolutions( t1,t2);
	}

	@Override
	public Vector4 getColorAtPoint(Vector4 p)
	{
//        p = transform.transformInverse( p ); 
        
	    double r = p.length();
	    
        // double v = Math.acos(p.z/r) / Math.PI;
        // double u = Math.acos( p.x/r * Math.sin (Math.PI * v) ) / 2*Math.PI;     
        
	    // ((x^2 + y^2 + z^2)^0.5, arctan(y/x), arccos(z/r))
        double longitude = Math.atan2( p.y , p.x);
        double latitude = Math.acos( p.z / r );
        
        double u = radToDeg( longitude ) / 360;
        double v = radToDeg(latitude ) / 360;
     
	    return material.texture.getColorAt( u , v );
	}
	
	private static double radToDeg(double rad) {
	    double result = rad*180.0/Math.PI;
	    if ( result > 359 ) {
	        result -= 360;
	    } else if ( result < 0 ) {
	        result += 360;
	    }
	    return result;
	}
	
	@Override
	public Vector4 normalVectorAt(Vector4 pointInViewCoordinates,Camera camera) 
	{
		Vector4 t = transform.transform( pointInViewCoordinates );		
		return t.normalize();
	}
	
	@Override
	public String toString() 
	{
		return "Sphere[ "+name+" , radius: "+radius+"]";
	}
}