package de.codesourcery.engine.raytracer;


public class Sphere extends Raytracable {

	public Matrix modelMatrix;
	public double radius;
	
	public Sphere(Matrix modelMatrix,double radius) 
	{
		this.modelMatrix = modelMatrix;
		this.radius = radius;
	}

	@Override
	public IntersectionInfo intersect(Ray inputRay) 
	{
		final Ray ray = inputRay.transform( modelMatrix );
		
		// intersection with sphere at center (0,0,0)
		
		double A = ray.v.magnitude(); // v^2
		double B = 2*ray.u.dotProduct( ray.v );
		double C = ray.u.magnitude() - radius*radius; // u^2 - 1
		
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
		return new IntersectionInfo(this).addSolutions( t1 , t2 );
	}

	@Override
	public Vector4 normalVectorAt(Vector4 point) 
	{
		// the normal vector is just ( point minus center )
		final Vector4 transformedPoint = point.multiply( modelMatrix );
		final Vector4 center = new Vector4(0,0,0).multiply( modelMatrix );
		return transformedPoint.minus( center ).normalize();
	}
	
	@Override
	public String toString() 
	{
		final Vector4 center = new Vector4(0,0,0).multiply( modelMatrix );
		return "Sphere[ center: "+center+" , radius: "+radius+"]";
	}
}