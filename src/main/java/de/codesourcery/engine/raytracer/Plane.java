package de.codesourcery.engine.raytracer;


public class Plane extends Raytracable {

	public Vector4 pointOnPlane;
	public Vector4 unitNormalVector; // unit-length normal vector
	
	private static final double EPSILON = 0.00000001;
	
	public Plane(String name,Vector4 pointOnPlane, Vector4 normalVector) 
	{
		super( name, new Material( 
				new Vector4(1,1,1), // diffuseColor
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
		final double denominator = ray.v.dotProduct( unitNormalVector );
		if ( denominator > -EPSILON && denominator < EPSILON ) 
		{
			return null;
		}
		final double nominator = pointOnPlane.minus( ray.u ).dotProduct( unitNormalVector );
		final double solution = nominator / denominator;
		return new IntersectionInfo( this ).addSolution( solution );
	}

	@Override
	public Vector4 normalVectorAt(Vector4 point) {
		return unitNormalVector;
	}
	
	@Override
	public String toString()
	{
	    return "Plane[ "+name+" , point: "+pointOnPlane+" , normal: "+unitNormalVector+"]";
	}
}