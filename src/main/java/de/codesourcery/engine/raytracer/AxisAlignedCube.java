package de.codesourcery.engine.raytracer;

public class AxisAlignedCube extends Raytracable
{
    private final Vector4 min;
    private final Vector4 max;

    public AxisAlignedCube(String name, double width, double height, double depth)
    {
        super(name);

        final double halfWidth = width / 2.0d;
        final double halfHeight = height / 2.0d;
        final double halfDepth = depth / 2.0d;

        max = new Vector4( + halfWidth, + halfHeight, + halfDepth); // MAX
        min = new Vector4( - halfWidth, - halfHeight, - halfDepth); // MIN   
    }
    
    @Override
    public IntersectionInfo intersect(Ray ray)
    {
        if ( transformation != null ) 
        {
            ray = ray.transform( transformation );
        }
        
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
        IntersectionInfo result = new IntersectionInfo( this );
        result.nearestIntersectionPoint = ray.evaluateAt( tmin );
        return result;
    }

    @Override
    public Vector4 normalVectorAt(Vector4 p)
    {
//        if ( transformation != null ) {
//            p = p.multiply( transformation );
//        }
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
//        return result;
        return transformation == null ? result : transformation.invert().transpose().multiply( result ); 
    }
}