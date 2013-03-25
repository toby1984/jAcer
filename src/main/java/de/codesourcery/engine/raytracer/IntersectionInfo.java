package de.codesourcery.engine.raytracer;


public class IntersectionInfo {

	public Raytracable object;
	public int solutionCount=0;
	public final double[] solutions =new double[5];
	public Vector4 nearestIntersectionPoint;

	public IntersectionInfo(Raytracable obj) 
	{
		this.object = obj;
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
	    return "Intersection[ obj: "+object+" , solutions: "+solutionCount+" , solution_0: "+solutions[0]+" , solution_1: "+solutions[1]+" ]";
	}
}