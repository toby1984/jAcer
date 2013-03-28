package de.codesourcery.engine.raytracer;

import org.apache.commons.lang.StringUtils;


public class IntersectionInfo {

	public final Raytracable object;
	public final int solutionCount;
	public final double[] solutions;
	
	public Vector4 nearestIntersectionPoint;
	
	public Raytracable[] subsurfacesHit;

	public IntersectionInfo(Raytracable obj,double solution) 
	{
		this.object = obj;
		this.solutionCount = 1;
		solutions=new double[]{solution};
	}
	
	public IntersectionInfo(Raytracable obj,double solution1,double solution2) 
	{
		this.object = obj;
		this.solutionCount = 2;
		this.solutions=new double[]{solution1,solution2};
	}	
	
	public IntersectionInfo subsurfaces(Raytracable... surfaces) {
		this.subsurfacesHit = surfaces;
		return this;
	}
	
	public Vector4 normalAtIntersection(Camera camera)
	{
		return object.normalVectorAt( nearestIntersectionPoint , camera );
	}
	
	@Override
	public String toString()
	{
		String solutionText="solutions: ";
		switch(solutionCount) {
			case 0:
				solutionText+= "<none>";
				break;
			case 1:
				solutionText += this.solutions[0];
				break;
			case 2:
				solutionText += this.solutions[0]+" , "+this.solutions[1];
				break;
			default:
				solutionText += " <internal error>";
		}
		if ( subsurfacesHit != null ) {
		    return "Intersection[ obj: "+object+" , "+solutionText+" , "+
		   " surfaces: "+StringUtils.join( subsurfacesHit , ", " )+" ]";
		}
	    return "Intersection[ obj: "+object+" , "+solutionText+" ]";
	}
}