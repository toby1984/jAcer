package de.codesourcery.engine.raytracer;


public class Ray 
{
	public Vector4 u; // point on ray
	public Vector4 v; // direction
	
	public Ray(Vector4 u, Vector4 v) 
	{
		this.u = u;
		this.v = v.normalize();
	}
	
	public Vector4 evaluateAt(double t) 
	{
		// u+v*t
		return u.plus( v.multiply( (float) t ) );
	}
	
	public Ray transform(Matrix m) 
	{
		Vector4 newPoint = u.multiply( m );
		Vector4 newDirection = v.multiply( m.invert().transpose() );
		return new Ray( newPoint , newDirection );
	}
	
	@Override
	public String toString() {
		return "Ray( point: "+u+" , direction: "+v+")";
	}
}
