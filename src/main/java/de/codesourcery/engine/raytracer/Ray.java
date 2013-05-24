package de.codesourcery.engine.raytracer;


public class Ray 
{
	public int bounceCount=0;
	public Vector4 point; // point on ray
	public Vector4 direction; // direction
	public boolean debug = false;
	public boolean fromInsideObject;
	
	public Ray(Vector4 origin, Vector4 direction) 
	{
	    this(origin,direction,0);
	}
	
	public Ray(Vector4 origin, Vector4 direction,int bounceCount) 
	{
		this.point = origin;
		this.direction = direction;
		this.bounceCount = bounceCount;
	}	
	
	public Vector4 evaluateAt(double t) 
	{
		return direction.multiplyAdd( t , point );
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
	
	public Ray transform(Transformation transform) 
	{
	    // direction is already normalized and transformDirection() applies only the rotational
	    // component of the transformation so the length of the vector does not change
		return new Ray( transform.transform( point ) , transform.transformDirection( direction ) );
	}	
	
	@Override
	public String toString() {
		return "Ray( point: "+point+" , direction: "+direction+")";
	}
}
