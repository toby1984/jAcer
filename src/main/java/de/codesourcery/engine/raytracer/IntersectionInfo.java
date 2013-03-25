package de.codesourcery.engine.raytracer;

import org.apache.commons.lang.StringUtils;


public class IntersectionInfo {

	public Raytracable object;
	
	public int solutionCount=0;
	public final double[] solutions =new double[5];
	
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
	
	public IntersectionInfo addSolution(double t) {
		solutions[ solutionCount++ ] = t;
		return this;
	}
	
	public IntersectionInfo addSolutions(double t1,double t2) {
		solutions[ solutionCount++ ] = t1;
		solutions[ solutionCount++ ] = t2;
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
		    return "Intersection[ obj: "+object+" , solutions: "+solutionCount+" , solution_0: "+solutions[0]+" , solution_1: "+solutions[1]+" , "+
		   " surfaces: "+StringUtils.join( subsurfacesHit , ", " )+" ]";
		}
	    return "Intersection[ obj: "+object+" , solutions: "+solutionCount+" , solution_0: "+solutions[0]+" , solution_1: "+solutions[1]+" ]";
	}
}