package de.codesourcery.engine.raytracer;

import java.util.Vector;

public class AxisAlignedCube extends Raytracable
{
    private Vector4 min;
    private Vector4 max;

    private final double width;
    private final double height;
    private final double depth;
    
    private final Vector4 center;
    
    public AxisAlignedCube(String name, Vector4 center,double width, double height, double depth)
    {
        super(name);
        this.width = width;
        this.depth = depth;
        this.height = height;
        this.center = center;
        transformation( null );
    }
    
    @Override
    public void transformation(Matrix m) 
    {
    	super.transformation( m );
    	
        final double halfWidth = width / 2.0d;
        final double halfHeight = height / 2.0d;
        final double halfDepth = depth / 2.0d;

        max = new Vector4( halfWidth, halfHeight, halfDepth); // MAX
        min = new Vector4( - halfWidth, - halfHeight, - halfDepth); // MIN
    }
    
    @Override
    public IntersectionInfo intersect(Ray inputRay)
    {
    	// translate ray by center
    	Ray ray = inputRay.transform( center );
        
        // r.dir is unit direction vector of ray
        double dirFracx = 1.0f / ray.direction.x;
        double dirFracy = 1.0f / ray.direction.y;
        double dirFracz = 1.0f / ray.direction.z;
        
        Vector4 rayOrigin = ray.point;
        
        // lb is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
        // r.org is origin of ray
        double t1 = (min.x - rayOrigin.x) * dirFracx;
        double t2 = (max.x - rayOrigin.x) * dirFracx;
        double t3 = (min.y - rayOrigin.y) * dirFracy;
        double t4 = (max.y - rayOrigin.y) * dirFracy;
        double t5 = (min.z - rayOrigin.z) * dirFracz;
        double t6 = (max.z - rayOrigin.z) * dirFracz;
        
        double tmin = Math.max( Math.max( Math.min( t1, t2) , Math.min(t3, t4) ) , Math.min(t5, t6));
        double tmax = Math.min( Math.min( Math.max( t1, t2)  , Math.max(t3, t4)) , Math.max(t5, t6));

        // tmax < 0: ray (line) is intersecting AABB, but whole AABB is behind us
        // tmin > tmax: ray doesn't intersect AABB        
        if (tmax < 0 || tmin > tmax)
        {
            return null;
        }
        
        final Vector4 point = ray.evaluateAt( tmin ).plus( center ); // reverse translation by center
       	return new IntersectionInfo( this ).addSolution( inputRay.solutionAt( point ) );
    }

    @Override
    public Vector4 normalVectorAt(Vector4 point)
    {
    	Vector4 p = point.minus( center );
        final double EPS = 0.001f;

        final Vector4 result;
        if (Math.abs( p.x - max.x) < EPS) // p.x on max.x plane
        {
            result = Vector4.RIGHT; 
        } 
        else if (Math.abs( p.x - min.x) < EPS) // p.x on min.x plane
        {
            result = Vector4.LEFT; 
        } else if (Math.abs( p.y - max.y) < EPS) // p.y on max.y plane
        {
            result = Vector4.UP;
        } else if (Math.abs( p.y - min.y) < EPS) // p.y on min.y plane
        {
           result = Vector4.DOWN;
        } else if (Math.abs( p.z - max.z ) < EPS) // p.z on max.z plane
        {
            result = Vector4.OUTOF_VIEWPLANE;
        } else if (Math.abs( p.z - min.z ) < EPS) // p.z on min.z plane
        {
            result = Vector4.INTO_VIEWPLANE;   
        } else {
            throw new RuntimeException("Internal error, point "+p+" is not on "+this);
        }
        return result;
    }
}