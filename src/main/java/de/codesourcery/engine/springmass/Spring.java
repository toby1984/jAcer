package de.codesourcery.engine.springmass;

public class Spring {

	public final Mass m1;
	public final Mass m2;
	
	public double restLen = 10;
	
	public Spring(Mass m1, Mass m2,double restLength) 
	{
		if ( m1 == null ) {
			throw new IllegalArgumentException("m1 must not be null");
		}
		if ( m2 == null ) {
			throw new IllegalArgumentException("m2 must not be null");
		}
		this.restLen = restLength;
		this.m1 = m1;
		this.m2 = m2;
	}
}
