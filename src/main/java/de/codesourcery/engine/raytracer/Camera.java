package de.codesourcery.engine.raytracer;

public class Camera
{
    public Vector4 position;
    public Vector4 direction;
    public Vector4 up = new Vector4(0,1,0);
    
    public double distanceToViewPlane=100;
    public double viewPlaneWidth=512;
    public double viewPlaneHeight=512;
    
    public Camera(Vector4 position, Vector4 direction ) {
        this.position = position;
        this.direction = direction.normalize();
    }
}
