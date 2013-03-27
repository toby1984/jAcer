package de.codesourcery.engine.raytracer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RayTracerDemo {

    private static final Dimension IMAGE_SIZE = new Dimension(300,300);
    
	public static void main(String[] args) throws Exception {

		final Vector4 eyePosition = new Vector4( -50.0,-250.0,1900.0 );
		final Camera camera = new Camera();
		camera.setEyePosition( eyePosition , 0 , 0 );

		final Sphere sphere1 = new Sphere( "sphere #1", new Vector4( -150 ,    0 , -100 ) , 100 );
		final Sphere sphere2 = new Sphere( "sphere #2", new Vector4(  250 ,    -500 , -200 ) , 100 );	
		final Sphere sphere3 = new Sphere( "sphere #3", new Vector4( 50 , -600 , -150 ) , 100 );

//		Plane p1 = new Plane( "horizontal plane", new Vector4( 0, -600,    0 ) , new Vector4( 0 , 100 ,   0 ) ); // horizontal plane
		Plane p1 = new Plane( "horizontal plane", new Vector4( 0, -600,    0 ) , new Transformation(AffineTransform.rotate(180, 0, 0)) ); // horizontal plane
		Plane p4 = new Plane( "vertical plane", new Vector4( 0,    0, -700 ) , new Transformation() ); // vertical plane
		
//		p4.material.diffuseColor = new Vector4(0,0,0.2);
		p1.material.texture = Texture.load( new File("/home/tobi/workspace/raytracer/src/main/resources/checkers.png" ) );
		
		final AxisAlignedCube cube1 = new AxisAlignedCube( "vertical plane",  new Vector4( 500 , -500 , 0 ) , 200,200,200 );
//		cube1.material.reflectivity(1.0);
		sphere2.material.reflectivity(1.0);

		Plane p3 = new Plane( "left plane", new Vector4( -300,    0, 0 ) , new Vector4( 100 ,   0 , 0 ) ); // left plane		

		final Scene scene = new Scene(camera);
		scene.addObject( sphere1 );
		scene.addObject( sphere2 );
		scene.addObject( sphere3 );	
		scene.addObject( p1 );
		scene.addObject( p3 );		
		scene.addObject( p4 );

		//        scene.addObject( cube1 );        

		//		scene.addObject( new PointLightsource( new Vector4( 0,500,500 ) , Color.RED) );
		scene.addObject( new PointLightsource( new Vector4( 400, 400, 500 ) , Color.RED) );
		//		scene.addObject( new PointLightsource( new Vector4( -200,0,0 ) , Color.BLUE) );		

		final Raytracer tracer = new Raytracer( scene );

		final RenderPanel panel = new RenderPanel(tracer);

		panel.setMinimumSize( IMAGE_SIZE );
		panel.setPreferredSize( IMAGE_SIZE );		

		final JFrame frame = new JFrame("Tracer V0.1");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE );
		frame.getContentPane().setLayout( new GridBagLayout() );

		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.BOTH;
		constraints.gridheight=GridBagConstraints.REMAINDER;
		constraints.gridwidth=GridBagConstraints.REMAINDER;
		constraints.weightx=1.0;
		constraints.weighty=1.0;

		frame.getContentPane().add( panel , constraints );
		frame.pack();
		frame.setVisible( true );
		frame.repaint();

		final float increment = 50;

		frame.addKeyListener( new KeyAdapter() 
		{
			@Override
			public void keyPressed(KeyEvent e) 
			{
				switch(e.getKeyCode() ) 
				{
					case KeyEvent.VK_SPACE:
						if ( tracer.getSamplesPerPixel() == 1 ) {
							tracer.setSamplesPerPixel(16); 
						} else {
							tracer.setSamplesPerPixel(1);
						}
						break;
					case KeyEvent.VK_PLUS:
						camera.moveUp( increment );
						break;
					case KeyEvent.VK_MINUS:
						camera.moveDown( increment );
						break;
					case KeyEvent.VK_W:
						camera.moveForward(increment);
						break;
					case KeyEvent.VK_S:
						camera.moveBackward(increment);
						break;
					case KeyEvent.VK_A:
						camera.strafeLeft(increment);
						break;
					case KeyEvent.VK_D:
						camera.strafeRight(increment);
						break;
					case KeyEvent.VK_Q:
						camera.rotate(-1,0);
						break;
					case KeyEvent.VK_E:
						camera.rotate(1,0);
						break;	
					case KeyEvent.VK_Y:
						camera.rotate(0,-1);
						break;
					case KeyEvent.VK_C:
						camera.rotate(0,1);
						break;	
					case KeyEvent.VK_HOME:
						try 
						{
							final File file = new File("/tmp/trace.png");
							panel.saveImage( file );
							System.out.println("Image saved to "+file.getAbsolutePath());
						} 
						catch (IOException e1) 
						{
							System.err.println("Saving image failed");
						}
						break;
					default:
						return;
				}
				System.out.println( tracer.scene.camera );
				panel.recalculate();				
			}
		});
	}

	protected static final class RenderPanel extends JPanel {

		private final Object LOCK = new Object();
		private final AtomicBoolean isCalculating = new AtomicBoolean(false);

		public final Raytracer tracer;

		public RenderPanel(Raytracer tracer) {
			this.tracer = tracer;
		}

		private BufferedImage image = null; 

		{
			addMouseListener( new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if ( e.getButton() == MouseEvent.BUTTON1 ) 
					{
						Point point = e.getPoint();
						System.out.println("Mouse clicked at: "+point);
						System.out.print("Point on view plane: ");
						System.out.println("CLICKED: "+tracer.getObjectAt( getWidth() , getHeight() , point));
					}
				}
			});			    
		}

		public void saveImage(File file) throws IOException 
		{
			BufferedImage tmp;
			do {
				synchronized( LOCK ) 
				{
					tmp = image;
				}

				if ( tmp != null ) {
					break;
				}

				trace( getWidth() , getHeight() , true );

				synchronized( LOCK ) 
				{
					tmp = image;
				}
				if ( tmp == null ) {
					System.err.println("No image ???");
					return;
				}				
			} while( true );

			ImageIO.write( tmp , "PNG" , file );
		}

		public void recalculate() 
		{
			trace(getWidth() , getHeight() , false );
		}

		private void trace(final int w,final int h,boolean waitForCompletion) 
		{
			if ( isCalculating.compareAndSet( false, true ) )
			{
				final CountDownLatch latch = new CountDownLatch(1);
				final Thread tracerThread = new Thread() 
				{
					public void run() 
					{
						System.out.println("Tracing "+w+" x "+h);	
						long time = -System.currentTimeMillis();
						try 
						{
							final BufferedImage newImage = tracer.trace( w , h );
							time += System.currentTimeMillis();
							synchronized( LOCK ) 
							{
								image = newImage;
							}

							SwingUtilities.invokeAndWait( new Runnable() 
							{
								public void run() 
								{
									RenderPanel.this.repaint();										
								}
							}
									);								
						} catch (Exception e) {
							e.printStackTrace();
						} 
						finally 
						{
							System.out.println("Finished tracing "+w+" x "+h+" [ "+time+" millis ]");
							latch.countDown();
							isCalculating.set( false );
						}
					}
				};
				tracerThread.start();

				if ( waitForCompletion ) 
				{
					try 
					{
						latch.await();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}					
			}
		}
		@Override
		public void paint(Graphics g) 
		{
			super.paint(g);

			final int w = getWidth();
			final int h = getHeight();

			final BufferedImage img;
			synchronized( LOCK ) 
			{
				img = image;
			}

			if ( img == null || img.getWidth() != w || img.getHeight() != h ) 
			{
				trace(w,h,false);
			} 
			else 
			{
				g.drawImage( img , 0 , 0 , w , h , null );
			}
		}

	}
}