package de.codesourcery.engine.raytracer;



public class Plane extends Raytracable {

	public Vector4 pointOnPlane;
	public Vector4 unitNormalVector; // unit-length normal vector
	
	private static final double EPSILON = 0.001;
	
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
		/*
    d = {(\mathbf{p_0}-\mathbf{l_0})\cdot\mathbf{n} \over \mathbf{l}\cdot\mathbf{n}}

	If the line starts outside the plane and is parallel to the plane, there is no intersection. 
	In this case, the above denominator will be zero and the numerator will be non-zero. 
	If the line starts inside the plane and is parallel to the plane, the line intersects the plane everywhere. 
	In this case, both the numerator and denominator above will be zero. 
	In all other cases, the line intersects the plane once and d represents the intersection as the distance along the line from \mathbf{l_0}, i.e. d\mathbf{l} + \mathbf{l_0}		 
		 */
		final double denominator = ray.direction.dotProduct( unitNormalVector );
		if ( denominator > -EPSILON && denominator < EPSILON ) 
		{
			return null;
		}
		final double nominator = pointOnPlane.minus( ray.point ).dotProduct( unitNormalVector );
		final double solution = nominator / denominator;
		return new IntersectionInfo( this , solution );
	}
	
	@Override
	public Vector4 sampleTextureColorAtPoint(Vector4 o)
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