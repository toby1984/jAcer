package de.codesourcery.engine.raytracer;

public class OrthonormalBasis {

	public static void fromZAxis(Vector4 xAxis,Vector4 yAxis,Vector4 zAxis) 
	{
		/*
	     - Set the smallest (in absolute value) component of R to zero.
	     
	     - Exchange the other two components of R, and then negate the first one.
	        S = ( 0, -Rz, Ry ), in case Rx is smallest.
	        
	     - Normalize vector S.
	        S = S / |S|
	     - Last vector T is a cross product of R and S then.
	        T = R x S
		*/
		
		double x = zAxis.x;
		double y = zAxis.y;
		double z = zAxis.z;
		
		final Vector4 r;
		final double temp;
		if ( x <= y && x <= z ) // ( Rx* , Ry , Rz ) 
		{
			x = 0;
			r = new Vector4(x,y,z);
			temp = y;
			y = -z;
			z= temp;
		} else if ( y <= x && y <= z ) { // ( Rx , Ry* , Rz )
			y = 0;
			r = new Vector4(x,y,z);
			temp = x;
			x = -z;
			z = temp;
		} else { // ( Rx , Ry , Rz* )
			z = 0;
			r = new Vector4(x,y,z);
			temp=x;
			x = -y;
			y=temp;
		}
		
		yAxis.x = x;
		yAxis.y = y;
		yAxis.z = z;
		
		yAxis.normalizeInPlace();
		
		r.crossProduct( yAxis ).copyInto( xAxis );
		yAxis.flipInPlace();
	}
}