package de.codesourcery.engine.raytracer;

import org.apache.commons.lang.StringUtils;


public class IntersectionInfo {

	public Raytracable object;
	
	public Vector4 nearestIntersectionPoint;
	
	public Raytracable[] subsurfacesHit;

	public IntersectionInfo(Raytracable obj) 
	{
		this.object = obj;
	}
	
	public IntersectionInfo subsurfaces(Raytracable... surfaces) {
		this.subsurfacesHit = surfaces;
		return this;
	}
	
	public Vector4 normalAtIntersection() 
	{
		return object.normalVectorAt( nearestIntersectionPoint );
	}
	
	@Override
	public String toString()
	{
		if ( subsurfacesHit != null ) {
		    return "Intersection[ obj: "+object+" , "+
		   " surfaces: "+StringUtils.join( subsurfacesHit , ", " )+" ]";
		}
	    return "Intersection[ obj: "+object+" ]";
	}
}