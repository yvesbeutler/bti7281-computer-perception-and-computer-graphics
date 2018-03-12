package ch.yvesbeutler.billard;

import java.awt.Color;
import java.io.File;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.PNG_Writer;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

public class Billard_Tracker implements PlugInFilter
{
    public int setup(String arg, ImagePlus imp)
    {   return DOES_8G;
    }

    public void run(ImageProcessor ip1)
    {   int w1 = ip1.getWidth();
        int h1 = ip1.getHeight();
        byte[] pix1 = (byte[]) ip1.getPixels();

        ImagePlus imgGray = NewImage.createByteImage("GrayDeBayered", w1/2, h1/2, 1, NewImage.FILL_BLACK);
        ImageProcessor ipGray = imgGray.getProcessor();
        byte[] pixGray = (byte[]) ipGray.getPixels();
        int w2 = ipGray.getWidth();
        int h2 = ipGray.getHeight();
        
        ImagePlus imgRGB = NewImage.createRGBImage("RGBDeBayered", w1/2, h1/2, 1, NewImage.FILL_BLACK);
        ImageProcessor ipRGB = imgRGB.getProcessor();
        int[] pixRGB = (int[]) ipRGB.getPixels();
        
        long msStart = System.currentTimeMillis();
        
        ImagePlus imgHue = NewImage.createByteImage("Hue", w1/2, h1/2, 1, NewImage.FILL_BLACK);
        ImageProcessor ipHue = imgHue.getProcessor();
        byte[] pixHue = (byte[]) ipHue.getPixels();
        
        int i1 = 0, i2 = 0;
        
        for (int y=0; y < h2; y++)
        {   
            for (int x=0; x<w2; x++)
            {

                // use debayering algorithm to colorize image
                debayerize(pix1, ipRGB, x, y, w1);

            }
        }

        long ms = System.currentTimeMillis() - msStart;
        System.out.println(ms);
        ImageStatistics stats = ipGray.getStatistics();
        System.out.println("Mean:" + stats.mean);
        
        PNG_Writer png = new PNG_Writer();
        try
        {   png.writeImage(imgRGB , "./resources/Billard1024x544x3.png",  0);
            png.writeImage(imgHue,  "./resources/Billard1024x544x1H.png", 0);
            png.writeImage(imgGray, "./resources/Billard1024x544x1B.png", 0);
            
        } catch (Exception e)
        {   e.printStackTrace();
        }
        
        imgGray.show();
        imgGray.updateAndDraw();
        imgRGB.show();
        imgRGB.updateAndDraw();
        imgHue.show();
        imgHue.updateAndDraw();
    }

    /**
     * Use the debayer algorithm to colorize the image. A full grid consists of
     * four pixel (two green because of the increased sensitivity of the human eye)
     *
     *      |
     *   G  |  B
     * -----|-----
     *   R  |  G
     *      |
     */
    private void debayerize(byte[] pix1, ImageProcessor ipRGB, int x, int y, int w1) {

        // determine position on original image
        int position = 2 * (x + (y * w1));

        // get rgb color values
        int green1 = pix1[position] & 0xff;
        int blue = pix1[position + 1] & 0xff;
        int red = pix1[position + w1] & 0xff;
        int green2 = pix1[position + w1 + 1] & 0xff;

        // simply get mean for green
        int green = (green1 + green2) / 2;

        Color color = new Color(red, green, blue);

        // draw single pixel
        ipRGB.setColor(color);
        ipRGB.drawPixel(x, y);
    }

    public static void main(String[] args)
    {
        Billard_Tracker plugin = new Billard_Tracker();

        // load resource file
        ClassLoader classLoader = new Billard_Tracker().getClass().getClassLoader();
        File file = new File(classLoader.getResource("Billard2048x1088x1.png").getFile());

        ImagePlus im = new ImagePlus(file.getPath());

        im.show();
        plugin.setup("", im);
        plugin.run(im.getProcessor());
    }
}
