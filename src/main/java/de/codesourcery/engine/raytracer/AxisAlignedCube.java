package de.codesourcery.engine.raytracer;

public class AxisAlignedCube extends Raytracable {

	public Vector4 center;

	private FinitePlane front;
	private FinitePlane back;

	private FinitePlane left;
	private FinitePlane right;	

	private FinitePlane top;
	private FinitePlane bottom;		
	
	public AxisAlignedCube(String name,Vector4 center,double width,double height,double depth) 
	{
		super(name);
		this.center = center;

		final double halfWidth = width / 2;
		final double halfHeight= height / 2;
		final double halfDepth  = depth / 2;

		Vector4 p1 = new Vector4( center.x - halfWidth , center.y + halfHeight , center.z + halfDepth );
		Vector4 p4 = new Vector4( center.x - halfWidth , center.y + halfHeight , center.z - halfDepth );
		
		Vector4 p2 = new Vector4( center.x + halfWidth , center.y + halfHeight , center.z + halfDepth );
		Vector4 p3 = new Vector4( center.x + halfWidth , center.y + halfHeight , center.z + halfDepth );
		
		Vector4 p6 = new Vector4( center.x - halfWidth , center.y - halfHeight , center.z + halfDepth );
		Vector4 p7 = new Vector4( center.x + halfWidth , center.y - halfHeight , center.z + halfDepth );		
		
		Vector4 p8 = new Vector4( center.x + halfWidth , center.y - halfHeight , center.z - halfDepth );
		Vector4 p5 = new Vector4( center.x - halfWidth , center.y - halfHeight , center.z - halfDepth );
		
	}

	@Override
	public IntersectionInfo intersect(Ray ray) 
	{
		return null;
	}

	@Override
	public Vector4 normalVectorAt(Vector4 point) 
	{
		return null;
	}
}
