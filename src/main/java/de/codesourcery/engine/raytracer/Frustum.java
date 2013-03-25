package de.codesourcery.engine.raytracer;


public final class Frustum  
{  
	private static final int TOP=0, BOTTOM =1, LEFT=2,RIGHT=3, NEAR=4, FAR=5;

	private final Plane[] planes = new Plane[6];
	private double tang;
	private double nearPlaneWidth,nearPlaneHeight,farPlaneWidth,farPlaneHeight;	
	
	private final Camera camera;
	
	public Frustum(Camera camera) 
	{
		for ( int i = 0 ; i < planes.length ; i++ ) {
			planes[i] = new Plane("Plane "+i,new Vector4(),new Vector4());
		}
		this.camera = camera;
		recalculate();
	}
	
	public Plane getNearPlane() {
	    return planes[NEAR];
	}
	
	public double getNearPlaneHeight() {
	    return nearPlaneHeight;
	}
	
    public double getNearPlaneWidth() {
        return nearPlaneWidth;
    }	
	
	public void recalculate() 
	{
		// compute width and height of the near and far plane sections
		tang = (float) Math.tan( Constants.DEG_TO_RAD * camera.fov * 0.5f) ;
		
		nearPlaneHeight = camera.zNear * tang;
		nearPlaneWidth = nearPlaneHeight * camera.aspectRatio;
		
		farPlaneHeight = camera.zFar  * tang;
		farPlaneWidth = farPlaneHeight * camera.aspectRatio;
		
		recalculatePlaneDefinitions();
	}
	
	@Override
	public String toString() {
		
		String result = "Plane TOP    = "+planes[TOP]+"\n"+
						"Plane BOTTOM = "+planes[BOTTOM]+"\n"+
						"Plane LEFT   = "+planes[LEFT]+"\n"+
						"Plane RIGHT  = "+planes[RIGHT]+"\n"+
						"Plane NEAR   = "+planes[NEAR]+"\n"+
						"Plane FAR    = "+planes[FAR];
		return result;
	}
	
	public void forceRecalculatePlaneDefinitions() {
		recalculatePlaneDefinitions();
	}
	
	private synchronized void recalculatePlaneDefinitions() 
	{
		// compute the Z axis of camera
		Vector4 Z = new Vector4(camera.viewOrientation).multiply(-1);
		Z.normalizeInPlace();

		// X axis of camera with given "up" vector and Z axis
		Vector4 X = camera.up.crossProduct( Z );
		X.normalizeInPlace();

		// the real "up" vector is the cross product of Z and X
		Vector4 Y = Z.crossProduct( X );

		// compute the centers of the near and far planes
		final Vector4 nc = camera.eyePosition.minus( Z.multiply( camera.zNear ) );
		final Vector4 fc = camera.eyePosition.minus( Z.multiply( camera.zFar) ); 

		planes[NEAR].setNormalAndPoint( Z.multiply(-1f) , nc);
		planes[FAR].setNormalAndPoint(  Z               , fc);

		Vector4 aux = ( nc.plus(Y.multiply(nearPlaneHeight) ) ).minus( camera.eyePosition );
		aux.normalizeInPlace();
		Vector4 normal = aux.crossProduct( X );
		planes[TOP].setNormalAndPoint(normal, nc.plus( Y.multiply( nearPlaneHeight ) ) );

		aux = (nc.minus( Y.multiply( nearPlaneHeight) ) ).minus( camera.eyePosition );
		aux.normalizeInPlace();
		normal = X.crossProduct( aux );
		planes[BOTTOM].setNormalAndPoint(normal,nc.minus( Y.multiply( nearPlaneHeight ) ));
		
		aux = (nc.minus( X.multiply(nearPlaneWidth) ) ).minus(camera.eyePosition);
		aux.normalizeInPlace();
		normal = aux.crossProduct( Y );
		planes[LEFT].setNormalAndPoint(normal, nc.minus( X.multiply( nearPlaneWidth) ) );

		aux = (nc.plus( X.multiply(nearPlaneWidth) )).minus( camera.eyePosition );
		aux.normalizeInPlace();
		normal = Y.crossProduct( aux );
		planes[RIGHT].setNormalAndPoint(normal,nc.plus( X.multiply(nearPlaneWidth)));	
	}
}