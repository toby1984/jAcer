package de.codesourcery.engine.raytracer;

import java.awt.Color;
import java.awt.Graphics;
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

	public static final int SLICE_FACTOR = 8;

	public Scene scene;

	private ThreadPoolExecutor threadpool;

	private volatile int samplesPerPixel = 1;

	private static boolean ENABLE_RAY_DEBUGGING=true;

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
				t.setName("trace-worker");
				t.setDaemon( true );
				return t;
			}
		};
		this.threadpool = new ThreadPoolExecutor(cpuCount, cpuCount, 60, TimeUnit.SECONDS, workQueue, threadFactory, new CallerRunsPolicy() );
	}

	public IntersectionInfo getObjectAt(int imageWidth,int imageHeight,Point point)
	{
		final Vector4 pointOnViewPlane = screenToPointOnViewPlane(imageWidth,imageHeight , point.x ,point.y );

		// cast ray from camera position through view plane
		final Ray ray = new Ray( scene.camera.eyePosition , pointOnViewPlane.minus( scene.camera.eyePosition ).normalize() );

		final Vector4 viewPlaneNormalVector = scene.camera.viewOrientation.flip().normalize();
		final Plane viewPlane = new Plane( "viewPlane", scene.camera.frustum.getNearPlane().pointOnPlane , viewPlaneNormalVector.flip() );     

		final double tStart=viewPlane.intersect( ray ).solutions[0];
		return scene.findNearestIntersection(ray , tStart );	        
	}

	public Vector4 screenToPointOnViewPlane(int imageWidth,int imageHeight,int screenX,int screenY) 
	{
		final int centerX = imageWidth / 2;
		final int centerY = imageHeight / 2;

		final double scaleX = imageWidth / scene.camera.frustum.getNearPlaneWidth();
		final double scaleY = imageHeight / scene.camera.frustum.getNearPlaneHeight();

		double worldX = ( screenX - centerX) / scaleX;
		double worldY = (centerY - screenY ) / scaleY;

		// calculate point on view plane
		final Vector4 xAxis = scene.camera.xAxis.normalize();
		final Vector4 yAxis = scene.camera.yAxis.normalize();

		final Vector4 viewPlaneNormalVector = scene.camera.viewOrientation.flip().normalize();
		final Plane viewPlane = new Plane( "viewPlane", scene.camera.frustum.getNearPlane().pointOnPlane , viewPlaneNormalVector.flip() );          
		final Vector4 pointOnViewPlane = viewPlane.pointOnPlane.plus( xAxis.multiply( worldX ) , yAxis.multiply( worldY) );
		return pointOnViewPlane;
	}

	public synchronized BufferedImage trace(final int imageWidth, final int imageHeight) {

		final BufferedImage image = new BufferedImage(imageWidth, imageHeight , BufferedImage.TYPE_INT_ARGB);

		final double x1 = -scene.camera.frustum.getNearPlaneWidth()*0.5;
		final double y1 = scene.camera.frustum.getNearPlaneHeight()*0.5; 

		final int cpus = Runtime.getRuntime().availableProcessors()*SLICE_FACTOR;     

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

		final Plane viewPlane = getViewPlane();
		final int sampleCount = this.samplesPerPixel;
		final double factor = 1.0/sampleCount;

		final MersenneTwisterFast twister = rnd.get();
		for ( double x = x1 ; x < x2 ; x += stepX ) 
		{
			for ( double y = y1 ; y > y2 ; y -= stepY )
			{
				// calculate point on view plane
				Vector4 color = new Vector4(0,0,0);
				for ( int i = 0 ; i < sampleCount ; i++ ) 
				{
					double viewX = x+twister.nextDouble()*stepX;
					double viewY = y-twister.nextDouble()*stepY;

					final Vector4 pointOnViewPlane = viewPlane.pointOnPlane.plus( xAxis.multiply( viewX ) , yAxis.multiply( viewY ) );
					tracePrimaryRay(viewPlane, color, pointOnViewPlane,image,false);
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

	public Plane getViewPlane() 
	{
		final Vector4 viewPlaneNormalVector = scene.camera.viewOrientation.flip().normalize();

		// hint: code assumes that 'pointOnPlane' is the center of the view plane
		return new Plane( "viewPlane",scene.camera.frustum.getNearPlane().pointOnPlane , viewPlaneNormalVector.flip() );
	}

	public void tracePrimaryRay(final Plane viewPlane, Vector4 color, final Vector4 pointOnViewPlane,BufferedImage image,boolean debug)
	{
		// cast ray from camera position through view plane
		final Ray ray = new Ray( scene.camera.eyePosition , pointOnViewPlane.minus( scene.camera.eyePosition ).normalize() );
		final double tStart  = viewPlane.intersect( ray ).solutions[0];

		// find nearest intersection that is BEHIND the view plane
		final IntersectionInfo intersection = scene.findNearestIntersection(ray , tStart );
		if ( intersection != null ) 
		{
			if ( ENABLE_RAY_DEBUGGING && debug ) {
				ray.debug = true;
			}            
			color.plusInPlace( calculateColorAt(ray,intersection,image , 1.0 ) );
		}
	}

	protected static enum RayType {
		RAY_TO_LIGHT,
		REFLECTED,
		NORMAL;
	}

	private void renderRay(BufferedImage image,Plane viewPlane,Vector4 p1,Vector4 p2,RayType type) 
	{
		final Graphics graphics = image.getGraphics();

		Point viewP1 = modelToScreen( p1 , viewPlane , image.getWidth() , image.getHeight() );
		Point viewP2 = modelToScreen( p2 , viewPlane , image.getWidth() , image.getHeight() );

		final Color color;
		switch( type ) 
		{   
			case RAY_TO_LIGHT:
				color=Color.WHITE;
				break;
			case REFLECTED:
				color=Color.BLUE;
				break;          
			case NORMAL:
				color=Color.GREEN;
				break;                  
			default:
				throw new RuntimeException("Unknown ray type: "+type);
		}
		graphics.setColor( color );
		graphics.drawLine(viewP1.x , viewP1.y , viewP2.x , viewP2.y );
	}

	public Point modelToScreen(Vector4 pointInWorldCoordinates,Plane viewPlane,int imageWidth,int imageHeight) 
	{
		System.out.println("Mapping "+pointInWorldCoordinates);

		// cast ray from point to camera
		final Vector4 direction = scene.camera.eyePosition.minus(pointInWorldCoordinates).normalize();
		final Ray r = new Ray( pointInWorldCoordinates , direction );

		// calculate intersection with view plane
		final IntersectionInfo i1= viewPlane.intersect( r );
		final Vector4 nearestIntersectionPoint = r.evaluateAt( i1.solutions[0] );
		System.out.println("Intersection with view plane "+nearestIntersectionPoint);

		// map point on view plane to screen coordinates

		// calculate view plane rotation relative to 
		// default view plane in untransformed coordinates with origin (0,0,0) and normal vector (0,0,1)

		Matrix invMatrix = LinAlgUtils.rotY( 360 - scene.camera.rotAngleY ).multiply( LinAlgUtils.rotX( scene.camera.rotAngleX ) );

		Vector4 pointOnViewPlane = nearestIntersectionPoint.minus(scene.camera.eyePosition ).multiply( invMatrix );
		System.out.println("Transformed point on view plane "+pointOnViewPlane);

		double nw = scene.camera.frustum.getNearPlaneWidth();
		double nh = scene.camera.frustum.getNearPlaneHeight();

		double scaleX = imageWidth / nw;
		double scaleY = imageHeight / nh;

		final int centerX = imageWidth / 2;
		final int centerY = imageHeight / 2;

		final double x = centerX - pointOnViewPlane.x*scaleX;
		final double y = centerY - pointOnViewPlane.y*scaleY;

		final Point result = new Point((int) Math.round(x),(int) Math.round(y));
		System.out.println("Mapped "+pointInWorldCoordinates+" -> ("+x+" , "+y+")");
		return result;
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

	private final ThreadLocal<MersenneTwisterFast> rnd = new ThreadLocal<MersenneTwisterFast>()  {
		protected MersenneTwisterFast initialValue() 
		{
			return new MersenneTwisterFast(System.currentTimeMillis());
		}
	};

	private Vector4 calculateColorAt(final Ray incomingRay,final IntersectionInfo intersection,BufferedImage image,double refr_indx )
	{
		final Vector4 normalAtIntersection = intersection.normalAtIntersection(scene.camera);
		final Vector4 intersectionPoint = intersection.nearestIntersectionPoint;

		final Material material = intersection.object.material;

		if ( ENABLE_RAY_DEBUGGING && incomingRay.debug ) 
		{
			renderRay(image, getViewPlane(), intersectionPoint, intersectionPoint.plus( normalAtIntersection.multiply( 100 ) ) , RayType.NORMAL);                    
		} 

		/*
		 * REFRACTION
		 */
		Vector4 finalColor = new Vector4(scene.ambientColor);
		Vector4 sumDiff=new Vector4();
		Vector4 sumSpec=new Vector4();
		
		if ( material.isRefractive ) 
		{
			double refraction_index = material.refractionIndex;
		    double n = refr_indx/refraction_index;
			Vector4 N1 = normalAtIntersection.multiply( incomingRay.fromInsideObject ? 1 : -1 ); // N.multiply(value)*HitOrMiss;
			double CosThetaI = N1.flip().dotProduct( incomingRay.direction);
			double SinThetaI = Math.sqrt(1.0 - CosThetaI*CosThetaI);
			double SinThetaT = n*SinThetaI;
			if(SinThetaT*SinThetaT < 1.0)
			{
				double CosThetaT = Math.sqrt(1.0 - SinThetaT*SinThetaT);
				
				Vector4 R4 = incomingRay.direction.multiply( n ).minus( N1.multiply( n*CosThetaI+CosThetaT) );
				R4.normalizeInPlace();
				
				double dist1;
				Vector4 R5 = intersectionPoint.plus(  R4.multiply(0.001) );
				Ray R6 = new Ray( R5,R4, incomingRay.bounceCount+1 );
				R6.fromInsideObject = ! incomingRay.fromInsideObject;
				
				IntersectionInfo info = scene.findNearestIntersection( R6 , 0.01 );
				final Vector4 refr_color;
				if ( info != null ) 
				{
					dist1 = info.solutions[0];
					refr_color = calculateColorAt( R6 , info , image , refraction_index );
				} else {
					dist1 = 0;
					refr_color = new Vector4();					
				}
				
				// Beer's Law
				Vector4 initialCol = new Vector4();
//				calculateReflectedColor(incomingRay, image, normalAtIntersection, intersectionPoint, material, initialCol);
				
				//      Color absorbance = object->GetMaterial()->GetColor()*0.15*dist1*(-1.0);
				Vector4 absorbance = initialCol.multiply( 0.15*dist1*(-1.0) );
				Vector4 transparency = new Vector4 ( Math.exp(absorbance.x), Math.exp(absorbance.y), Math.exp(absorbance.z) );
				finalColor.plusInPlace( refr_color.straightMultiply( transparency ) );
				return finalColor;
			}			
		} else {
			/*
			 * DIRECT LIGHTING
			 */
			calcDiffuseAndSpecular(incomingRay, intersection, intersectionPoint, normalAtIntersection, material, image, sumDiff, sumSpec);
			finalColor.plusInPlace( sumDiff , sumSpec);
		}

		/*
		 * REFLECTION
		 */
		if ( material.reflectivity() != 0.0d ) 
		{
			calculateReflectedColor(incomingRay, image, normalAtIntersection, intersectionPoint, material, finalColor); 
		}
		return finalColor.clamp(0,1);
	}

	private void calculateReflectedColor(final Ray incomingRay, BufferedImage image,
			final Vector4 normalAtIntersection,
			final Vector4 intersectionPoint, final Material material,
			Vector4 finalColor) 
	{
		if ( incomingRay.bounceCount > 6 ) {
			return;
		}
		
		// calculate reflected ray
		final Vector4 reflected = Raytracable.reflect( incomingRay.direction , normalAtIntersection );
		// hint: reflect() already returns a normalized vector so need to normalize it here
		final Ray ray = new Ray( intersectionPoint , reflected , incomingRay.bounceCount+1 );
		if ( ENABLE_RAY_DEBUGGING && incomingRay.debug ) {
			ray.debug = true;
		}
		final IntersectionInfo hit = scene.findNearestIntersection( ray , 0.1 );
		if ( hit != null ) 
		{
			if ( ENABLE_RAY_DEBUGGING && incomingRay.debug ) {
				renderRay( image , getViewPlane() , ray.point , ray.evaluateAt( hit.solutions[0] ) , RayType.REFLECTED );
			}
			Vector4 refColor = calculateColorAt( ray , hit , image , 1.0 );
			finalColor.multiplyAddInPlace( 1 - material.reflectivity() , refColor ) ;
		}
	}
	
	private void calcDiffuseAndSpecular(Ray incomingRay,IntersectionInfo intersection,
			Vector4 intersectionPoint, 
			Vector4 normalAtIntersection,
			Material material,
			BufferedImage image,
			Vector4 sumDiff,
			Vector4 sumSpec) 
	{
		for ( Lightsource light : scene.lightsources ) 
		{
			// cast ray from light source to point of intersection
			final Vector4 vectorToIntersection = intersectionPoint.minus( light.position );
			final Ray rayFromLight = new Ray( light.position , vectorToIntersection.normalize() );

			final double solutionAtIntersection = rayFromLight.solutionAt( intersectionPoint ) - 0.001;

			final IntersectionInfo occluder = scene.hasAnyIntersection( rayFromLight , 0.001 );
			// TODO: Handle rays passing through transparent materials
			if (  occluder == null ||  occluder.solutions[0] > solutionAtIntersection ) // no occlusion or hit is behind intersection point
			{
				// calculate diffuse color
				final Vector4 vectorToLight = light.position.minus( intersectionPoint ).normalize();

				double dotProduct = normalAtIntersection.dotProduct( vectorToLight );
				if ( dotProduct > 0 ) 
				{
					if ( ENABLE_RAY_DEBUGGING && incomingRay.debug ) 
					{
						renderRay(image, getViewPlane(), intersectionPoint, light.position , RayType.RAY_TO_LIGHT);
					}                    
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
		}

		if ( material.texture != null ) 
		{
			sumDiff.copyFrom( intersection.object.sampleTextureColorAtPoint( intersectionPoint ).straightMultiply( sumDiff ) );
			sumSpec.setToZero();
		} else {
			sumDiff.copyFrom( sumDiff.multiply( 1 - material.reflectivity() ) );
		}		
	}

	public void setSamplesPerPixel(int samplesPerPixel) {
		this.samplesPerPixel = samplesPerPixel;
	}

	public int getSamplesPerPixel() {
		return samplesPerPixel;
	}
}