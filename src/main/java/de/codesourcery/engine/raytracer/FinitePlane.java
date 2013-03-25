package de.codesourcery.engine.raytracer;

public class FinitePlane extends Plane {

	public Vector4 edge2;
	public Vector4 edge3;

	public double xMin; 
	public double xMax; 

	public double yMin; 
	public double yMax;

	public double zMin; 
	public double zMax; 

	public FinitePlane(String name,Vector4 edge,Vector4 normal,double width,double height) 
	{
		super(name,edge,normal);

		// construct orthonormal basis
		/*
		 * Let's call x your unit vector. 
		 * 
		 * * Let u = (1,0,0) 
		 *   If dot(u,x) ~= 0, then let u = (0,1,0)
		 * * y = x ^ u 
		 * * z = x ^ y
		 */
		Vector4 u = new Vector4(1,0,0);
		if ( Math.abs( u.dotProduct( normal ) ) < 0.001 ) {
			u = new Vector4(0,1,0);
		}
		final Vector4 y = normal.crossProduct( u ).normalize();
		final Vector4 z = normal.crossProduct( y ).normalize();

		edge2 = edge.plus( y.multiply( width ) );
		edge3 = edge.plus( z.multiply( height ) );

		xMin = Math.min( Math.min( pointOnPlane.x , edge2.x ) , edge3.x )-0.01;
		xMax = Math.max( Math.max( pointOnPlane.x , edge2.x ) , edge3.x )+0.01;

		yMin = Math.min( Math.min( pointOnPlane.y , edge2.y ) , edge3.y )-0.01;
		yMax = Math.max( Math.max( pointOnPlane.y , edge2.y ) , edge3.y )+0.01;

		zMin = Math.min( Math.min( pointOnPlane.z , edge2.z ) , edge3.z )-0.01;
		zMax = Math.max( Math.max( pointOnPlane.z , edge2.z ) , edge3.z )+0.01;		
	}

	public void setNormalAndPoint(Vector4 normal,Vector4 point) {
		throw new UnsupportedOperationException("setNormalAndPoint() on finite plane "+this);
	}

	public boolean contains(Vector4 point) 
	{
		if ( point.x >= xMin && point.x <= xMax &&
				point.y >= yMin && point.y <= yMax &&
				point.z >= zMin && point.z <= zMax ) 
		{
			return true;
		}
		return false;
	}

	@Override
	public IntersectionInfo intersect(Ray ray) 
	{
		final IntersectionInfo  result = super.intersect(ray);
		if ( result == null ) {
			return null;
		}

		final Vector4 point = ray.evaluateAt( result.solutions[0] );

		if ( contains( point ) ) 
		{
			return result;
		}
		return null;
	}
}
