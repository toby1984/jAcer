package de.codesourcery.engine.raytracer;

public final class Transformation {

	private final int count;
	
	private final AffineTransform first;
	private final AffineTransform second;
	private final AffineTransform third;
	
	private final Matrix invTransposedMatrix;
	
	public Transformation() 
	{
		first = second = third = null;
		this.count = 0;
		invTransposedMatrix = Matrix.identity();
	}
	
	public static void main(String[] args) 
	{
		Transformation t = new Transformation( AffineTransform.translate( 1 ,2 ,3 ) , AffineTransform.rotate( 1 ,2 ,3 ) , AffineTransform.scale( 1 ,2 ,3 )  );
		
		Vector4 v1 = new Vector4(1,2,3);
		System.out.println("IN: "+v1);
		Vector4 v2 = t.transform( v1 ); 
		System.out.println("OUT: "+v2);
		Vector4 v3 = t.transformInverse( v2 ); 
		System.out.println("ORIGINAL: "+v3);
		if ( ! v1.equals( v3 , 0.0000001 ) ) {
			throw new RuntimeException("Inversion failed");
		}
	}

	public Transformation(AffineTransform... affineTransforms) 
	{
		Matrix m = Matrix.identity();
		if ( affineTransforms != null && affineTransforms.length > 0 )
		{
			this.first = affineTransforms[0];
			m = this.first.getMatrix();
			if ( affineTransforms.length > 1 ) 
			{
				this.second= affineTransforms[1];
				m = m.multiply( this.second.getMatrix() );
				if ( affineTransforms.length > 2 ) 
				{
					this.third = affineTransforms[2];
					m = m.multiply( this.third.getMatrix() );
					this.count = 3;
				} 
				else 
				{
					this.count = 2;					
					this.third = null;
				}
			} else {
				this.count = 1;
				this.second = null;
				this.third = null;
			}
		} else {
			this.count = 0;
			this.first = null;
			this.second = null;
			this.third = null;
		}
		this.invTransposedMatrix = m.invert().transpose();
	}

	public Vector4 transform(Vector4 v) 
	{
		switch( count ) 
		{
			case 0:
				return v;
			case 1:
				return first.apply( v );
			case 2:
				return second.apply( first.apply( v ) );
			case 3:
				return third.apply( second.apply( first.apply( v ) ) );
			default:
				throw new RuntimeException("Unreachable code reached");
		}
	}
	
	public Vector4 transformDirection(Vector4 v) 
	{
		Vector4 result = v;
		switch( count ) 
		{
			case 0:
				return v;
			case 1:
				if ( first.isRotation() ) {
					result = first.apply( result );
				}
				break;
			case 2:
				if ( first.isRotation() ) {
					result = first.apply( result );
				}				
				if ( second.isRotation() ) {
					result = second.apply( result );
				}				
				break;
			case 3:
				if ( first.isRotation() ) {
					result = first.apply( result );
				}				
				if ( second.isRotation() ) {
					result = second.apply( result );
				}	
				if ( third.isRotation() ) {
					result = third.apply( result );
				}				
				break;
			default:
				throw new RuntimeException("Unreachable code reached");
		}		
		return result;
	}
	
	public Vector4 transformNormal(Vector4 v) {
		return v.multiply( invTransposedMatrix );
	}
	
	public Vector4 transformInverse(Vector4 v) 
	{
		switch( count ) 
		{
			case 0:
				return v;
			case 1:
				return first.applyInverse( v );
			case 2:
				return first.applyInverse( second.applyInverse( v ) );
			case 3:
				return first.applyInverse( second.applyInverse( third.applyInverse( v ) ) );
			default:
				throw new RuntimeException("Unreachable code reached");
		}
	}	
}