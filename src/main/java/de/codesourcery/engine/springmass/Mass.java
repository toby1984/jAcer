package de.codesourcery.engine.springmass;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.codesourcery.engine.raytracer.FinitePlane;
import de.codesourcery.engine.raytracer.Vector4;

public class Mass {
	
	public Vector4 currentPosition;
	public Vector4 previousPosition;	
	
	public final double mass = 1;
	
	public final Color color;
	
	public final List<Spring> springs = new ArrayList<>();
	
	private byte flags;
	
	public static enum Flag 
	{
		FIXED(0),
		SELECTED(1);
		
		private final byte mask;
		
		private Flag( int bit ) {
			this.mask = (byte) ( 1 << bit );
		}
		
		public boolean isSet(byte in) {
			return ( in & mask ) != 0;
		}
		
		public byte setOrClear(byte in, boolean set) {
			if ( set ) {
				return (byte) ( in | mask );
			} 
			return (byte) (in & ~mask );
		}
	}
	
	public Vector4 calculateNeighbourForces() {
		
		final Vector4 result = new Vector4();
		for ( Spring s : springs ) 
		{
			final Vector4 force = s.calculateForce(this);
//			System.out.println("Force of "+s+" on "+this+" => "+force);
			result.plusInPlace( force );
		}
		return result;
	}
	
	@Override
	public String toString() {
		return "Mass( "+currentPosition+" )";
	}
	
	public List<Spring> getLeftSprings() 
	{
		final List<Spring> result = new ArrayList<>();
		for ( Spring s : springs ) {
			final Mass candidate;
			if ( s.m1 == this ) {
				candidate = s.m2;
			} else {
				candidate = s.m1;
			}
			if ( candidate.currentPosition.x < this.currentPosition.x ) 
			{
				result.add( s );
			}
		}
		return result;
	}
	
	public Set<Spring> getIntersectingSprings(FinitePlane p) 
	{
		final Set<Spring> result = new HashSet<>();
		for ( Spring s : springs ) 
		{
			if ( s.intersects(p ) ) 
			{
				result.add( s );
			}
		}
		return result;
	}
	
	public void setPosition(Vector4 p) {
		this.currentPosition = new Vector4(p);
		this.previousPosition = new Vector4(p);
	}
	
	public double distanceTo(Mass other) {
		return currentPosition.distanceTo( other.currentPosition );
	}
	
	public Mass createCopy() 
	{
		final Mass result = new Mass(this.color , new Vector4( this.currentPosition ) );
		result.flags = this.flags;
		return result;
	}

	public void setFixed(boolean yesNo) {
		flags = Flag.FIXED.setOrClear( flags  , yesNo );
	}
	
	public void addSpring(Spring s) 
	{
		s.m1.springs.add( s );
		s.m2.springs.add( s );
	}
	
	public boolean isFixed() {
		return Flag.FIXED.isSet( flags );
	}
	
	public void setSelected(boolean yesNo) {
		flags = Flag.SELECTED.setOrClear( flags  , yesNo );
	}	
	
	public boolean isSelected() {
		return Flag.SELECTED.isSet( flags );
	}	
	
	public Mass(Color color,Vector4 position) {
		if (position == null) {
			throw new IllegalArgumentException("position must not be null");
		}
		this.color = color;
		setPosition(position);
	}

	public double squaredDistanceTo(Vector4 other) {
		return currentPosition.distanceTo( other );
	}
}