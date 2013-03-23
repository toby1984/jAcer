package de.codesourcery.engine.raytracer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Raytracer {

	public  Vector4 eyePosition; // eye position relative to view plane at (0,0,0)
	public Scene scene;
	
	public Raytracer(Vector4 eyePosition,Scene scene) 
	{
		this.eyePosition = eyePosition;
		this.scene = scene;
	}
	
	public BufferedImage trace(int imageWidth, int imageHeight) {
		
		final BufferedImage image = new BufferedImage(imageWidth, imageHeight , BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = (Graphics2D) image.getGraphics();
		
		final int centerX = imageWidth / 2;
		final int centerY = imageHeight / 2;
		
		final int x1 = -(imageWidth/2);
		final int x2 = (imageWidth/2);
		
		final int y1 = -(imageHeight/2);
		final int y2 = (imageHeight/2);		
		
		final Vector4 p0 = new Vector4(0,0,0);
		final Vector4 p1 = new Vector4(-5,0,0);
		final Vector4 p2 = new Vector4(0,5,0);
		
		final Vector4 n1 = p1.minus( p0 );
		final Vector4 n2 = p2.minus( p0 );
		
		final Vector4 viewPlaneNormalVector = n1.crossProduct( n2 ); // xAxis X yAxis
		final Plane viewPlane = new Plane( new Vector4(0,0,0 ) , viewPlaneNormalVector );
		
		final double xInc = 1; 
		final double yInc = 1; 
		
		for ( float x = x1 ; x < x2 ; x+=xInc ) 
		{
			for ( float y = y1 ; y < y2 ; y+=yInc ) 
			{
				final Vector4 pointOnViewPlane = new Vector4( x , y , 0 );
				final Ray ray = new Ray( eyePosition , pointOnViewPlane.minus( eyePosition ) );
				final double tStart = viewPlane.intersect( ray ).solutions[0];
				final IntersectionInfo intersection = scene.findNearestIntersection(ray , tStart );
				if ( intersection != null ) 
				{
					final float distance = intersection.nearestIntersectionPoint.distanceTo( pointOnViewPlane );
					float factor = 0.1f*(float)Math.sqrt( distance );
					if ( factor > 1 ) {
						factor = 1;
					} else if ( factor < 0 ) {
						factor = 0;
					}
					final Color color = new Color( 1 * factor , 0 , 0  );
					graphics.setColor(color);
					graphics.drawRect( centerX + (int) x , centerY - (int) y , 1 ,1 );
				}
			}
		}
		return image;
	}
}