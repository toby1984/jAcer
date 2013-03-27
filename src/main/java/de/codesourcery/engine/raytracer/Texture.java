package de.codesourcery.engine.raytracer;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Texture
{
    private final int maxX;
    private final int maxY;
    
    private final BufferedImage image;
    
    public static Texture load(File file) throws IOException {
        return new Texture( ImageIO.read( file ) );
    }
    
    public Texture(BufferedImage image) 
    {
        this.image = image;
        this.maxX= image.getWidth()-1;
        this.maxY = image.getHeight()-1;
    }
    
    public Vector4 getColorAt(double u,double v) {
        
        final double u1 = Math.abs( u ) % 1.0d;
        final double v1 = Math.abs( v ) % 1.0d;
        
        int x = (int) Math.round( maxX * u1 );
        int y = (int) Math.round( maxY * v1 );
        
        final int rgb= image.getRGB( x , y );

        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        return new Vector4( r/255.0d ,  g/255.0d , b/255.0d );
    }
}
