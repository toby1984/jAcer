package de.codesourcery.engine.raytracer;

import java.util.ArrayList;
import java.util.List;

public class Scene {

	public final List<Raytracable> objects = new ArrayList<>();
	public final List<Lightsource> lightsources = new ArrayList<>();

	public static final double EPSILON = 0.00000001;
	
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
		if( tStart < 0 ) {
			throw new IllegalArgumentException("Should not happen");
		}
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
					final double delta = solution - tStart;
//					System.out.println("Solution at "+intersection.solutions[i]+" / tStart: "+tStart+" , delta: "+delta);
					if ( delta > EPSILON ) 
					{
						// only add intersection if we didn't find any so far OR
						// if this intersection is closer to tStart than the one we already found
						if ( result == null || solution < nearestIntersection ) 
						{
							// make sure the intersection point is actually facing the camera
							// by checking the angle between the ray vector
							// and the surface normal at the intersection point
							
//							final Vector4 intersectionPoint = ray.evaluateAt( intersection.solutions[i] );
//							final Vector4 normalVectorAt = intersection.object.normalVectorAt( intersectionPoint );
							
//							if ( normalVectorAt.dotProduct( ray.v ) > 0 ) // angle is less than 90 degrees 
//							{
								result = intersection;
								nearestIntersection = solution;
//							}
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
}