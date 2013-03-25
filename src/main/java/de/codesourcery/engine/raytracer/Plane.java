package de.codesourcery.engine.raytracer;


public class Plane extends Raytracable {

	public Vector4 pointOnPlane;
	public Vector4 unitNormalVector; // unit-length normal vector
	
	public Plane(Vector4 pointOnPlane, Vector4 normalVector) 
	{
		super( new Material( 
				new Vector4(1,1,1), // diffuseColor
				new Vector4(0,0,0) // specularColor
				,512) ); // shininess		
		this.pointOnPlane = pointOnPlane;
		this.unitNormalVector = normalVector.normalize();
	}

	@Override
	public IntersectionInfo intersect(Ray ray) 
	{
		final double denominator = ray.v.dotProduct( unitNormalVector );
		if ( Math.abs( denominator ) < 0.000001 ) 
		{
			// either no intersection OR the ray is inside the plane
			return null;
		}
		final double nominator = pointOnPlane.minus( ray.u ).dotProduct( unitNormalVector );
		final double solution = nominator / denominator;
		return new IntersectionInfo( this).addSolution( solution );
	}

	@Override
	public Vector4 normalVectorAt(Vector4 point) {
		return unitNormalVector;
	}
}