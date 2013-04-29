package de.codesourcery.engine.springmass;

import de.codesourcery.engine.raytracer.Vector4;


public class Spring
{
    /* 1. The rest length, which is the length of the spring when it is neither stretched not compressed
     * 2. The minimum length of the spring when fully compressed
     * 3. The maximum length of the spring when fully extended
     * 4. The force exerted by the spring, proportional to its displacement from the rest length     
     */
    public double maxLength=5.0;
    public double minLength=1.0;
    public double restLength=2.5;
    
    public double force=1.0;
    public double dampening=0.5;
    
    public Spring()
    {
    }
    
    public Vector4 calcForce(Mass m1,Mass m2) 
    {
        final Vector4 v12 = m2.position.minus( m1.position );
        
        double actualLength = v12.length();
        double delta = restLength - actualLength;
        
        v12.normalizeInPlace();
        
        double dampFactor = dampening*delta;
        v12.multiplyInPlace( -force * delta - dampFactor );
        return v12;
    }
}