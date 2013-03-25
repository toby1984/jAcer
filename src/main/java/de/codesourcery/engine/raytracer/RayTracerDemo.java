package de.codesourcery.engine.raytracer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class RayTracerDemo {

	public static void main(String[] args) {

		final Vector4 eyePosition = new Vector4( 0 , 500 , 500 );
		final Vector4 viewDirection = new Vector4( 0 , 0 , -1 );
		final Camera camera = new Camera(eyePosition , viewDirection );

		final Sphere sphere1 = new Sphere( new Vector4( -150 ,0,-100) , 100 );
		final Sphere sphere2 = new Sphere( new Vector4( 250 , 00 , -200) , 100 );		
		final Sphere sphere3 = new Sphere( new Vector4( -100 , -400 , -200) , 100 );	
		
		Plane p1 = new Plane( new Vector4(0,-200,0 ) , new Vector4(0,100,0) );
		Plane p2 = new Plane( new Vector4(0,0,-700 ) , new Vector4(0,0,100) );

		final Scene scene = new Scene(camera);
		scene.addObject( sphere1 );
		scene.addObject( sphere2 );
		scene.addObject( sphere3 );	
		scene.addObject( p1 );
		scene.addObject( p2 );
		
//		scene.addObject( new PointLightsource( new Vector4( 200,250,0 ) , Color.GREEN ) );
		scene.addObject( new PointLightsource( new Vector4( 0,250,0 ) , Color.RED) );
//		scene.addObject( new PointLightsource( new Vector4( -200,0,0 ) , Color.BLUE) );		

		final Raytracer tracer = new Raytracer( scene );

		final JPanel panel = new JPanel() {

			private final Object LOCK = new Object();

			private Thread tracerThread = null;
			private BufferedImage image = null; 

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
								final BufferedImage newImage = tracer.trace( w , h );
								System.out.println("Finished tracing "+w+" x "+h);
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
		};

		panel.setMinimumSize( new Dimension(800,600 ) );
		panel.setPreferredSize( new Dimension(800,600 ) );		

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
	}
}
