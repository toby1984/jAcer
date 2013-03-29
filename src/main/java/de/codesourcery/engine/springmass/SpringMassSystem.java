package de.codesourcery.engine.springmass;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import de.codesourcery.engine.raytracer.Vector4;

public class SpringMassSystem {

	public List<Mass> masses = new ArrayList<>();
	public List<Spring> springs = new ArrayList<>();
	
	public void addMass(Mass m) 
	{
		this.masses.add( m );
	}
	
	public void addSpring(Spring s) 
	{
		springs.add( s );
	}
	
	public Mass getNearest(Vector4 pos,double maxDistanceSquared) {
		
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

	public synchronized void step() 
	{
		final IdentityHashMap<Mass,Mass> newMasses = new IdentityHashMap<>();
		for ( Mass m : masses ) {
			Mass copy = m.createCopy();
			copy.movementSum = new Vector4(0,0,0);
			copy.springCount = 0;			
			newMasses.put( m , copy );
		}
		
		final double EPSILON = 1;
		
		final List<Spring> newSprings = new ArrayList<>();
		for ( Spring s : springs ) 
		{
			final Mass m1 = newMasses.get( s.m1 );
			final Mass m2 = newMasses.get( s.m2 );
			
			newSprings.add( new Spring( m1 , m2 , s.restLen ) );
			
			double delta = s.m1.distanceTo( s.m2 ) - s.restLen;
			if ( delta > EPSILON ) {
				// contract
				Vector4 direction = s.m2.position.minus( s.m1.position ).normalize();
				m1.movementSum.plusInPlace( direction.multiply( delta / 2.0 ) );
				m2.movementSum.plusInPlace( direction.multiply( -delta / 2.0 ) );
				
				m1.springCount++;
				m2.springCount++;
			} 
			else if ( delta < -EPSILON )
			{
				// expand
				Vector4 direction = s.m2.position.minus( s.m1.position ).normalize();
				m1.movementSum.plusInPlace( direction.multiply( -delta / 2.0 ) );
				m2.movementSum.plusInPlace( direction.multiply( delta / 2.0 ) );		
				
				m1.springCount++;
				m2.springCount++;				
			}
		}
		
		this.springs = newSprings;
		this.masses = new ArrayList<>( newMasses.values() );
		for ( Mass m : this.masses ) 
		{
			if ( m.springCount > 1 ) {
				m.movementSum.multiplyInPlace( 1.0 / (double) m.springCount );
			}
			m.position.plusInPlace( m.movementSum );
		}
	}
}
