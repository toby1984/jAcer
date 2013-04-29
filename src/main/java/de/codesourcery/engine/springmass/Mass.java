package de.codesourcery.engine.springmass;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import de.codesourcery.engine.raytracer.Vector4;

public final class Mass
{
    private static final Integer DUMMY = new Integer(0);
    
    public Vector4 position;
    public Vector4 previousPosition;    
    
    public final Map<Mass,Integer> neighbors = new IdentityHashMap<Mass,Integer>();
    
    public double mass;
    
    public boolean isAnchor = false;
    
    private final Spring spring = new Spring();
    
    public Mass(Vector4 position, double mass)
    {
        this.position = position;
        this.previousPosition = new Vector4(position);
        this.mass = mass;
    }
    
    @Override
    public String toString()
    {
        return "Mass[ "+position+" ]";
    }
    
    public List<Mass> getNeighbors() {
        return new ArrayList<>( neighbors.keySet() );
    }
    
    public void addNeighbor(Mass m) 
    {
        this.neighbors.put(m,DUMMY);
    }
    
    public void setAnchor(boolean isAnchor)
    {
        this.isAnchor = isAnchor;
    }
    
    public Vector4 calcForce() 
    {
        if ( isAnchor ) {
            return new Vector4(0,0,0);
        }
        final Vector4 result = new Vector4(0,0,0);
        for ( Mass n : neighbors.keySet() ) 
        {
            result.plusInPlace( spring.calcForce( this , n ) );
        }
        result.plusInPlace( new Vector4(0,-1,0).multiply(9.81 ) );
        return result;
    }

    public void updatePosition(Vector4 newPosition)
    {
        this.previousPosition = position;
        this.position = newPosition;
    }
}
