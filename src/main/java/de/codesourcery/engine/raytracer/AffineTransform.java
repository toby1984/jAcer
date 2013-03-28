package de.codesourcery.engine.raytracer;


public abstract class AffineTransform 
{
	private final boolean isTranslation;
	private final boolean isRotation;
	private final boolean isScaling;
	
	protected AffineTransform(boolean isTranslation, boolean isRotation,boolean isScaling) 
	{
		this.isTranslation = isTranslation;
		this.isRotation = isRotation;
		this.isScaling = isScaling;
	}

	protected static final class Scale extends AffineTransform 
	{
		private final double scaleX;
		private final double scaleY;
		private final double scaleZ;
		
		public Scale(double scaleX, double scaleY,double scaleZ) 
		{
			super(false,false,true);
			if ( scaleX == 0 || scaleY == 0 || scaleZ == 0 ) {
				throw new IllegalArgumentException("scaling factor must not be zero");
			}
			this.scaleX = scaleX;
			this.scaleY = scaleY;
			this.scaleZ = scaleZ;
		}
		
        @Override
        public String toString()
        {
            return "Scaling[ x: "+scaleX+" , y: "+scaleY+" , z: "+scaleZ+" ]";
        }		
		
		@Override
		public Vector4 apply(Vector4 v) 
		{
			return new Vector4(v.x * scaleX ,v.y * scaleY , v.z * scaleZ);
		}
		
		@Override
		public Vector4 applyInverse(Vector4 v) 
		{
			return new Vector4(v.x / scaleX ,v.y/scaleY , v.z/scaleZ);
		}

		@Override
		public Matrix getMatrix() {
			return LinAlgUtils.scalingMatrix( scaleX , scaleY , scaleZ );
		}
	}
	
	protected static final class Translation extends AffineTransform 
	{
		private final double translateX;
		private final double translateY;
		private final double translateZ;
		
		public Translation(double translateX, double translateY,double translateZ) 
		{
			super(true,false,false);
			this.translateX = translateX;
			this.translateY = translateY;
			this.translateZ = translateZ;
		}
		
		@Override
		public Matrix getMatrix() {
			return LinAlgUtils.translationMatrix( translateX , translateY , translateZ );
		}		
		
		@Override
		public String toString()
		{
		    return "Translation[ x: "+translateX+" , y: "+translateY+" , z: "+translateZ+" ]";
		}
		
		@Override
		public Vector4 applyInverse(Vector4 v) 
		{
			return new Vector4(v.x + translateX ,v.y + translateY , v.z + translateZ);
		}
		
		@Override
		public Vector4 apply(Vector4 v) {
			return new Vector4(v.x - translateX ,v.y - translateY , v.z - translateZ);
		}
	}
	
	public static void main(String[] args) {
		
		Vector4 test = new Vector4(0,0,1);
		Transformation t = new Transformation( AffineTransform.rotate( 0 , 0, 90 ) );
		System.out.println("Rotated: "+t.transform( test ) );
		System.out.println("Back: "+t.transformInverse( t.transform( test ) ) );
	}
	
	protected static final class Rotation extends AffineTransform 
	{
		private final Matrix m;
		private final Matrix mInverse;
		
		private final double rotX;
		private final double rotY;
		private final double rotZ;

		public Rotation(double rotX, double rotY,double rotZ) 
		{
			super(false,true,false);
			
			this.rotX = rotX;
			this.rotY = rotY;
			this.rotZ = rotZ;
			
			Matrix tmp = Matrix.identity();
			if ( rotX != 0 ) {
				tmp = tmp.multiply( LinAlgUtils.rotX(rotX ) );
			}
			if ( rotY != 0 ) {
				tmp = tmp.multiply( LinAlgUtils.rotY(rotY ) );
			}
			if ( rotZ != 0 ) {
				tmp = tmp.multiply( LinAlgUtils.rotZ(rotZ ) );
			}			
			m = tmp;
			Matrix tmp2 = Matrix.identity();
            if ( rotZ != 0 ) {
                tmp2 = tmp2.multiply( LinAlgUtils.rotZ( - rotZ ) );
            } 			
            if ( rotY != 0 ) {
                tmp2 = tmp2.multiply( LinAlgUtils.rotY( - rotY ) );
            }            
			if ( rotX != 0 ) {
			    tmp2 = tmp2.multiply( LinAlgUtils.rotX( - rotX ) );
			}
			mInverse = tmp2;
		}
		
		@Override
		public Vector4 apply(Vector4 v) 
		{
			return v.multiply( m );
		}
		
        @Override
        public String toString()
        {
            return "Rotation[ x: "+rotX+" , y: "+rotY+" , z: "+rotZ+" ]";
        }		
		
		@Override
		public Vector4 applyInverse(Vector4 v) {
			return v.multiply( mInverse );
		}

		@Override
		public Matrix getMatrix() {
			return m;
		}
	}	

	public static AffineTransform translate(double x,double y,double z) {
		return new Translation(x,y,z);
	}
	
	public static AffineTransform scale(double x,double y,double z) {
		return new Scale(x,y,z);
	}
	
	public static AffineTransform rotate(double x,double y,double z) {
		return new Rotation(x,y,z);
	}
	
	public abstract Vector4 apply(Vector4 v);
	
	public abstract Vector4 applyInverse(Vector4 v);	
	
	public abstract Matrix getMatrix();
	
	public final boolean isRotation() {
		return isRotation;
	}
	
	public final boolean isScaling() {
		return isScaling;
	}
	
	public final boolean isTranslation() {
		return isTranslation;
	}
}