package de.codesourcery.engine.springmass;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.codesourcery.engine.raytracer.Vector4;

public class SpringMassSystem
{
    private final List<Mass> masses = new ArrayList<>();
    
    public SpringMassSystem() {
    }
    
    public void addMass(Mass m) 
    {
        masses.add( m );
    }
    
    public List<Mass> getMasses()
    {
        return masses;
    }
    
    public Mass getClosestMass(Vector4 point,double maxDelta) {
        
        // TODO: For large mass counts, replace slow k-nearest algorithm with KD-Tree 
        double bestDelta = Double.MAX_VALUE;
        Mass bestMatch = null;
        for ( Mass m : masses ) 
        {
            double delta = m.position.distanceTo( point );
            if ( bestMatch == null || delta < bestDelta ) {
                bestMatch = m;
                bestDelta = delta;
            }
        }
        return bestDelta <= maxDelta ? bestMatch : null;
    }
    
    public void step(double deltaT) {
        
        /* 
         * Verlet integration:
         * 
         * F = total of forces acting on this point
         * T = Time step to update over
         * 
         * X0 is the previous position, X1 is the current position
         * 
         * XT = X1
         * X1 += (X1-X0) + F/M*T*T
         * X0 = XT         
         */
        final List<Vector4> newPositions = new ArrayList<>();
        for ( Mass m : masses ) 
        {
            if ( ! m.isAnchor ) {
                final Vector4 force = m.calcForce();
                final Vector4 newPosition = m.position.minus( m.previousPosition ).plus( force.multiply( 1.0 / ( m.mass * deltaT * deltaT) ) );
                newPositions.add( newPosition );
            }
        }
        
        final Iterator<Mass> it1 = masses.iterator();
        final Iterator<Vector4> it2 = newPositions.iterator();
        while( it1.hasNext() ) 
        {
            Mass mass = it1.next();
            if ( ! mass.isAnchor ) 
            {
                final Vector4 newPosition = it2.next();                
                mass.updatePosition( newPosition );
            }
        }
    }
}