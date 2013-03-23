package de.codesourcery.engine.raytracer;


public class IntersectionInfo {

	public Raytracable object;
	public int solutionCount=0;
	public final double[] solutions =new double[5];
	public Vector4 nearestIntersectionPoint;

	public IntersectionInfo(Raytracable obj) {
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
}
