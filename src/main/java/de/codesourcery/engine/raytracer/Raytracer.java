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
	
	public BufferedImage trace(int w, int h) {
		
		final BufferedImage image = new BufferedImage(w, h , BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = (Graphics2D) image.getGraphics();
		
		final int centerX = w / 2;
		final int centerY = h / 2;
		
		final int max = ((w > h ? w : h)/2)+1;
		final int x1 = -max;
		final int x2 = max;
		
		final int y1 = -max;
		final int y2 = max;
		
		final double scaleX = w / (double) max/2.0;
		final double scaleY = h / (double) max/2.0;
		
		final Vector4 p0 = new Vector4(0,0,0);
		final Vector4 p1 = new Vector4(-5,0,0);
		final Vector4 p2 = new Vector4(0,5,0);
		
		final Vector4 n1 = p1.minus( p0 );
		final Vector4 n2 = p2.minus( p0 );
		
		final Vector4 viewPlaneNormalVector = n1.crossProduct( n2 ); // xAxis X yAxis
		final Plane viewPlane = new Plane( new Vector4(0,0,0 ) , viewPlaneNormalVector );
		
		for ( int x = x1 ; x < x2 ; x+=1) 
		{
			for ( int y = y1 ; y < y2 ; y+=1 ) 
			{
				final Vector4 pointOnViewPlane = new Vector4( x , y , 0 );
				final Ray ray = new Ray( eyePosition , pointOnViewPlane.minus( eyePosition ) );
				final double tStart = viewPlane.intersect( ray ).solutions[0];
				final IntersectionInfo intersection = scene.findNearestIntersection(ray , tStart );
				if ( intersection != null ) 
				{
					/* direct lighting
					 * 
					 * - cast rays from intersection point to each light source
					 * - if a ray does not intersect with any other object (=light source is not occluded), calculate the lighting 
					 *   components coming from the respective light source   
					 */
					final Vector4 normalAtIntersection = intersection.normalAtIntersection();
					final Vector4 intersectionPoint = intersection.nearestIntersectionPoint;
					
					final Material objMaterial = intersection.object.material;
					
					Vector4 sumAmbient=new Vector4(0,0,0);
					Vector4 sumDiff=new Vector4(0,0,0);
					Vector4 sumSpec=new Vector4(0,0,0);
					
					for ( Lightsource light : scene.lightsources ) 
					{
						final Vector4 lightDir = light.position.minus( intersectionPoint ).normalize();
						final Ray rayToLight = new Ray( intersectionPoint , lightDir );
						if ( ! scene.hasAnyIntersection( rayToLight , 0 ) ) 
						{
							// light source is visible from intersection point
							
							// calculate ambient color
							Vector4 ambientColor = objMaterial.ambientColor.straightMultiply( light.ambientColor );
							
							// calculate diffuse color
							double dotProduct = Math.max( 0 , normalAtIntersection.dotProduct( lightDir ) );
							Vector4 diffuseColor = objMaterial.diffuseColor.straightMultiply( light.diffuseColor ).multiply( dotProduct );
							
							// specular color
							Vector4 reflected = Raytracable.reflect( rayToLight.v.multiply(-1) , normalAtIntersection );
							double eyeReflectionAngle = Math.max( 0 , normalAtIntersection.dotProduct( reflected ) );
							double fspec = Math.pow( eyeReflectionAngle , objMaterial.shininess );
							
							Vector4 specularColor = light.specularColor.straightMultiply( objMaterial.specularColor ).multiply( fspec );

							sumAmbient = sumAmbient.plus( ambientColor ).clamp(0,1);
							sumDiff = sumDiff.plus( diffuseColor ).clamp(0.0d,1.0d);
							sumSpec = sumSpec.plus( specularColor ).clamp(0,1);
						} 
					}

					Vector4 sum = sumAmbient.plus(sumDiff).plus(sumSpec).clamp(0, 1);
					graphics.setColor( new Color((float) sum.x() , (float) sum.y(),(float) sum.z() ) );
//					graphics.setColor(Color.RED);
					graphics.drawRect( centerX + (int) Math.ceil( x*scaleX ) , centerY - (int) Math.ceil( y * scaleY ) , 1 ,1 );
				}
			}
		}
		return image;
	}
}