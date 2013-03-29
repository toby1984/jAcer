package de.codesourcery.engine.raytracer;


public class AxisAlignedCube extends Raytracable
{
	private Transformation transform;	
    private Vector4 min;
    private Vector4 max;
    
    public AxisAlignedCube(String name, Vector4 center,double width, double height, double depth)
    {
        super(name);
        
        final double halfWidth = width / 2.0d;
        final double halfHeight = height / 2.0d;
        final double halfDepth = depth / 2.0d;

        max = new Vector4( halfWidth, halfHeight, halfDepth); // MAX
        min = new Vector4( - halfWidth, - halfHeight, - halfDepth); // MIN
        
        transformation( new Transformation( AffineTransform.translate( center.x , center.y , center.z ) ) );
    }
    
    @Override
    public String toString()
    {
        return "Cube[ "+name+" , min: "+min+" / max: "+max+" , transform: "+transform+" ]";
    }
    
    public static void main(String[] args)
    {
        Vector4 input = new Vector4(1,2,3 );
//        Transformation transform = new Transformation( AffineTransform.rotate( 45, 90 , 0 ) , AffineTransform.translate( 0 , -500 , -400 ));
        Transformation transform = new Transformation( AffineTransform.translate( 0 , -500 , -400 ) ,  AffineTransform.rotate( 45, 90 , 0 ));
        Vector4 p2 = transform.transformInverse( transform.transform( input ) );
        System.out.println("Point #1: "+input);
        System.out.println("Point #2: "+p2);
    }
    
    public void transformation(Transformation transform) 
    {
    	this.transform = transform; 
    }
    
    @Override
    public IntersectionInfo intersect(Ray inputRay)
    {
    	// translate ray by center
    	Ray ray = inputRay.transform( transform );
        
        // r.dir is unit direction vector of ray
        double dirFracx = 1.0d / ray.direction.x;
        double dirFracy = 1.0d / ray.direction.y;
        double dirFracz = 1.0d / ray.direction.z;
        
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
        
        Vector4 point = ray.evaluateAt( tmin ); // reverse translation
        point = transform.transformInverse( point );
       	return new IntersectionInfo( this, inputRay.solutionAt( point ) );
    }

    @Override
    public Vector4 normalVectorAt(Vector4 point,Camera camera)
    {
    	Vector4 p = transform.transform( point );
    	
        final double EPS = 0.01f;

        final Vector4 result;
        if (Math.abs( p.x - max.x) < EPS) // p.x on max.x plane
        {
            result = camera.xAxis;
        } 
        else if (Math.abs( p.x - min.x) < EPS) // p.x on min.x plane
        {
            result = camera.xAxis.flip(); 
        } else if (Math.abs( p.y - max.y) < EPS) // p.y on max.y plane
        {
            result = camera.yAxis;
        } else if (Math.abs( p.y - min.y) < EPS) // p.y on min.y plane
        {
           result = camera.yAxis.flip();
        } else if (Math.abs( p.z - max.z ) < EPS) // p.z on max.z plane
        {
            result = camera.zAxis.flip();
        } else if (Math.abs( p.z - min.z ) < EPS) // p.z on min.z plane
        {
            result = camera.zAxis;
        } else {
            throw new RuntimeException("Internal error, point "+p+" is not on "+this);
        }
        return transform.getFirstRotation().applyInverse( result );
    }
}