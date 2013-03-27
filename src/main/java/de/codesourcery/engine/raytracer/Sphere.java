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
	
	public static void main(String[] args) {
		
		Sphere s = new Sphere("test" , new Vector4(0,0,0) , 10 );
		
		Ray r = new Ray(new Vector4(0,0,100) , new Vector4(0,0,-1 ) );
		IntersectionInfo intersect = s.intersect( r );
		System.out.println("Intersect: "+intersect);
		Vector4 p1 = r.evaluateAt( intersect.solutions[0] );
		Vector4 p2 = r.evaluateAt( intersect.solutions[1] );
		System.out.println("p1: "+p1);
		System.out.println("p2: "+p2);
		System.out.println("Normal at p1: "+s.normalVectorAt( p1 ) );
		System.out.println("Normal at p2: "+s.normalVectorAt( p2 ) );
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
	public Vector4 normalVectorAt(Vector4 pointInViewCoordinates) 
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