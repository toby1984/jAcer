package de.codesourcery.engine.raytracer;

import java.util.ArrayList;
import java.util.List;

public class LinAlgUtils
{
	public static Matrix createMatrix(Vector4 v1,Vector4 v2,Vector4 v3,Vector4 v4) 
	{
		return new Matrix(v1,v2,v3,v4);
	}

	public static Matrix identity() 
	{
		return Matrix.identity();
	}

	public static Matrix mult(Matrix m1 , Matrix m2) 
	{
		return m1.multiply( m2 );
	}

	public static Matrix rotX(double angleInDegrees) 
	{
		final double angleInRad = (double) ( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );

		final double cos = (double) Math.cos( angleInRad );
		final double sin = (double) Math.sin( angleInRad );

		/*
		 *  0   0    0 0
		 *  0 cos -sin 0
		 *  0 sin  cos 0
		 *  0   0    0 0
		 */    	
		Matrix result =
				createMatrix( vector( 1, 0 , 0 , 0 ) , 
						vector( 0, cos , sin , 0 ) , 
						vector( 0 , -sin, cos , 0 ) , 
						vector( 0,0,0,1 ) );

		return result;
	}

	/**
	 * Transforms vertices from another coordinate system to model coordinates.
	 * 
	 * <p>Note that this method will update the input vertices.</p>
	 * <p>Coordinate system axis are expected to be columns 0 (x axis),1 (y axis),2 (z axis) in the
	 * input matrices.</p> 
	 * 
	 * @param vertices vertices to convert
	 * @param srcSystem the orthonormal basis of the source coordinate system 
	 * @param dstSystem the orthonormal basis of the target coordinate system
	 */
	public static void convertToCoordinateSystem(List<Vector4> vertices,Matrix dstSystem, Vector4 dstCenter) 
	{
		// 
		final Vector4 xAxis2 = dstSystem.getColumn( 0 ).normalize();
		final Vector4 yAxis2 = dstSystem.getColumn( 1 ).normalize();
		final Vector4 zAxis2 = dstSystem.getColumn( 2 ).normalize();

		Matrix result2 = new Matrix();
		result2.set( 0 , 0 , xAxis2.x() );
		result2.set( 1 , 0 , xAxis2.y() );
		result2.set( 2 , 0 , xAxis2.z() );

		result2.set( 0 , 1 , yAxis2.x() );
		result2.set( 1 , 1 , yAxis2.y() );
		result2.set( 2 , 1 , yAxis2.z() );

		result2.set( 0 , 2 , -zAxis2.x() );
		result2.set( 1 , 2 , -zAxis2.y() );
		result2.set( 2 , 2 , -zAxis2.z() );

		result2.set( 3 , 0 , -1 * xAxis2.dotProduct( dstCenter ) );
		result2.set( 3 , 1 , -1 * yAxis2.dotProduct( dstCenter ) );
		result2.set( 3 , 2 , zAxis2.dotProduct( dstCenter ) );
		result2.set( 3 , 3 , 1 );  

		result2 = result2.invert();

		for ( Vector4 v : vertices ) {
			result2.multiplyInPlace( v );
		}
	}

	public static Matrix rotY(double angleInDegrees) 
	{
		final double angleInRad = (double) ( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );

		final double cos = (double) Math.cos( angleInRad );
		final double sin = (double) Math.sin( angleInRad );

		/*
		 *  cos 0 sin 0
		 *    0 1   0 0
		 * -sin 0 cos 0
		 *    0 0   0 1
		 */    	
		Matrix result =
				createMatrix( vector( cos, 0 , -sin , 0 ) ,
						vector( 0, 1 , 0 , 0 ) , 
						vector( sin , 0 , cos , 0 ) , 
						vector( 0,0,0,1 ) );
		return result;
	}

	public static Matrix rotZ(double angleInDegrees) 
	{
		final double angleInRad =  (double)( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );

		final double cos = (double) Math.cos( angleInRad );
		final double sin = (double) Math.sin( angleInRad );

		/*
		 *  cos -sin   0 0
		 *  sin  cos   0 0
		 *    0    0   1 0
		 *    0    0   0 1
		 */    	
		Matrix result =
				createMatrix( vector( cos, sin , 0 , 0 ) ,
						vector( -sin, cos , 0 , 0 ) , 
						vector( 0 , 0 , 1 , 0 ) , 
						vector( 0,0,0,1 ) );
		return result;
	}

	public static Matrix scalingMatrix(double x , double y , double z ) {
		/*
		 *  x 0 0 0
		 *  0 y 0 0
		 *  0 0 z 0
		 *  0 0 0 1
		 */
		return createMatrix( vector( x , 0 , 0 , 0 ) , vector( 0, y , 0 , 0 ) , vector( 0 , 0, z , 0 ) , vector( 0,0,0, 1 ) );
	}

	public static Matrix translationMatrix(double x , double y , double z ) {
		/*
		 *  1 0 0 x
		 *  0 1 0 y
		 *  0 0 1 z
		 *  0 0 0 1
		 */    	
		return createMatrix( vector( 1 , 0 , 0 , 0 ) , vector( 0, 1 , 0 , 0 ) , vector( 0 , 0, 1 , 0 ) , vector( x,y,z,1 ) );
	}

	public static Vector4 vector(double x,double y , double z ) {
		return new Vector4(x,y,z);
	}

	public static Vector4 vector(double x,double y , double z ,double w) {
		return new Vector4(x,y,z,w);
	}

	public static Matrix makeFrustum(double left, double right, double bottom, double top, double near,double far) 
	{
		final double[] data = new double[16];
		
		/*
		 *     2 * near    
		 *  --------------                   0
		 *  (right - left)
		 *                                2 * near   
		 *        0                     --------------
		 *                               top - bottom
		 *                               
		 *        0                          0
		 *        
		 *        0                          0
		 */

		data[0] = 2.0f * near / (right - left);
		data[1] = 0.0f;
		data[2] = 0.0f;
		data[3] = 0.0f;

		data[4] = 0.0f;
		data[5] = 2.0f * near / (top - bottom);
		data[6] = 0.0f;
		data[7] =  0.0f;

		data[8] = (right + left) / (right - left);
		data[9] = (top + bottom) / (top - bottom);
		data[10] = ( -(far + near) / (far - near) );
		data[11] = -1.0f;

		data[12] = 0.0f;
		data[13] = 0.0f;
		data[14] = (( -2.0f * far * near) / (far - near) );
		data[15] = 0.0f;

		return new Matrix(data);
	}


	public static Matrix createOrthoProjection(double field_of_view , double aspect_ratio , double near,double far) 
	{
		final double rad = field_of_view * Constants.DEG_TO_RAD;
		double size = near * (double) Math.tan( rad / 2.0f); 

		double left = -size; // left X
		double right = size;  // right X
		double bottom = -size / aspect_ratio; // bottom Y
		double top = size / aspect_ratio; // top Y


		Matrix result = new Matrix(
				vector(2.0f / (right - left), 0, 0, 0 ) ,
				vector(0, 2.0f / (top - bottom), 0, 0),
				vector(0, 0, -2.0f / (far - near), 0),
				vector(-(right + left) / (right - left), -(top + bottom) / (top - bottom), -(far + near) / (far - near), 1 ) );

		return result;
	}

	public static Matrix createPerspectiveProjection(double field_of_view, double aspect_ratio ,double zNear, double zFar) 
	{
		return createPerspectiveProjection1(field_of_view, aspect_ratio, zNear, zFar);
	}

	private static Matrix createPerspectiveProjection1(double field_of_view, double aspect_ratio ,double zNear, double zFar) 
	{
		final double rad = field_of_view * Constants.DEG_TO_RAD;

		double size = zNear * (double) Math.tan( rad / 2.0f); 

		double xLeft = -size;
		double xRight = size;
		double yBottom = -size / aspect_ratio;
		double yTop = size / aspect_ratio;

		return makeFrustum(xLeft, xRight, yBottom,yTop, zNear, zFar);
	}

	public static Vector4 calcRotation(Vector4 point,Vector4 rotatePoint,Vector4 rotateAngle) 
	{
		final Vector4 d = new Vector4();
		
		d.x( point.x() - rotatePoint.x());
		d.y( point.y() - rotatePoint.y() );
		d.z( point.z() - rotatePoint.z() );

		// X + Y + Z Rotation
		// Internet
		double x = rotatePoint.x() + Math.cos(rotateAngle.y()) * Math.cos(rotateAngle.z()) * d.x() - Math.cos(rotateAngle.y()) * Math.sin(rotateAngle.z()) * d.y() + Math.sin(rotateAngle.y()) * d.z();
		double y = rotatePoint.y() + (Math.cos(rotateAngle.x()) * Math.sin(rotateAngle.z()) + Math.sin(rotateAngle.x()) * Math.sin(rotateAngle.y()) * Math.cos(rotateAngle.z())) * d.x() + (Math.cos(rotateAngle.x()) * Math.cos(rotateAngle.z()) - Math.sin(rotateAngle.x()) * Math.sin(rotateAngle.y()) * Math.sin(rotateAngle.z())) * d.y() - Math.sin(rotateAngle.x()) * Math.cos(rotateAngle.y()) * d.z();
		double z = rotatePoint.z() + (Math.sin(rotateAngle.x()) * Math.sin(rotateAngle.z()) - Math.cos(rotateAngle.x()) * Math.sin(rotateAngle.y()) * Math.cos(rotateAngle.z())) * d.x() + (Math.sin(rotateAngle.x()) * Math.cos(rotateAngle.z()) + Math.cos(rotateAngle.x()) * Math.sin(rotateAngle.y()) * Math.sin(rotateAngle.z())) * d.y() + Math.cos(rotateAngle.x()) * Math.cos(rotateAngle.y()) * d.z();

		return new Vector4((double) x, (double) y , (double) z );
	}    

	private static Matrix createPerspective2(double fzNear,double fzFar) 
	{
		final double fFrustumScale = 1.0f;

		double[] matrixData = new double[ 16 ];

		matrixData[0] = fFrustumScale;
		matrixData[5] = fFrustumScale;
		matrixData[10] = (fzFar + fzNear) / (fzNear - fzFar);
		matrixData[14] = (2 * fzFar * fzNear) / (fzNear - fzFar);
		matrixData[11] = -1.0f;
		return new Matrix( matrixData );
	}

	public static double findFarestDistance(Vector4 referencePoint,Vector4[] points,int pointsToCompare) 
	{
		double dist = points[0].distanceTo( referencePoint );
		for ( int i = 1 ; i < pointsToCompare ; i++) 
		{
			double tmpDist = points[i].distanceTo( referencePoint );
			if ( tmpDist > dist ) {
				dist = tmpDist;
			}
		}
		return dist;
	}     
}
