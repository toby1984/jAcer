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
	
	public IntersectionInfo findNearestIntersection(Ray ray,double tStart) 
	{
		IntersectionInfo result = null;
		double nearestIntersection = 0;
		
		for ( Raytracable obj : objects ) 
		{
			final IntersectionInfo intersection = obj.intersect( ray );
			if ( intersection != null ) 
			{
				for ( int i = 0 ; i < intersection.solutionCount ; i++ ) 
				{
					final double solution = intersection.solutions[i];
					if ( solution > tStart ) 
					{
						// only add intersection if we didn't find any so far OR
						// if this intersection is closer to tStart than the one we already found
						if ( result == null || solution < nearestIntersection ) 
						{
							result = intersection;
							nearestIntersection = solution;
						} 
					}
				}
			}
		}
		
		if ( result != null ) {
			result.nearestIntersectionPoint = ray.evaluateAt( nearestIntersection );
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
	public IntersectionInfo hasAnyIntersection(Ray ray,double tStart) 
	{
		for ( Raytracable obj : objects ) 
		{
			final IntersectionInfo intersection = obj.intersect( ray );
			if ( intersection != null ) 
			{
				for ( int i = 0 ; i < intersection.solutionCount ; i++ ) 
				{
					final double solution = intersection.solutions[i];
					final double delta = solution - tStart;
					if ( delta > EPSILON ) 
					{
						return intersection;
					}
				}
			}
		}
		return null;
	}		
}