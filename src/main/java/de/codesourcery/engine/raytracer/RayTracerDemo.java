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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RayTracerDemo {

	public static void main(String[] args) {

		final Vector4 eyePosition = new Vector4( 0 , -300 , 1350 );
		final Camera camera = new Camera();
		camera.setEyePosition( eyePosition , 0 , 0 );

		final Sphere sphere1 = new Sphere( "sphere #1", new Vector4( -150 ,    0 , -100 ) , 100 );
		final Sphere sphere2 = new Sphere( "sphere #2", new Vector4(  250 ,    -500 , -200 ) , 100 );	
		final Sphere sphere3 = new Sphere( "sphere #3", new Vector4( 50 , -600 , -150 ) , 100 );
		
		Plane p1 = new Plane( "horizontal plane", new Vector4( 0, -600,    0 ) , new Vector4( 0 , 100 ,   0 ) ); // horizontal plane
        Plane p2 = new Plane( "left plane", new Vector4( -300,    0, 0 ) , new Vector4( 100 ,   0 , 0 ) ); // left plane  		
		Plane p3 = new Plane( "vertical plane", new Vector4( 0,    0, -700 ) , new Vector4( 0 ,   0 , 100 ) ); // vertical plane
	      
		final AxisAlignedCube cube1 = new AxisAlignedCube( "vertical plane",  200,200,200 );
		cube1.transformation = LinAlgUtils.translationMatrix( 0 , -500 , -0 ); // .multiply( LinAlgUtils.rotY( 45 ) );
		cube1.material.reflectivity(1.0);
		
	    sphere3.material.reflectivity(0.5);
		
		final Scene scene = new Scene(camera);
		
//		scene.addObject( sphere1 );
//		scene.addObject( sphere2 );
//		scene.addObject( sphere3 );	
		
		scene.addObject( p1 );
		scene.addObject( p2 );		
        scene.addObject( p3 );
        
//        scene.addObject( cube1 );        
		
//		scene.addObject( new PointLightsource( new Vector4( 0,500,500 ) , Color.RED) );
		scene.addObject( new PointLightsource( new Vector4( 0, -400, 0 ) , Color.RED) );
//		scene.addObject( new PointLightsource( new Vector4( -200,0,0 ) , Color.BLUE) );		

		final Raytracer tracer = new Raytracer( scene );

		final RenderPanel panel = new RenderPanel(tracer);

		panel.setMinimumSize( new Dimension(300,300) );
		panel.setPreferredSize( new Dimension(300,300) );		

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
		
		public final Raytracer tracer;
		
		public RenderPanel(Raytracer tracer) {
			this.tracer = tracer;
		}

		private Thread tracerThread = null;
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
		
		public void recalculate() {
			trace(getWidth() , getHeight() );
			repaint();
		}
		
		private void trace(final int w,final int h) 
		{
			synchronized( LOCK ) 
			{
				if ( tracerThread == null || ! tracerThread.isAlive() ) 
				{
					final JPanel outer = this;
					tracerThread = new Thread() 
					{
						public void run() 
						{
							System.out.println("Tracing "+w+" x "+h);	
							long time = -System.currentTimeMillis();
							final BufferedImage newImage = tracer.trace( w , h );
							time += System.currentTimeMillis();
							System.out.println("Finished tracing "+w+" x "+h+" [ "+time+" millis ]");
							SwingUtilities.invokeLater( new Runnable() 
							{
								public void run() 
								{
									synchronized( LOCK ) 
									{
										image = newImage;
									}
									outer.repaint();										
								};
							});
						}
					};

					tracerThread.start();
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
			
			if ( img != null ) 
			{
				g.drawImage( img , 0 , 0 , w , h , null );
				if ( img.getWidth() != w || img.getHeight() != h ) 
				{
					trace(w,h);
				}
			} else {
				trace(w,h);
			}
		}
			
	}
}