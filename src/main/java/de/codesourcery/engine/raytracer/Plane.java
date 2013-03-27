package de.codesourcery.engine.raytracer;

import org.apache.commons.lang.StringUtils;


public class Plane extends Raytracable {

	public Vector4 pointOnPlane;
	public Vector4 unitNormalVector; // unit-length normal vector
	
	private static final double EPSILON = 0.00001;
	
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
	
	public static void main(String[] args)
    {
	    Plane p = new Plane("test",new Vector4(0,0,0) , new Vector4(0,1,0 ) );
    }
	
	@Override
	public Vector4 getColorAtPoint(Vector4 o)
	{
        Vector4 u = o.minus( pointOnPlane ).normalize(); 
        Vector4 v = u.crossProduct( unitNormalVector ).normalize();
        u = unitNormalVector.crossProduct( v ).normalize(); 
        
	    Vector4 p = pointOnPlane;
        double a = ( o.y * v.x - v.x*p.y - v.y * o.x + v.y*p.x) / (v.x*u.y - v.y*u.x );
        double b = ( o.x - p.x - u.x*a) / v.x;
	    return material.texture.getColorAt(  a , b );
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