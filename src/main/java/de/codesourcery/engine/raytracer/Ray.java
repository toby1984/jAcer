package de.codesourcery.engine.raytracer;


public class Ray 
{
	public int bounceCount=0;
	public Vector4 point; // point on ray
	public Vector4 direction; // direction
	
	public Ray(Vector4 u, Vector4 v) 
	{
	    this(u,v,0);
	}
	
	public Ray(Vector4 u, Vector4 v,int bounceCount) 
	{
		this.point = u;
		this.direction = v;
		this.bounceCount = bounceCount;
	}	
	
	public Vector4 evaluateAt(double t) 
	{
		// u+v*t
		return point.plus( direction.multiply( (float) t ) );
	}
	
	public double solutionAt(Vector4 p) 
	{
		if ( direction.x != 0 ) {
			return (p.x - point.x) / direction.x;
		} 
		
		if ( direction.y != 0 ) 
		{
			return (p.y - point.y) / direction.y;
		} 
		
		if ( direction.z != 0 ) 
		{
			return (p.z - point.z) / direction.z;
		}
		return 0;
	}
	
	public static void main(String[] args) 
	{
		Ray ray = new Ray( new Vector4(0,0,0) , new Vector4(0,0,1 ).normalize() );
        System.out.println( "Input: "+ray);		
		ray = ray.transform( Matrix.identity() );
		System.out.println( ray);
	}
	
	public Ray transform(Transformation transform) 
	{
	    // direction is already normalized and transformDirection() applies only the rotational
	    // component of the transformation so the length of the vector does not change
		return new Ray( transform.transform( point ) , transform.transformDirection( direction ) );
	}	
	
	public Ray transform(Matrix m) 
	{
	    final Vector4 p1 = point.plus( direction );
	    Vector4 newp0 = point.multiply( m );
	    Vector4 newp1 = p1.multiply(m);
		return new Ray( newp0 , newp1.minus(newp0).normalize() );
	}
	
	@Override
	public String toString() {
		return "Ray( point: "+point+" , direction: "+direction+")";
	}
}
