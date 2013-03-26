package de.codesourcery.engine.raytracer;

public class AxisAlignedCube extends Raytracable
{
    public Vector4 center;

    public Matrix matrix=Matrix.identity();
    
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
    
    public static void main(String[] args)
    {
        final AxisAlignedCube cube = new AxisAlignedCube("test" , new Vector4(0,0,0 ) , 10 , 10 , 10 );
        
        Ray r = new Ray(new Vector4(0,100,0) , new Vector4(0,-1,0) );
        
        IntersectionInfo intersectionInfo = cube.intersect( r );
        if ( intersectionInfo != null ) {
            System.out.println("Intersection: "+intersectionInfo);
            Vector4 p = r.evaluateAt( intersectionInfo.solutions[0] );
            Vector4 normal = cube.normalVectorAt( p );
            System.out.println("Normal: "+normal);
        } else {
            System.out.println("NO intersection.");
        }
    }

    @Override
    public IntersectionInfo intersect(Ray ray)
    {
        ray = ray.transform( matrix );
        
        // r.dir is unit direction vector of ray
        double dirfracx = 1.0f / ray.direction.x;
        double dirfracy = 1.0f / ray.direction.y;
        double dirfracz = 1.0f / ray.direction.z;
        
        Vector4 rorg = ray.point;
        
        // lb is the corner of AABB with minimal coordinates - left bottom, rt is maximal corner
        // r.org is origin of ray
        double t1 = (min.x - rorg.x)*dirfracx;
        double t2 = (max.x - rorg.x)*dirfracx;
        double t3 = (min.y - rorg.y)*dirfracy;
        double t4 = (max.y - rorg.y)*dirfracy;
        double t5 = (min.z - rorg.z)*dirfracz;
        double t6 = (max.z - rorg.z)*dirfracz;
        
        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));

        // if tmax < 0, ray (line) is intersecting AABB, but whole AABB is behing us
        // if tmin > tmax, ray doesn't intersect AABB        
        if (tmax < 0 || tmin > tmax)
        {
            return null;
        }
        return new IntersectionInfo( this ).addSolution( tmin );        
    }

    @Override
    public Vector4 normalVectorAt(Vector4 pointOfIncident)
    {
        final Vector4 p = pointOfIncident.multiply( matrix );
        final double EPS = 0.01f;

        if (Math.abs( p.x - max.x) < EPS) 
        {
            return new Vector4(-1, 0, 0);
        } 
        if (Math.abs( p.x - min.x) < EPS) 
        {
            return new Vector4(1, 0, 0); // left side
        } 
        if (Math.abs( p.y - max.y) < EPS) 
        {
            return new Vector4(0, -1, 0);
        } 
        if (Math.abs( p.y - min.y) < EPS) 
        {
           return new Vector4(0, 1, 0);
        } 
        if (Math.abs( p.z - max.z ) < EPS) 
        {
            return new Vector4(0, 0, 1);
        } 
        if (Math.abs( p.z - min.z ) < EPS) 
        {
            return new Vector4(0, 0, -1);
        }
        throw new RuntimeException("Internal error, point "+p+" is not on "+this);
    }
}