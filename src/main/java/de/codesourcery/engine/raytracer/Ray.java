package de.codesourcery.engine.raytracer;


public class Ray 
{
	public int bounceCount=0;
	public Vector4 point; // point on ray
	public Vector4 direction; // direction
	
	public Ray(Vector4 u, Vector4 v) 
	{
		this.point = u;
		this.direction = v.normalize();
	}
	
	public Ray(Vector4 u, Vector4 v,int bounceCount) 
	{
		this.point = u;
		this.direction = v.normalize();
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
	
	public static void main(String[] args) {
		Ray ray = new Ray( new Vector4(0,0,0) , new Vector4(1,0,0 ) );
		
		Vector4 p = ray.evaluateAt( 1111.2 );
		System.out.println( ray +" => "+p);
		System.out.println( "solution: "+ray.solutionAt( p ) );
	}
	
	public Ray transform(Vector4 vec) 
	{
		return new Ray( point.minus(vec) , direction );
	}	
	
	public Ray transform(Matrix m) 
	{
		Vector4 newP0 = m.multiply( point );
		Vector4 newP1 = m.multiply( point.plus( direction ) );
		Vector4 newDirection = newP1.minus( newP0 );
		return new Ray( newP0 , newDirection );
	}
	
	@Override
	public String toString() {
		return "Ray( point: "+point+" , direction: "+direction+")";
	}
}
