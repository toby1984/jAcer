package de.codesourcery.engine.springmass;

import de.codesourcery.engine.raytracer.Vector4;

public class Mass {
	
	public Vector4 position;
	
	public Vector4 movementSum;
	public int springCount = 0;
	
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
	
	public double distanceTo(Mass other) {
		return position.distanceTo( other.position );
	}
	
	public Mass createCopy() 
	{
		final Mass result = new Mass(new Vector4( this.position ) );
		result.flags = this.flags;
		return result;
	}

	public void setFixed(boolean yesNo) {
		flags = Flag.FIXED.setOrClear( flags  , yesNo );
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
	
	public Mass(Vector4 position) {
		if (position == null) {
			throw new IllegalArgumentException("position must not be null");
		}
		this.position = position;
	}

	public double squaredDistanceTo(Vector4 other) {
		return position.distanceTo( other );
	}
}