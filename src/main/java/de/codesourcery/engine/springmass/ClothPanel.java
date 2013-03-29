package de.codesourcery.engine.springmass;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import de.codesourcery.engine.raytracer.Vector4;

public class ClothPanel extends JFrame {

	private static final int X_RESOLUTION = 300;
	private static final int Y_RESOLUTION = 300;
	
	public static final int DELAY_MILLIS = 100;
	
	private final SimulationThread simulationThread;
	private final RenderPanel renderPanel;
	
	public static void main(String[] args) 
	{
		new ClothPanel();
	}
	
	public ClothPanel() 
	{
		// setup mass system
		final SpringMassSystem system = new SpringMassSystem();
		
		final double xCenter = X_RESOLUTION / 2.0;
		final double yCenter = Y_RESOLUTION / 2.0;
		
		final Mass m1 = new Mass( new Vector4(xCenter-(xCenter/2) , yCenter-(yCenter/2) , 0) );
		final Mass m2 = new Mass( new Vector4(xCenter+(xCenter/2) , yCenter-(yCenter/2) , 0) );
		
		final Mass m3 = new Mass( new Vector4(xCenter-(xCenter/2) , yCenter+(yCenter/2) , 0) );
		final Mass m4 = new Mass( new Vector4(xCenter+(xCenter/2) , yCenter+(yCenter/2) , 0) );		
		
		system.addMass( m1 );
		system.addMass( m2 );
		system.addMass( m3 );
		system.addMass( m4 );
		
		final int restLength = 100;

		// outer shape
		system.addSpring( new Spring(m1,m2 , restLength) );
		system.addSpring( new Spring(m1,m3 , restLength) );
		system.addSpring( new Spring(m2,m4 , restLength) );
		system.addSpring( new Spring(m3,m4 , restLength) );
		
		// diagonals
		system.addSpring( new Spring(m1,m4 , Math.sqrt(restLength*restLength) ) );
		system.addSpring( new Spring(m2,m3 , Math.sqrt(restLength*restLength) ) );

		 simulationThread = new SimulationThread(system);
		 simulationThread.start();
		 
		// setup panel
		renderPanel = new RenderPanel(system);
		renderPanel.setMinimumSize( new Dimension(400,200 ) );
		renderPanel.setPreferredSize( new Dimension(400,200 ) );
		getContentPane().add( renderPanel );
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible( true );
	}
	
	protected final class SimulationThread extends Thread {
		
		private final Object LOCK = new Object();
		private final SpringMassSystem system;
		
		private boolean runSimulation = false;
		
		public SimulationThread(SpringMassSystem system) {
			setDaemon(true);
			this.system = system;
		}
		
		public void toggle() 
		{
			synchronized( LOCK ) 
			{
				if ( runSimulation ) 
				{
					System.out.println("Stopping simlation");
					runSimulation = false;
				} 
				else 
				{
					System.out.println("Starting simlation");
					runSimulation = true;
					LOCK.notifyAll();
				}
			}
		}
		
		@Override
		public void run() 
		{
			while(true) 
			{
				synchronized (LOCK) 
				{
					while( ! runSimulation ) 
					{
						try {
							System.out.println("Simulation thread sleeping");
							LOCK.wait();
							System.out.println("Simulation thread woke up");
						} catch (InterruptedException e) {
						}
					}
				}
				
				System.out.println("Step");
				synchronized( system ) 
				{
					system.step();
				}
				
				renderPanel.repaint();
				
				try {
					Thread.sleep(DELAY_MILLIS);
				} 
				catch (InterruptedException e) {
				}
			}
		}
	}
	
	protected final class RenderPanel extends JPanel {
		
		private final SpringMassSystem system;
		
		private Mass selected;
		
		private final MouseAdapter mouseAdapter = new MouseAdapter() 
		{
			public void mousePressed(java.awt.event.MouseEvent e) 
			{
				if ( e.getButton() == MouseEvent.BUTTON1 ) 
				{ 
					// left click
					selected = system.getNearest( viewToModel( e.getX() , e.getY() ) , 5*5 );
					System.out.println("Selected: "+selected);
					if ( selected != null ) 
					{
						RenderPanel.this.repaint();
					}
				}
			}
			
			public void mouseReleased(MouseEvent e) 
			{
				if ( e.getButton() == MouseEvent.BUTTON1 ) 
				{ 
					selected = null;
				}
			}
			
			public void mouseDragged(MouseEvent e) 
			{
				if ( selected != null ) 
				{
					selected.position = viewToModel( e.getX() , e.getY() );
					RenderPanel.this.repaint();
				}
			}
		};
		
		private final KeyAdapter keyListener = new KeyAdapter() 
		{
			public void keyTyped(java.awt.event.KeyEvent e) 
			{
				if ( e.getKeyChar() == 's' ) 
				{
					simulationThread.toggle();
				}
			}
		};
		public RenderPanel(SpringMassSystem system) 
		{
			this.system = system;
			
			setFocusable(true);
			
			addKeyListener( keyListener );
			addMouseListener( mouseAdapter );
			addMouseMotionListener( mouseAdapter );
		}
		
		private Vector4 viewToModel(int x,int y) {
			
			double scaleX = getWidth() / (double) X_RESOLUTION;
			double scaleY = getHeight() / (double) Y_RESOLUTION;
			return new Vector4( x / scaleX , y / scaleY , 0 );
		}
		
		private Point modelToView(Vector4 vec) 
		{
			double scaleX = getWidth() / (double) X_RESOLUTION;
			double scaleY = getHeight() / (double) Y_RESOLUTION;
			return modelToView( vec , scaleX , scaleY ); 
		}
		
		private Point modelToView(Vector4 vec,double scaleX,double scaleY) 
		{
			return new Point( (int) Math.round( vec.x * scaleX ) , (int) Math.round( vec.y * scaleY ) );
		}		
		
		@Override
		protected void paintComponent(Graphics g) 
		{
			super.paintComponent(g);
			
			synchronized( system ) 
			{
				final double scaleX = getWidth() / (double) X_RESOLUTION;
				final double scaleY = getHeight() / (double) Y_RESOLUTION;
				
				final int boxWidthUnits = 5;
	
				final int boxWidthPixels = (int) Math.round( boxWidthUnits * scaleX );
				final int boxHeightPixels = (int) Math.round( boxWidthUnits * scaleY );
				
				final int halfBoxWidthPixels = (int) Math.round( boxWidthPixels / 2.0 );
				final int halfBoxHeightPixels = (int) Math.round( boxHeightPixels / 2.0 );
				
				// render masses
				g.setColor(Color.BLUE);
				for ( Mass m : system.masses ) 
				{
					final Point p = modelToView( m.position , scaleX , scaleY );
					if ( selected == m ) 
					{
						g.setColor(Color.RED );
						g.drawRect( p.x - halfBoxWidthPixels , p.y - halfBoxHeightPixels , boxWidthPixels , boxHeightPixels );
						g.setColor(Color.BLUE);
					} else {
						g.drawRect( p.x - halfBoxWidthPixels , p.y - halfBoxHeightPixels , boxWidthPixels , boxHeightPixels );	
					}
				}
				
				// render springs
				g.setColor(Color.GREEN);
				for ( Spring s : system.springs ) 
				{
					final Point p1 = modelToView( s.m1.position );
					final Point p2 = modelToView( s.m2.position );
					g.drawLine( p1.x , p1.y , p2.x , p2.y );
				}
			}
		}
	}
}
