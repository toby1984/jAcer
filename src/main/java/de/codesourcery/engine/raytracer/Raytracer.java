package de.codesourcery.engine.raytracer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Raytracer {

	public Scene scene;
	
	public Raytracer(Scene scene) 
	{
		this.scene = scene;
	}
	
	public BufferedImage trace(int imageWidth, int imageHeight) {
		
		final BufferedImage image = new BufferedImage(imageWidth, imageHeight , BufferedImage.TYPE_INT_ARGB);
		final Graphics2D graphics = (Graphics2D) image.getGraphics();
		
		final Vector4 eyePosition = scene.camera.position;
		
		final int centerX = imageWidth / 2;
		final int centerY = imageHeight / 2;
		
		// calculate view plane
	    final double max = (imageWidth > imageHeight ? imageWidth : imageHeight);
		         
		final double x1 = -(max/2.0);
		final double x2 = max/2.0;
		
		final double y1 = -(max/2.0d);
		final double y2 = max/2.0;
		
		final double scaleX = imageWidth / max;
		final double scaleY = imageHeight / max;
		
		final Vector4 p0 = new Vector4(0,0,0);
		final Vector4 p1 = new Vector4(-5,0,0);
		final Vector4 p2 = new Vector4(0,5,0);
		
		final Vector4 n1 = p1.minus( p0 );
		final Vector4 n2 = p2.minus( p0 );
		
		final Vector4 viewPlaneNormalVector = n1.crossProduct( n2 ); // (xAxis) x (yAxis)
		final Plane viewPlane = new Plane( new Vector4( 0,0,0 ) , viewPlaneNormalVector );
		
		for ( double x = x1 ; x < x2 ; x+=1) 
		{
			for ( double y = y1 ; y < y2 ; y+=1 ) 
			{
			    // cast ray from camera position through view plane
				final Vector4 pointOnViewPlane = new Vector4( x , y , 0 );
				final Ray ray = new Ray( eyePosition , pointOnViewPlane.minus( eyePosition ) );
				final double tStart = viewPlane.intersect( ray ).solutions[0];
				
				final IntersectionInfo intersection = scene.findNearestIntersection(ray , tStart );
				if ( intersection != null ) 
				{
					final Vector4 sum = calculateColorAt(intersection);
					graphics.setColor( new Color((float) sum.x() , (float) sum.y(),(float) sum.z() ) );
					graphics.drawRect( centerX + (int) Math.ceil( x*scaleX ) , centerY - (int) Math.ceil( y * scaleY ) , 1 ,1 );
				} else {
//				    System.out.println("No intersection: "+ray);
				}
			}
		}
		return image;
	}

    private Vector4 calculateColorAt(final IntersectionInfo intersection)
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
        
        Vector4 sumDiff=new Vector4(0,0,0);
        Vector4 sumSpec=new Vector4(0,0,0);
        
        for ( Lightsource light : scene.lightsources ) 
        {
        	final Vector4 lightDir = light.position.minus( intersectionPoint ).normalize();
        	final Ray rayToLight = new Ray( intersectionPoint , lightDir );
        	// check whether ray cast to light source does not pass through any other objects 
        	if ( ! scene.hasAnyIntersection( rayToLight , 0 ) ) 
        	{
        		// calculate diffuse color
        		double dotProduct = Math.max( 0 , normalAtIntersection.dotProduct( lightDir ) );
        		Vector4 diffuseColor = objMaterial.diffuseColor.straightMultiply( light.diffuseColor ).multiply( dotProduct );
        		
        		// specular color
        		Vector4 reflected = Raytracable.reflect( rayToLight.v.multiply(-1) , normalAtIntersection );
        		double eyeReflectionAngle = Math.max( 0 , normalAtIntersection.dotProduct( reflected ) );
        		double fspec = Math.pow( eyeReflectionAngle , objMaterial.shininess );
        		
        		Vector4 specularColor = light.specularColor.straightMultiply( objMaterial.specularColor ).multiply( fspec );

        		sumDiff = sumDiff.plus( diffuseColor ).clamp(0.0d,1.0d);
        		sumSpec = sumSpec.plus( specularColor ).clamp(0,1);
        	} 
        }
        return scene.ambientColor.plus(sumDiff).plus(sumSpec).clamp(0, 1);
    }
}