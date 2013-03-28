package de.codesourcery.engine.raytracer;



public class Plane extends Raytracable {

	public Vector4 pointOnPlane;
	public Vector4 unitNormalVector; // unit-length normal vector
	
	private static final double EPSILON = 0.00001;
	
	public Transformation transform;
	
	public Plane(String name,Vector4 pointOnPlane, Transformation transform) 
	{
		this(name, pointOnPlane , transform.transform( new Vector4(0,0,1 ) ) );
		this.transform = transform;
	}
	
	public Plane(String name,Vector4 pointOnPlane, Vector4 normalVector) 
	{
		super( name, new Material( 
				new Vector4(1,1,1), // diffuseColor
				0, // reflectivity
				new Vector4(0,0,0) // specularColor
				,512) ); // shininess		
		this.pointOnPlane = pointOnPlane;
		this.unitNormalVector = normalVector.normalize();
	}
	
	public void setNormalAndPoint(Vector4 normal,Vector4 point) {
	    this.pointOnPlane = point;
	    this.unitNormalVector = normal.normalize();
	}

	@Override
	public IntersectionInfo intersect(Ray ray) 
	{
		final double denominator = ray.direction.dotProduct( unitNormalVector );
		if ( denominator > -EPSILON && denominator < EPSILON ) 
		{
			return null;
		}
		final double nominator = pointOnPlane.minus( ray.point ).dotProduct( unitNormalVector );
		final double solution = nominator / denominator;
		return new IntersectionInfo( this ).addSolution( solution );
	}
	
	@Override
	public Vector4 getColorAtPoint(Vector4 o)
	{
		Vector4 p = transform.transformInverse( o );
	    return material.texture.getColorAt(  ( Math.abs(p.x) % 100d ) / 100.0d , ( Math.abs( p.y ) % 100d / 100d ) );
	}

	@Override
	public Vector4 normalVectorAt(Vector4 point,Camera camera) {
		return unitNormalVector;
	}
	
	@Override
	public String toString()
	{
	    return "Plane[ "+name+" , point: "+pointOnPlane+" , normal: "+unitNormalVector+"]";
	}
}