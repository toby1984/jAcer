package de.codesourcery.engine.raytracer;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

public class Raytracer {

	public Scene scene;
	
	private final int LINES_X = 800;
	private final int LINES_Y = 800;
	
	private ThreadPoolExecutor threadpool;
	
	public Raytracer(Scene scene) 
	{
		this.scene = scene;
		final int cpuCount = Runtime.getRuntime().availableProcessors()+1;
		
		final BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(2*cpuCount*cpuCount);
		
        final ThreadFactory threadFactory = new ThreadFactory() {
            
            @Override
            public Thread newThread(Runnable r)
            {
                final Thread t = new Thread(r);
                t.setDaemon( true );
                return t;
            }
        };
        this.threadpool = new ThreadPoolExecutor(cpuCount, cpuCount, 60, TimeUnit.SECONDS, workQueue, threadFactory, new CallerRunsPolicy() );
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
	
	public synchronized BufferedImage trace(final int imageWidth, final int imageHeight) {
		
		final BufferedImage image = new BufferedImage(imageWidth, imageHeight , BufferedImage.TYPE_INT_ARGB);
        System.out.println( scene.camera );
		
		final double x1 = -scene.camera.frustum.getNearPlaneWidth()*0.5;
        final double y1 = scene.camera.frustum.getNearPlaneHeight()*0.5; 
        
        final int cpus = 3; // Runtime.getRuntime().availableProcessors();     
        
        final double sliceWidth = scene.camera.frustum.getNearPlaneWidth() / cpus;
        final double sliceHeight = scene.camera.frustum.getNearPlaneHeight() / cpus;        
		
        final double stepX = scene.camera.frustum.getNearPlaneWidth() / LINES_X;
        final double stepY = scene.camera.frustum.getNearPlaneHeight() / LINES_Y;        
        
        final List<Slice> slices = new ArrayList<>();
		for ( int x = 0 ; x < cpus ; x++ )  
		{
			for ( int y = 0 ; y < cpus ; y++ ) 
			{
			    final double sliceX1 = x1+x*sliceWidth;
			    final double sliceY1 = y1-y*sliceHeight;
                
			    final double sliceX2 = sliceX1+sliceWidth;
                final double sliceY2 = sliceY1-sliceHeight;
                
                Slice slice = new Slice(sliceX1 , sliceX2 , sliceY1 , sliceY2 , image ,stepX , stepY );
			    slices.add(  slice );
			}
			System.out.println();
		}
		
        final CountDownLatch latch = new CountDownLatch( slices.size() );		
		for ( final Slice slice : slices ) 
		{
            threadpool.execute( new Runnable() {

                @Override
                public void run()
                {
                    try {
                        trace(slice, imageWidth,imageHeight );
                    } finally {
                        latch.countDown();
                    }
                }
            });
		}
		
		try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread();
        }
		return image;
	}
	
	protected void trace(Slice slice,int imageWidth,int imageHeight) 
	{
	    final double x1 = slice.x1;
	    final double y1 = slice.y1;
	    
        final double x2 = slice.x2;
        final double y2 = slice.y2;	
        
        final double stepX = slice.stepX;
        final double stepY = slice.stepY;
        
        final double scaleX = imageWidth / scene.camera.frustum.getNearPlaneWidth();
        final double scaleY = imageHeight / scene.camera.frustum.getNearPlaneHeight();        
        
        final int centerX = imageWidth / 2;
        final int centerY = imageHeight / 2;       
        
        final BufferedImage image = slice.image;
        
        final Vector4 xAxis = scene.camera.xAxis.normalize();
        final Vector4 yAxis = scene.camera.yAxis.normalize();        
        
        final Vector4 viewPlaneNormalVector = scene.camera.viewOrientation.flip().normalize();
        
        // hint: code assumes that 'pointOnPlane' is the center of the view plane
        final Plane viewPlane = new Plane( "viewPlane",scene.camera.frustum.getNearPlane().pointOnPlane , viewPlaneNormalVector.flip() );        
	    
        for ( double viewX = x1 ; viewX < x2 ; viewX += stepX ) 
        {
            for ( double viewY = y1 ; viewY > y2 ; viewY -= stepY )
            {
                // calculate point on view plane
                final Vector4 pointOnViewPlane = viewPlane.pointOnPlane.plus( xAxis.multiply( viewX ) ).plus( yAxis.multiply( viewY ) );
                
                // cast ray from camera position through view plane
                final Ray ray = new Ray( scene.camera.eyePosition , pointOnViewPlane.minus( scene.camera.eyePosition ) );
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
                    
                    synchronized(image) 
                    {
                        image.setRGB( imageX ,imageY , 0xff000000 | r <<16 | g << 8 | b );
                    }
                }
            }
        }	    
	}
	
	protected static final class Slice {
	    
	    public final double x1;
	    public final double x2;
	    
	    public final double y1;
	    public final double y2;

	    public final double stepX;
	    public final double stepY;
	    
	    public final BufferedImage image;

        public Slice(double x1, double x2, double y1, double y2, BufferedImage image,double stepX,double stepY)
        {
            this.x1 = x1;
            this.x2 = x2;
            
            this.y1 = y1;
            this.y2 = y2;

            this.stepX = stepX;
            this.stepY = stepY;
            
            this.image = image;            
        }

        @Override
        public String toString()
        {
            return "Slice [x1=" + x1 + ", x2=" + x2 + ", y1=" + y1 + ", y2=" + y2 + ", stepX=" + stepX + ", stepY="
                    + stepY + "]";
        }
        
        
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
        if ( material.reflectivity() != 0.0d && incomingRay.bounceCount < 4 ) 
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