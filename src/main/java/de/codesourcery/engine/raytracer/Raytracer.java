package de.codesourcery.engine.raytracer;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class Raytracer {

	public Scene scene;
	
	private final int LINES_X = 800;
	private final int LINES_Y = 800;
	
	public Raytracer(Scene scene) 
	{
		this.scene = scene;
	}
	
	public IntersectionInfo getObjectAt(int imageWidth,int imageHeight,Point point)
	{
	        final int centerX = imageWidth / 2;
	        final int centerY = imageHeight / 2;
	        
	        final double scaleX = imageWidth / scene.camera.frustum.getNearPlaneWidth();
	        final double scaleY = imageHeight / scene.camera.frustum.getNearPlaneHeight();

	        double worldX = (point.x - centerX) / scaleX;
	        double worldY = (centerY - point.y ) / scaleY;
	        
            // calculate point on view plane
	        final Vector4 xAxis = scene.camera.xAxis.normalize();
	        final Vector4 yAxis = scene.camera.yAxis.normalize();
	        
	        final Vector4 viewPlaneNormalVector = scene.camera.viewOrientation.multiply(-1).normalize();
	        final Plane viewPlane = new Plane( "viewPlane", scene.camera.frustum.getNearPlane().pointOnPlane , viewPlaneNormalVector.multiply(-1) );	        
            final Vector4 pointOnViewPlane = viewPlane.pointOnPlane.plus( xAxis.multiply( worldX ) ).plus( yAxis.multiply( worldY) );
            
            // cast ray from camera position through view plane
            final Ray ray = new Ray( scene.camera.eyePosition , pointOnViewPlane.minus( scene.camera.eyePosition ) );
            
            // TODO: Performance - intersection ray <-> plane could use pre-calculated values !!
            final double tStart=viewPlane.intersect( ray ).solutions[0];
            return scene.findNearestIntersection(ray , tStart );	        
	}
	
	public BufferedImage trace(int imageWidth, int imageHeight) {
		
		final BufferedImage image = new BufferedImage(imageWidth, imageHeight , BufferedImage.TYPE_INT_ARGB);
		final Vector4 eyePosition = scene.camera.eyePosition;
		
		final int centerX = imageWidth / 2;
		final int centerY = imageHeight / 2;
		
		// calculate view plane
		final Frustum frustum = scene.camera.frustum;

        System.out.println( scene.camera );
        
		System.out.println("Near plane: "+scene.camera.frustum.getNearPlane());
		
	    System.out.println("Near plane width : "+scene.camera.frustum.getNearPlaneWidth() );
	    System.out.println("Near plane height: "+scene.camera.frustum.getNearPlaneHeight() );
	    
		/* Construct orthonormal basis for view plane with normal vector N:
		 * 
		 * - Set the smallest (in absolute value) component of N to zero
		 * 
         * - Exchange the other two components of N, and then negate the first one
         * 
         *     S = ( 0, -Nz, Ny ), in case Nx is smallest
         *     
         * - Normalize vector S
         *     S = S / |S|
         *     
         * - Last vector T is a cross product of R and S then
         * 
         *   T = R x S
		 */
		final Vector4 xAxis = scene.camera.xAxis.normalize();
		final Vector4 yAxis = scene.camera.yAxis.normalize();
		
		final double x1 = -scene.camera.frustum.getNearPlaneWidth()/2.0;
		final double y1 = scene.camera.frustum.getNearPlaneHeight()/2.0;
		
        final double x2 = scene.camera.frustum.getNearPlaneWidth()/2.0;
        final double y2 = -scene.camera.frustum.getNearPlaneHeight()/2.0;		
		
		final Vector4 viewPlaneNormalVector = scene.camera.viewOrientation.multiply(-1).normalize();
		final Plane viewPlane = new Plane( "viewPlane",frustum.getNearPlane().pointOnPlane , viewPlaneNormalVector.multiply(-1) );
	    System.out.println("View plane: "+viewPlane);
	    
	    final double stepX = scene.camera.frustum.getNearPlaneWidth() / LINES_X;
	    final double stepY = scene.camera.frustum.getNearPlaneHeight() / LINES_Y;
	    
        final double scaleX = imageWidth / scene.camera.frustum.getNearPlaneWidth();
        final double scaleY = imageHeight / scene.camera.frustum.getNearPlaneHeight();

        System.out.println("scale X: "+scaleX);
        System.out.println("scale Y: "+scaleY);	   
        
        System.out.println("X1: "+x1);
        System.out.println("X2: "+x2);
        
        System.out.println("Y1: "+y1);
        System.out.println("Y2: "+y2);
        
		for ( double viewX = x1 ; viewX < x2 ; viewX += stepX ) 
		{
			for ( double viewY = y1 ; viewY > y2 ; viewY -= stepY )
			{
			    // calculate point on view plane
			    final Vector4 pointOnViewPlane = viewPlane.pointOnPlane.plus( xAxis.multiply( viewX ) ).plus( yAxis.multiply( viewY ) );
			    
			    // cast ray from camera position through view plane
				final Ray ray = new Ray( eyePosition , pointOnViewPlane.minus( eyePosition ) );
				final double tStart  = viewPlane.intersect( ray ).solutions[0];
				
				final IntersectionInfo intersection = scene.findNearestIntersection(ray , tStart );
				if ( intersection != null ) 
				{
					final Vector4 color = calculateColorAt(ray,intersection);
					
					final int r = (int) (color.r() * 255.0);
					final int g = (int) (color.g() * 255.0);
					final int b = (int) (color.b() * 255.0);
					
					final int imageX = (int) (centerX + scaleX * viewX);
					final int imageY = (int) (centerY - scaleY * viewY);
					
					image.setRGB( imageX ,imageY , 0xff000000 | r <<16 | g << 8 | b );
				}
			}
		}
		return image;
	}

    public Vector4 calculateColorAt(final Ray incomingRay,final IntersectionInfo intersection)
    {
        final Vector4 normalAtIntersection = intersection.normalAtIntersection();
        final Vector4 intersectionPoint = intersection.nearestIntersectionPoint;
        
        final Material material = intersection.object.material;
        
        Vector4 sumDiff=new Vector4(0,0,0);
        Vector4 sumSpec=new Vector4(0,0,0);
        
        int lsCount=0;
        for ( Lightsource light : scene.lightsources ) 
        {
            // cast ray from light source to point of intersection
            final Vector4 vectorToIntersection = intersectionPoint.minus( light.position );
        	final Ray rayFromLight = new Ray( light.position , vectorToIntersection );
        	
        	final double solutionAtIntersection = rayFromLight.solutionAt( intersectionPoint ) - 0.001;
        	// check whether ray cast to light source does not pass through any other objects
        	// BEFORE hitting the light
        	IntersectionInfo occluder = scene.hasAnyIntersection( rayFromLight , 0 );
        	if (  occluder == null ||  occluder.solutions[0] > solutionAtIntersection ) 
        	{
        		// calculate diffuse color
                final Vector4 vectorToLight = light.position.minus( intersectionPoint ).normalize();
        		double dotProduct = Math.max( 0 , normalAtIntersection.dotProduct( vectorToLight ) );
        		Vector4 diffuseColor = material.diffuseColor.straightMultiply( light.diffuseColor ).multiply( dotProduct );
        		
        		// specular color
        		Vector4 reflected = Raytracable.reflect( rayFromLight.direction , normalAtIntersection );
        		double eyeReflectionAngle = Math.max( 0 , normalAtIntersection.dotProduct( reflected ) );
        		double fspec = Math.pow( eyeReflectionAngle , material.shininess );
        		
        		Vector4 specularColor = light.specularColor.straightMultiply( material.specularColor ).multiply( fspec );

        		sumDiff = sumDiff.plus( diffuseColor );
        		sumSpec = sumSpec.plus( specularColor );
        		lsCount++;
        	} 
        }
        
        Vector4 finalColor;
        if ( lsCount == 0 ) {
            finalColor = scene.ambientColor;
        } else {
        	finalColor= scene.ambientColor.plus(sumDiff ).plus(sumSpec);
        }

        // handle reflection
        if ( material.reflectivity() != 0 && incomingRay.bounceCount < 2 ) 
        {
        	// calculate reflected ray
        	final Vector4 reflected = Raytracable.reflect( incomingRay.direction , normalAtIntersection );
        	final Ray ray = new Ray( intersectionPoint , reflected , incomingRay.bounceCount+1 );
        	final IntersectionInfo hit = scene.findNearestIntersection( ray , 0.1 );
        	if ( hit != null ) {
        		Vector4 refColor = calculateColorAt( ray , hit );
        		finalColor = finalColor.multiply( 1 - material.reflectivity() ).plus( refColor.multiply( material.reflectivity() ) );
        	}
        }
        return finalColor.clamp(0,1);
    }
}