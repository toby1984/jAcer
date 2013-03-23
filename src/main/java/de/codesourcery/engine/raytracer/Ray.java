package de.codesourcery.engine.raytracer;


public class Ray 
{
	public int bounceCount=0;
	public Vector4 u; // point on ray
	public Vector4 v; // direction
	
	public Ray(Vector4 u, Vector4 v) 
	{
		this.u = u;
		this.v = v.normalize();
	}
	
	public Ray(Vector4 u, Vector4 v,int bounceCount) 
	{
		this.u = u;
		this.v = v.normalize();
		this.bounceCount = bounceCount;
	}	
	
	public Vector4 evaluateAt(double t) 
	{
		// u+v*t
		return u.plus( v.multiply( (float) t ) );
	}
	
	public Ray transform(Vector4 vec) 
	{
		return new Ray( u.minus(vec) , v );
	}	
	
	public Ray transform(Matrix m) 
	{
		Vector4 newP0 = m.multiply( u );
		Vector4 newP1 = m.multiply( u.plus( v ) );
		Vector4 newDirection = newP1.minus( newP0 );
		return new Ray( newP0 , newDirection );
	}
	
	@Override
	public String toString() {
		return "Ray( point: "+u+" , direction: "+v+")";
	}
}
