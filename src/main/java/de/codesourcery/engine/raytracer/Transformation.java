package de.codesourcery.engine.raytracer;

public final class Transformation {

    private final int count;

    private final AffineTransform first;
    private final AffineTransform second;
    private final AffineTransform third;

    private final AffineTransform firstRotation;
    
    private final Matrix invTransposedMatrix;
    
    private final ChainedTransformation transform;    
    private final ChainedTransformation inverseTransform;

    public Transformation() 
    {
        first = second = third = null;
        this.count = 0;
        invTransposedMatrix = Matrix.identity();
        this.firstRotation = AffineTransform.IDENTITY;
        this.inverseTransform = new ChainedTransformation() {

			@Override
			public final Vector4 apply(Vector4 v) {
				return v;
			}
        };
        this.transform = new ChainedTransformation() {

			@Override
			public final Vector4 apply(Vector4 v) {
				return v;
			}
        };        
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
        AffineTransform rotation = null;
        if ( affineTransforms != null && affineTransforms.length > 0 )
        {
            this.first = affineTransforms[0];
            if ( this.first.isRotation() ) {
            	rotation = this.first;
            }
            m = this.first.getMatrix();
            if ( affineTransforms.length > 1 ) 
            {
                this.second= affineTransforms[1];
                if ( rotation == null && this.second.isRotation() ) {
                	rotation = this.second;
                }                
                m = m.multiply( this.second.getMatrix() );
                if ( affineTransforms.length > 2 ) 
                {
                    this.third = affineTransforms[2];
                    if ( rotation == null && this.third.isRotation() ) {
                    	rotation = this.third;
                    }                      
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
        this.firstRotation = rotation == null ? AffineTransform.IDENTITY : rotation;
        this.invTransposedMatrix = m.invert().transpose();
        this.inverseTransform = createInverseTransform();
        this.transform = createChainedTransformation();
    }
    
    private ChainedTransformation createInverseTransform() 
    {
        // make sure to apply transformations in reverse order third -> second -> first
        switch( count ) 
        {
            case 0:
                return new ChainedTransformation() {
					
					@Override
					public final Vector4 apply(Vector4 v) {
						return v;
					}
				};
            case 1:
                return new ChainedTransformation() {
					@Override
					public final Vector4 apply(Vector4 v) {
		                return first.applyInverse( v );						
					}
				};            	
            case 2:
                return new ChainedTransformation() {
					@Override
					public final Vector4 apply(Vector4 v) {
		                return first.applyInverse( second.applyInverse( v ) );						
					}
				};               	
            case 3:
                return new ChainedTransformation() {
					@Override
					public final Vector4 apply(Vector4 v) {
		                return first.applyInverse( second.applyInverse( third.applyInverse( v ) ) );		               
					}
				};             	
            default:
                throw new RuntimeException("Unreachable code reached");
        }    	
    }

    private ChainedTransformation createChainedTransformation() 
    {
        switch( count ) 
        {
            case 0:
            	return new ChainedTransformation() {

					@Override
					public final Vector4 apply(Vector4 v) {
						return v;
					}
            	}; 
            case 1:
            	return new ChainedTransformation() {

					@Override
					public final Vector4 apply(Vector4 v) {
		                return first.apply( v );					
					}
            	};             	
            case 2:
            	return new ChainedTransformation() {

					@Override
					public final Vector4 apply(Vector4 v) {
		                return second.apply( first.apply( v ) );		              
					}
            	};             	
            case 3:
            	return new ChainedTransformation() {

					@Override
					public final Vector4 apply(Vector4 v) {
		                return third.apply( second.apply( first.apply( v ) ) );		             
					}
            	};              	
            default:
                throw new RuntimeException("Unreachable code reached");
        }
    }    

    @Override
    public String toString()
    {
        switch( count ) 
        {
            case 0:
                return "{ <identity transform> }";
            case 1:
                return "{ "+first.toString()+" }";
            case 2:
                return "{ "+first.toString()+" , "+second.toString()+" }";
            case 3:
                return "{ "+first.toString()+" , "+second.toString()+" , "+third.toString()+" }";
            default:
                throw new RuntimeException("Unreachable code reached");
        }       
    }
    
    public Vector4 transform(Vector4 v) 
    {
    	return this.transform.apply( v );
    }    
    
    public Vector4 transformDirection(Vector4 v) 
    {
    	return firstRotation.apply( v );
    }

    public Vector4 transformNormal(Vector4 v) {
        return v.multiply( invTransposedMatrix );
    }
    
    protected abstract class ChainedTransformation {
    	
    	public abstract Vector4 apply(Vector4 v);
    }

    public Vector4 transformInverse(Vector4 v) 
    {
    	return inverseTransform.apply( v );
    }
    
    public AffineTransform getFirstRotation() {
		return firstRotation;
	}
}