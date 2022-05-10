package edu.ufl.cise.plc.runtime;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class ImageOpsAdd {
    public static boolean equals(BufferedImage image0, BufferedImage image1) {
        int[] pixels0 = image0.getRGB(0,0,image0.getWidth(), image0.getHeight(), null,0,image0.getWidth());
        int[] pixels1 = image1.getRGB(0,0,image1.getWidth(), image1.getHeight(), null,0,image1.getWidth());
        return Arrays.equals(pixels0, pixels1);
    }

    public static BufferedImage setAllPixels(BufferedImage image, ColorTuple c) {
        for (int x = 0; x < image.getWidth(); x++)
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y,  c.pack());
            }
        return image;
    }

    public static BufferedImage setAllPixels(BufferedImage image, ColorTupleFloat c) {
        for (int x = 0; x < image.getWidth(); x++)
            for (int y = 0; y < image.getHeight(); y++) {
                image.setRGB(x, y,  c.pack());
            }
        return image;
    }

    public static void setColor(BufferedImage image, int x, int y, ColorTuple c) {
        image.setRGB(x, y, c.pack());
    }

    public static void setColor(BufferedImage image, int x, int y, ColorTupleFloat c) {
        image.setRGB(x, y, c.pack());
    }

    public static BufferedImage makeConstantImage(int width, int height, ColorTuple c) {
        BufferedImage image = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
        return setAllPixels(image, c);
    }

    public static BufferedImage makeConstantImage(int width, int height, ColorTupleFloat c) {
        BufferedImage image = new BufferedImage(width, height,BufferedImage.TYPE_INT_RGB);
        return setAllPixels(image, c);
    }
}


