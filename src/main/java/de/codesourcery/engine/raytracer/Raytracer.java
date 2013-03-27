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

public final class Raytracer 
{
    private static final boolean multiThreaded = true;
    
	public Scene scene;
	
	private ThreadPoolExecutor threadpool;
	
	private static final int SAMPLES_PER_PIXEL = 4;
	
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
            final Ray ray = new Ray( scene.camera.eyePosition , pointOnViewPlane.minus( scene.camera.eyePosition ).normalize() );
            
            // TODO: Performance - intersection ray <-> plane could use pre-calculated values !!
            final double tStart=viewPlane.intersect( ray ).solutions[0];
            return scene.findNearestIntersection(ray , tStart );	        
	}
	
	public synchronized BufferedImage trace(final int imageWidth, final int imageHeight) {
		
		final BufferedImage image = new BufferedImage(imageWidth, imageHeight , BufferedImage.TYPE_INT_ARGB);
        System.out.println( scene.camera );
		
		final double x1 = -scene.camera.frustum.getNearPlaneWidth()*0.5;
        final double y1 = scene.camera.frustum.getNearPlaneHeight()*0.5; 
        
        final int cpus = Runtime.getRuntime().availableProcessors();     
        
        final double sliceWidth = scene.camera.frustum.getNearPlaneWidth() / cpus;
        final double sliceHeight = scene.camera.frustum.getNearPlaneHeight() / cpus;        
		
        final double stepX = scene.camera.frustum.getNearPlaneWidth() / (imageWidth+1);
        final double stepY = scene.camera.frustum.getNearPlaneHeight() / (imageHeight+1);        
        
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
		}
		
        final CountDownLatch latch = new CountDownLatch( slices.size() );		
		for ( final Slice slice : slices ) 
		{
			final Runnable r =  new Runnable() {

                @Override
                public void run()
                {
                    try {
                        trace(slice, imageWidth,imageHeight );
                    } finally {
                        latch.countDown();
                    }
                }
            };
            
            if ( multiThreaded ) {
            	threadpool.execute( r );
            } else {
            	r.run();
            }
		}
		
		try {
            latch.await();
        } 
		catch (InterruptedException e) {
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
        final double factor = 1.0/SAMPLES_PER_PIXEL;
        
        for ( double x = x1 ; x < x2 ; x += stepX ) 
        {
            for ( double y = y1 ; y > y2 ; y -= stepY )
            {
                // calculate point on view plane
                Vector4 color = new Vector4(0,0,0);
                for ( int i = 0 ; i < SAMPLES_PER_PIXEL ; i++ ) 
                {
                    double viewX = x+rnd.get().nextDouble()*stepX;
                    double viewY = y-rnd.get().nextDouble()*stepY;
                    
                    final Vector4 pointOnViewPlane = viewPlane.pointOnPlane.plus( xAxis.multiply( viewX ) , yAxis.multiply( viewY ) );
                    
                    // cast ray from camera position through view plane
                    final Ray ray = new Ray( scene.camera.eyePosition , pointOnViewPlane.minus( scene.camera.eyePosition ).normalize() );
                    final double tStart  = viewPlane.intersect( ray ).solutions[0];
                    
                    // find nearest intersection that is BEHIND the view plane
                    final IntersectionInfo intersection = scene.findNearestIntersection(ray , tStart );
                    if ( intersection != null ) 
                    {
                        color.plusInPlace( calculateColorAt(ray,intersection) );
                    }
                }

                final int r = (int) (color.r() * factor * 255.0);
                final int g = (int) (color.g() * factor * 255.0);
                final int b = (int) (color.b() * factor * 255.0);
                
                final int imageX = (int) (centerX + scaleX * x);
                final int imageY = (int) (centerY - scaleY * y);
                
                synchronized(image) 
                {
                    image.setRGB( imageX ,imageY , 0xff000000 | r <<16 | g << 8 | b );
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
	
	private final ThreadLocal<MersenneTwisterFast> rnd = new ThreadLocal<MersenneTwisterFast>() 
			{
		protected MersenneTwisterFast initialValue() 
		{
			return new MersenneTwisterFast(System.currentTimeMillis());
		};
	};

    private Vector4 calculateColorAt(final Ray incomingRay,final IntersectionInfo intersection)
    {
        final Vector4 normalAtIntersection = intersection.normalAtIntersection();
        final Vector4 intersectionPoint = intersection.nearestIntersectionPoint;
        
        final Material material = intersection.object.material;
        
        Vector4 sumDiff=new Vector4(0,0,0);
        Vector4 sumSpec=new Vector4(0,0,0);
        
        for ( Lightsource light : scene.lightsources ) 
        {
            // cast ray from light source to point of intersection
            final Vector4 vectorToIntersection = intersectionPoint.minus( light.position );
        	final Ray rayFromLight = new Ray( light.position , vectorToIntersection.normalize() );
        	
        	final double solutionAtIntersection = rayFromLight.solutionAt( intersectionPoint ) - 0.001;
        	
        	final IntersectionInfo occluder = scene.hasAnyIntersection( rayFromLight , 0.001 );
        	if (  occluder == null ||  occluder.solutions[0] > solutionAtIntersection ) // no occlusion or hit is behind intersection point
        	{
        		// calculate diffuse color
                final Vector4 vectorToLight = light.position.minus( intersectionPoint ).normalize();
        		double dotProduct = Math.max( 0 , normalAtIntersection.dotProduct( vectorToLight ) );
        		
        		sumDiff.x += (material.diffuseColor.x * light.diffuseColor.x)*dotProduct;
        		sumDiff.y += (material.diffuseColor.y * light.diffuseColor.y)*dotProduct;
        		sumDiff.z += (material.diffuseColor.z * light.diffuseColor.z)*dotProduct;
                
        		// specular color
        		if ( material.reflectivity() == 0 ) 
        		{
        		    // hint: reflect() returns a normalized vector  
        		    Vector4 reflected = Raytracable.reflect( rayFromLight.direction , normalAtIntersection );
        		    double eyeReflectionAngle = Math.max( 0 , normalAtIntersection.dotProduct( reflected ) );
        		    double fspec = Math.pow( eyeReflectionAngle , material.shininess );
        		
                    sumSpec.x += (material.specularColor.x * light.specularColor.x)*fspec;
                    sumSpec.y += (material.specularColor.y * light.specularColor.y)*fspec;
                    sumSpec.z += (material.specularColor.z * light.specularColor.z)*fspec;
        		}
        	} 
        }
        
        if ( material.texture != null ) 
        {
             final double weight = 1; //  sumDiff.clamp(0,1).length() / 1.73205080757d; //  1.73205080757d = Math.sqrt( 1^2 + 1^2 + 1^2 )
//            double weight = 1.0;
//            return intersection.object.getColorAtPoint( intersectionPoint );
            sumDiff = intersection.object.getColorAtPoint( intersectionPoint ).multiply( weight );
            sumSpec = new Vector4(0,0,0);
        } else {
            sumDiff = sumDiff.multiply( 1 - material.reflectivity() );
        }
        
        Vector4 finalColor = scene.ambientColor.plus( sumDiff , sumSpec);
        
//        if ( material.reflectivity() < 1 && incomingRay.bounceCount < 4 ) 
//        {
//            Vector4 global=new Vector4(0,0,0);
//            int hitCount = 0;
//        	for ( int i = 0 ;i < 8 ; i++ ) 
//        	{
//        		double rx = -1 + rnd.get().nextDouble()*2.0;
//        		double ry = -1 + rnd.get().nextDouble()*2.0;
//        		double rz = -1 + rnd.get().nextDouble()*2.0;
//        		final Vector4 newDir = new Vector4(rx,ry,rz).normalize();
//        		Ray secondaryRay = new Ray( intersectionPoint , newDir , incomingRay.bounceCount+1 );
//        		IntersectionInfo hit = scene.findNearestIntersection( secondaryRay , 0.01 );
//        		if ( hit != null && hit.object != intersection.object ) 
//        		{
//        		    hitCount++;
//        		    global.plusInPlace( calculateColorAt( secondaryRay , hit ) );
//        		}
//        	}
//        	if ( hitCount > 0 ) {
//        	    finalColor.plusInPlace( global.multiply( 1/hitCount).multiply( 1 / (1+incomingRay.bounceCount ) ) );
//        	}
//        }

        // handle reflection
        if ( material.reflectivity() != 0.0d && incomingRay.bounceCount < 4 ) 
        {
        	// calculate reflected ray
        	final Vector4 reflected = Raytracable.reflect( incomingRay.direction , normalAtIntersection );
        	// hint: reflect() already returns a normalized vector so need to normalize it here
        	final Ray ray = new Ray( intersectionPoint , reflected , incomingRay.bounceCount+1 );
        	final IntersectionInfo hit = scene.findNearestIntersection( ray , 0.1 );
        	if ( hit != null ) 
        	{
        		Vector4 refColor = calculateColorAt( ray , hit );
        		finalColor = finalColor.multiplyAdd( 1 - material.reflectivity() , refColor );
        	} 
        }
        return finalColor.clamp(0,1);
    }
}