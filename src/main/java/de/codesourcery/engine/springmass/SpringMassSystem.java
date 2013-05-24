package de.codesourcery.engine.springmass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.codesourcery.engine.raytracer.Plane;
import de.codesourcery.engine.raytracer.Vector4;

public class SpringMassSystem {

	public List<Mass> masses = new ArrayList<>();

	public static final double MAX_SPEED = 20;
	
	public void addMass(Mass m) 
	{
		this.masses.add( m );
	}
	
	public Mass getNearestMass(Vector4 pos,double maxDistanceSquared) {
		
		Mass best = null;
		double closestDistance = Double.MAX_VALUE; 
		for ( Mass m : masses ) 
		{
			double distance = m.squaredDistanceTo( pos ); 
			if ( best == null || distance < closestDistance ) 
			{
				best = m;
				closestDistance = distance; 
			}
		}
		return closestDistance > maxDistanceSquared ? null : best;
	}
	
	public Set<Spring> getIntersectingSprings(double xCoordinate,Vector4 point,double maxDistance) 
	{
		final Set<Spring> result = new HashSet<>();
		for ( Mass m : masses ) 
		{
			for ( Spring s : m.springs ) 
			{
				if ( ( s.m1.currentPosition.x <= xCoordinate && s.m2.currentPosition.x >= xCoordinate ) ||
					   s.m2.currentPosition.x <= xCoordinate && s.m1.currentPosition.x >= xCoordinate ) 
				{  						
					if ( s.distanceTo( point) <= maxDistance) {
						result.add( s );
					}
				}
			}
		}
		return result;
	}
	
	public Set<Spring> getSprings() {
		
		final Set<Spring> result = new HashSet<Spring>();
		for ( Mass m : masses ) {
			result.addAll( m.springs );
		}
		return result;
	}
	
	public synchronized void removeSpring(Spring s) 
	{
		s.m1.springs.remove( s );
		s.m2.springs.remove( s );
	}
	
	public void addSpring(Spring s) {
		s.m1.addSpring( s );
	}
 
	public synchronized void step() 
	{
		final IdentityHashMap<Mass, Vector4> newForces=new IdentityHashMap<>();
		
		final double deltaT = 6;
		final double deltaTSquared = deltaT*deltaT;
		for ( Mass m : masses ) 
		{
			if ( ! m.isFixed() && ! m.isSelected() ) 
			{
				final Vector4 internalForces = m.calculateNeighbourForces();
				newForces.put( m , internalForces );
			}
		}
		
		// 
		
		/* Apply forces.
		 * 
		 * F = total of forces acting on this point
		 * T = Time step to update over
		 * X0 is the previous position, X1 is the current position
		 * XT = X1
		 * X1 += (X1-X0) + F/M*T*T
		 * X0 = XT		 
		 */
		Vector4 gravity = new Vector4(0,1,0).multiply(9.81);
		for ( Entry<Mass, Vector4> entry : newForces.entrySet() ) 
		{
		   final Mass mass = entry.getKey();
		   
		   final Vector4 sumForces = entry.getValue();
		   // apply gravity
		   sumForces.plusInPlace( gravity );
		   
		   final Vector4 tmp = new Vector4(mass.currentPosition);
		   
		   final Vector4 posDelta = mass.currentPosition.minus(mass.previousPosition);
		   
		   sumForces.multiplyInPlace( 1.0 / (mass.mass*deltaTSquared) );
		   posDelta.plusInPlace( sumForces );
		   
		   posDelta.clampMagnitudeInPlace( MAX_SPEED );
		   mass.currentPosition.plusInPlace( posDelta );
		   mass.previousPosition = tmp;
		}
		
	}
	
}
