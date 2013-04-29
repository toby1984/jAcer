package de.codesourcery.engine.springmass;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import de.codesourcery.engine.raytracer.Vector4;

public class SpringPanel extends JPanel
{
    private final SpringMassSystem system = new SpringMassSystem();
    
    public static final double unitsX = 100;
    public static final double unitsY = 100;
    
    private volatile Mass selected;
    
    private final SimulationThread simulationThread = new SimulationThread();
    
    protected static enum SimulationState {
        RUNNING,
        STOPPED;
        
        public final SimulationState inverse() 
        {
            switch( this ) {
                case RUNNING:
                    return STOPPED;
                case STOPPED:
                    return RUNNING;
                default:
                    throw new RuntimeException("Internal error");
            }
        }
    }
    
    protected final class SimulationThread extends Thread {

        private final double timeInc = 0.0001;
        
        private final Object LOCK = new Object();
        
        // @GuardedBy( LOCK )
        private SimulationState state = SimulationState.STOPPED;
        
        public SimulationThread() 
        {
            setDaemon(true);
            setName("simulation-thread");
        }
        
        @Override
        public void run()
        {
            while( true ) 
            {
                synchronized(LOCK) 
                {
                    while ( state != SimulationState.RUNNING ) {
                        try {
                            LOCK.wait();
                        } 
                        catch(Exception e) {
                            // ok
                        }
                    }
                }
                step();
                
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                }
            }
        }
        
        private void step() 
        {
            system.step( timeInc );
            
            try {
                SwingUtilities.invokeAndWait( new Runnable() {

                    @Override
                    public void run()
                    {
                        SpringPanel.this.repaint();
                    }
                });
            } 
            catch (InvocationTargetException | InterruptedException e) 
            {
                throw new RuntimeException(e);
            }
        }
        
        public void startSimulation() 
        {
            synchronized(LOCK) {
                state = SimulationState.RUNNING;
                LOCK.notifyAll();
            }
        }
        
        public void toggleState() 
        {
            SimulationState newState;
            synchronized(LOCK) {
                newState = state.inverse();
                state = newState;
                LOCK.notifyAll();                
            }
            System.out.println("Simulation is now "+newState);
        }
        
        public void stopSimulation() 
        {
            synchronized(LOCK) {
                state = SimulationState.STOPPED;
                LOCK.notifyAll();
            }
        }        
    }
    
    private final KeyListener keyListener = new KeyAdapter() {
        
        public void keyTyped(java.awt.event.KeyEvent e) 
        {
            System.out.println("key typed: "+e.getKeyChar());
            if ( e.getKeyChar() == 's' ) 
            {
                simulationThread.toggleState();
            }
        }
    };
    
    private final MouseListener mouseListener = new MouseAdapter()
    {
        public void mouseClicked(java.awt.event.MouseEvent e) 
        {
            final Vector4 modelCoords = viewToModel( e.getX() ,  e.getY() );      
            final Mass closest = system.getClosestMass( modelCoords , 100.0 );
            boolean repaint = false;
            switch ( e.getButton() ) 
            {
                case MouseEvent.BUTTON1:
                    Mass m = new Mass(modelCoords,1.0 ) ;
                    System.out.println("Added "+m);
                    system.addMass( m );
                    repaint = true;
                    break;
                case MouseEvent.BUTTON2:
                    if ( closest != null ) 
                    {
                        closest.setAnchor( ! closest.isAnchor );
                        repaint = true;
                    }
                    break;
                case MouseEvent.BUTTON3:
                    
                    if ( selected == null ) 
                    {
                        selected = system.getClosestMass( modelCoords , 100.0 );
                        repaint = true;
                    }
                    else if ( closest != null ) 
                    {
                        selected.addNeighbor( closest );
                        System.out.println("Connected "+selected +" -> "+closest);
                        selected = null;
                        repaint = true;
                    }
                    break;
                default:
                    // ok , fall-through
            }
            
            if ( repaint ) 
            {
                SwingUtilities.invokeLater( new Runnable() {

                    @Override
                    public void run()
                    {
                        SpringPanel.this.repaint();
                    }
                });                
            }
        }
    };
    
    private Vector4 viewToModel(int px,int py) 
    {
        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;
        
        double scaleX = getWidth() / unitsX;
        double scaleY = getHeight() / unitsY;
        
        double x = (px - centerX) / scaleX;
        double y = (centerY - py) / scaleY;
        Vector4 result = new Vector4(x,y,0);
        Point p = modelToView(result);
        return result;
    }
    
    private Point modelToView(Vector4 v) 
    {
        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;
        
        double scaleX = getWidth() / unitsX;
        double scaleY = getHeight() / unitsY;
        
        double x = centerX+( v.x * scaleX );
        double y = centerY-( v.y * scaleY );
        return new Point( (int) Math.round(x) , (int) Math.round(y) );
    }
    
    public static void main(String[] args)
    {
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.getContentPane().setLayout( new GridBagLayout() );
        
        final GridBagConstraints cnstrs = new GridBagConstraints();
        cnstrs.fill=GridBagConstraints.BOTH;
        cnstrs.weightx = 1.0;
        cnstrs.weighty = 1.0;
        
        SpringPanel panel = new SpringPanel();
        panel.setMinimumSize( new Dimension(400,200 ) );
        panel.setPreferredSize( new Dimension(400,200 ) );
        frame.getContentPane().add( panel , cnstrs );
        
        frame.pack();
        frame.setVisible( true );
    }
    
    public SpringPanel() {
        simulationThread.start();
        addMouseListener( mouseListener );
        addKeyListener( keyListener );
        setFocusable( true );
    }
    
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        final double scaleX = getWidth() / unitsX;
        final double scaleY = getHeight() / unitsY;        
        
        Graphics2D graphics = (Graphics2D) g;
        for ( Mass m : system.getMasses() ) 
        {
            renderMass( m , scaleX,scaleY  ,  graphics );
        }
    }

    private void renderMass(Mass m, double scaleX,double scaleY , Graphics2D graphics)
    {
        final Point p1 = modelToView( m.position );
        
        // render springs
        graphics.setColor(Color.BLUE);
        for ( Mass n : m.getNeighbors() ) 
        {
            Point p2 = modelToView( n.position );
            graphics.drawLine( p1.x , p1.y , p2.x , p2.y );
        }
        
        // render mass
        if ( selected == m ) 
        {
            graphics.setColor(Color.WHITE);
            graphics.fillArc( p1.x , p1.y , 5 , 5 , 0 , 360 );
        } 
        else 
        {
            if ( m.isAnchor ) {
                graphics.setColor(Color.RED);
            } else {
                graphics.setColor(Color.GREEN);
            }
            graphics.fillArc( p1.x , p1.y , 5 , 5 , 0 , 360 );            
        }        
    }
}