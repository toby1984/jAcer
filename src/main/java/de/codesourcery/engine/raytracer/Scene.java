package de.codesourcery.engine.raytracer;

import java.util.ArrayList;
import java.util.List;

public class Scene {

	public final List<Raytracable> objects = new ArrayList<>();
	public final List<Lightsource> lightsources = new ArrayList<>();

	public static final double EPSILON = 0.001;
	
	public Vector4 ambientColor=new Vector4(0,0,0);
	
	public Camera camera;
	
	public Scene(Camera camera) {
	    this.camera = camera;
	}
	
	public void addObject(Raytracable obj) 
	{
		objects.add( obj );
	}
	
	public void addObject(Lightsource obj) 
	{
		lightsources.add( obj );
	}	
	
	public IntersectionInfo findNearestIntersection(Ray ray) 
	{
		IntersectionInfo result = null;
		double distanceToSolution = Double.MAX_VALUE;
		
		for ( Raytracable obj : objects ) 
		{
		    final IntersectionInfo intersection = obj.intersect( ray );
			if ( intersection != null ) 
			{
			    final double distance = intersection.nearestIntersectionPoint.distanceTo( ray.point );
			    if ( result == null || ( distance < distanceToSolution && distance > EPSILON ) ) {			    
			        result = intersection;
			        distanceToSolution = distance;
			    }
			}
		}
		return result;
	}
	
	/**
	 * Looks for any intersections with a non-lightsource object.
	 * 
	 * @param ray
	 * @param tStart
	 * @return
	 */
	public IntersectionInfo hasAnyIntersection(Ray ray) 
	{
		for ( Raytracable obj : objects ) 
		{
			final IntersectionInfo intersection = obj.intersect( ray );
			if ( intersection != null && intersection.nearestIntersectionPoint.distanceTo( ray.point ) > EPSILON ) 
			{
			    return intersection;
			}
		}
		return null;
	}		
}