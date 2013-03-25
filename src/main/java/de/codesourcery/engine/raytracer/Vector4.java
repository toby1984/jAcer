package de.codesourcery.engine.raytracer;

import java.text.DecimalFormat;

public final class Vector4 
{
    public double x;
    public double y;
    public double z;
    public double w;
    
    public Vector4(Vector4 input) 
    {
        this.x = input.x;
        this.y = input.y;
        this.z = input.z;
        this.w = input.w;
    }
    
    public int toRGB() {
        int color = ((int) ( r() *255f) ) << 16;
        color |= ((int) ( g() *255f) ) << 8;
        color |= ((int) ( b() *255f) );
        return color;
    }
    
    public Vector4() {
    }
    
    public Vector4(double[] data) {
        this.x=data[0];
        this.y=data[1];
        this.z=data[2];
        this.w=data[3];
    }    
    
    public void setData(double[] data,int offset) {
        this.x = data[offset];
        this.y = data[offset+1];
        this.z = data[offset+2];
        this.w = data[offset+3];
    }
    
    public void copyFrom(Vector4 other)
    {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
    }    
    
    public void copyInto(double[] array,int startingOffset) 
    {
        array[startingOffset] = x;
        array[startingOffset+1] = y;
        array[startingOffset+2] = z;
        array[startingOffset+3] = w;
    }
    
    public Vector4(double[] data,int offset) 
    {
        this.x = data[offset];
        this.y = data[offset+1];
        this.z = data[offset+2];
        this.w = data[offset+3];
    }
    
    public Vector4 flip() {
    	return new Vector4(-x,-y,-z,w);
    }
    
    public void flipInPlace() 
    {
    	this.x = -x;
    	this.y = -y;
    	this.z = -z;
    }    
    
    public boolean isEquals(Vector4 other) 
    {
        return this.x == other.x &&
                this.y == other.y &&
                this.z == other.z &&
                this.w == other.w;                
    }
    
    public void x(double value) {
        this.x = value;
    }
    
    public void r(double value) {
        this.x = value;
    }    
    
    public void y(double value) {
        this.y = value;        
    }
    
    public void g(double value) {
        this.y = value;
    }    
    
    public void z(double value) {
        this.z = value;
    }
    
    public void b(double value) {
        this.z = value;        
    }    
    
    public void w(double value) {
        this.w = value;        
    }
    
    public void a(double value) {
        this.w = value;
    }    
    
    public double x() {
        return x;
    }
    
    public double r() {
        return x;
    }    
    
    public double y() {
        return y;
    }
    
    public double g() {
        return y;
    }    
    
    public double z() {
        return z;
    }
    
    public double b() {
        return z;
    }    
    
    public double w() {
        return w;
    }
    
    public double a() {
        return w;
    }    
    
    public Vector4 minus(Vector4 other) 
    {
        return new Vector4( this.x - other.x , this.y - other.y , this.z - other.z , this.w );
    }
    
    public void minusInPlace(Vector4 other) 
    {
        this.x = this.x - other.x;
        this.y = this.y - other.y;
        this.z = this.z - other.z;
    }    
    
    public double distanceTo(Vector4 point) 
    {
    	double x = this.x - point.x;
    	double y = this.y - point.y;
    	double z = this.z - point.z;
    	return Math.sqrt( x*x + y*y + z*z );
    }
    
    public Vector4 plus(Vector4 other) {
        return new Vector4( this.x + other.x , this.y + other.y , this.z + other.z , w );
    }     
    
    public void plusInPlace(Vector4 other) 
    {
        this.x = this.x + other.x;
        this.y = this.y + other.y;
        this.z = this.z + other.z;        
    }     
    
    public Vector4(double x,double y,double z) {
        this(x,y,z,1);
    }
    
    public Vector4(double x,double y,double z,double w) 
    {
        this.x = x;
        this.y = y;
        this.z=z;
        this.w=w;
    }
    
    public Vector4 multiply( Matrix matrix) 
    {
        final double[] result = new double[4];
        final double[] matrixData = matrix.getData();

        result[0] = x * matrixData[0] + y * matrixData[1]+
                    z * matrixData[2]+ w * matrixData[3];
        
        result[1] = x * matrixData[4] + y * matrixData[5] +
                    z * matrixData[6] + w * matrixData[7];
        
        result[2] = x * matrixData[8] + y * matrixData[9] +
                    z * matrixData[10] + w * matrixData[11];
        
        result[3] = x * matrixData[12] + y * matrixData[13] +
                    z * matrixData[14] + w * matrixData[15];
        
        return new Vector4( result );
    }
    
    public Vector4 multiply(double value) 
    {
        return new Vector4( x * value , y * value , z * value , w );
    }
    
    public void multiplyInPlace(double value) 
    {
        this.x = this.x * value;
        this.y = this.y * value;
        this.z = this.z * value;
    }    
    
    public Vector4 normalize() 
    {
        final double len = length();
        if ( len  == 0 ) {
        	return new Vector4(0,0,0); 
        }
        return new Vector4( x / len , y / len , z / len  , w );
    }
    
    public void normalizeInPlace() 
    {
        final double len = length();
        if ( len  != 0 && len != 1 ) 
        {
        	this.x = this.x / len;
        	this.y = this.y / len;
        	this.z = this.z / len;
        }
    }    
    
    public Vector4 normalizeW() 
    {
        if ( w != 1.0 ) 
        {
            return new Vector4( x / w, y / w , z / w , 1 );
        }
        return this;
    }    
    
    public void normalizeWInPlace() 
    {
        if ( w != 1.0 ) 
        {
        	x = x / w ;
        	y = y / w ;
        	z = z / w ;
        }
    }      
    
    // scalar / dot product
    public double dotProduct(Vector4 o) 
    {
        return x*o.x + y*o.y + z * o.z;
    }
    
    public Vector4 straightMultiply(Vector4 o) 
    {
        return new Vector4( this.x * o.x , this.y * o.y , this.z * o.z, this.w * o.w );
    }    
    
    public double length() 
    {
        return Math.sqrt( x*x + y*y + z*z );   
    }
    
    public double magnitude() {
        return x*x + y * y + z * z;   
    }    
    
    public double angleInRadians(Vector4 o) {
        // => cos
        final double cosine = dotProduct( o ) / ( length() * o.length() );
        return Math.acos( cosine );
    }
    
    public double angleInDegrees(Vector4 o) {
        final double factor = (180.0d / Math.PI);
        return angleInRadians(o)*factor;
    }        
    
    public Vector4 crossProduct(Vector4 o) 
    {
        double newX = y * o.z - o.y * z;
        double newY = z * o.x - o.z * x;
        double newZ = x * o.y - o.x * y;
        return new Vector4( newX ,newY,newZ );
    }
    
    @Override
    public String toString()
    {
        return "("+format( x() ) +","+format( y() ) +","+format( z() )+","+format( w() )+")";
    }
    
    private static String format(double d) {
        return new DecimalFormat("##0.0###").format( d );
    }

	public Vector4 clamp(double min, double max) 
	{
		double newX = x;
		double newY = y;
		double newZ = z;
		if ( newX < min ) {
			newX = min;
		} else if ( newX > max ) {
			newX = max;
		}
		if ( newY < min ) {
			newY = min;
		} else if ( newY > max ) {
			newY = max;
		}
		if ( newZ < min ) {
			newZ = min;
		} else if ( newZ > max ) {
			newZ = max;
		}		
		return new Vector4(newX,newY,newZ);
	}
}