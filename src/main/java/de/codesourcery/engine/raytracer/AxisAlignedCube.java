package de.codesourcery.engine.raytracer;

public class AxisAlignedCube extends Raytracable
{
    public Vector4 center;

    private final double[] minArray;
    private final double[] maxArray;
    
    private final Vector4 min;
    private final Vector4 max;

    public AxisAlignedCube(String name, Vector4 center, double width, double height, double depth)
    {
        super(name);
        this.center = center;

        final double halfWidth = width / 2;
        final double halfHeight = height / 2;
        final double halfDepth = depth / 2;

        max = new Vector4(center.x + halfWidth, center.y + halfHeight, center.z + halfDepth); // MAX
        min = new Vector4(center.x - halfWidth, center.y - halfHeight, center.z - halfDepth); // MIN   

//        minArray = min.toArray3D();
//        maxArray = max.toArray3D();
        
        maxArray = max.toArray3D();
        minArray = min.toArray3D();        
    }
    
    @Override
    public IntersectionInfo intersect(Ray ray)
    {
        // r.dir is unit direction vector of ray
        double dirFracx = 1.0f / ray.direction.x;
        double dirFracy = 1.0f / ray.direction.y;
        double dirFracz = 1.0f / ray.direction.z;
        
        Vector4 rayOrigin = ray.point;
        
        // lb is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
        // r.org is origin of ray
        double t1 = (min.x - rayOrigin.x)*dirFracx;
        double t2 = (max.x - rayOrigin.x)*dirFracx;
        double t3 = (min.y - rayOrigin.y)*dirFracy;
        double t4 = (max.y - rayOrigin.y)*dirFracy;
        double t5 = (min.z - rayOrigin.z)*dirFracz;
        double t6 = (max.z - rayOrigin.z)*dirFracz;
        
        double tmin = Math.max( Math.max( Math.min( t1, t2) , Math.min(t3, t4) ) , Math.min(t5, t6));
        
        double tmax = Math.min( Math.min( Math.max( t1, t2)  , Math.max(t3, t4)) , Math.max(t5, t6));

        // tmax < 0: ray (line) is intersecting AABB, but whole AABB is behind us
        // tmin > tmax: ray doesn't intersect AABB        
        if (tmax < 0 || tmin > tmax)
        {
            return null;
        }
        return new IntersectionInfo( this ).addSolution( tmin );        
    }

    @Override
    public Vector4 normalVectorAt(Vector4 p)
    {
        final double EPS = 0.001f;

        if (Math.abs( p.x - max.x) < EPS) 
        {
            return Vector4.LEFT;
        } 
        
        if (Math.abs( p.x - min.x) < EPS) 
        {
            return Vector4.RIGHT; // left side
        } 
        
        if (Math.abs( p.y - max.y) < EPS) 
        {
            return Vector4.DOWN;            
        } 
        if (Math.abs( p.y - min.y) < EPS) 
        {
           return Vector4.UP;
        } 
        if (Math.abs( p.z - max.z ) < EPS) 
        {
            return Vector4.BACKWARDS;
        } 
        if (Math.abs( p.z - min.z ) < EPS) 
        {
            return Vector4.FORWARDS;            
        }
        throw new RuntimeException("Internal error, point "+p+" is not on "+this);
    }
}