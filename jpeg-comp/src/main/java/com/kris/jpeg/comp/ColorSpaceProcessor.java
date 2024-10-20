package com.kris.jpeg.comp;

import java.awt.image.BufferedImage;

public class ColorSpaceProcessor {

    public ColorSpaceProcessor() {}

    private static int getRGBComponentComp(int rgb, int shift) {
        return (rgb >> shift) & 0xFF;
    }

    private static int getYUVComponentComp(int r, int g, int b, float yFactor, float uFactor, float vFactor, int offset) {
        return Math.max(0, Math.min(255, (int)((yFactor * r + uFactor * g + vFactor * b) + offset)));
    }

    private static int getYUVComponentDecomp(int yuv, int shift) {
        return getRGBComponentComp(yuv, shift); //+ offset;
    }

    private static int getRGBComponentDecomp(int y, int u, int v, float gFactor, float bFactor) {
        return Math.max(0, Math.min(255, (int)(/*rFactor*/ 1.0f * (y - 16) + gFactor * (u - 128) + bFactor * (v - 128))));
    }

    private static int getYuv(int rgb) {

        int r = getRGBComponentComp(rgb, 16);
        int g = getRGBComponentComp(rgb, 8);
        int b = getRGBComponentComp(rgb, 0);

        int Y = getYUVComponentComp(r, g, b, 0.257f, 0.504f, 0.098f, 16);
        int U = getYUVComponentComp(r, g, b, -0.148f, -0.291f, 0.439f, 128);
        int V = getYUVComponentComp(r, g, b, 0.439f, -0.368f, -0.071f, 128);

        return (Y << 16) | (U << 8) | V;
    }

    private static int getRgb(int yuv) {
        int Y = getYUVComponentDecomp(yuv, 16);
        int U = getYUVComponentDecomp(yuv, 8);
        int V = getYUVComponentDecomp(yuv, 0);

        int r = getRGBComponentDecomp(Y, U, V, 0.0f, 1.402f);
        int g = getRGBComponentDecomp(Y, U, V, -0.344f, -0.714f);
        int b = getRGBComponentDecomp(Y, U, V, 1.772f, 0.0f);

        return ((r << 16) | (g << 8) | b);
    }

    public static BufferedImage convertRGBToYUV(BufferedImage rgbImage) {
        int width = rgbImage.getWidth();
        int height = rgbImage.getHeight();
        BufferedImage yuvImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = rgbImage.getRGB(x, y);

                int yuv = getYuv(rgb);
                yuvImage.setRGB(x, y, yuv);
            }
        }

//        int[] pixels = ((DataBufferInt) yuvImage.getRaster().getDataBuffer()).getData();
//
//        for (int i = 0; i < pixels.length; i++) {
//            System.out.printf("%08X ", pixels[i]);
//
//            if ((i + 1) % 8 == 0) {
//                System.out.println();
//            }
//        }

        return yuvImage;
    }

    public static BufferedImage convertYUVToRGB(BufferedImage yuvImage) {
        int width = yuvImage.getWidth();
        int height = yuvImage.getHeight();
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int yuv = yuvImage.getRGB(x, y);

                int rgb = getRgb(yuv);
                rgbImage.setRGB(x, y, rgb);
            }
        }

//        int[] pixels = ((DataBufferInt) rgbImage.getRaster().getDataBuffer()).getData();
//
//        for (int i = 0; i < pixels.length; i++) {
//            System.out.printf("%08X ", pixels[i]);
//
//            if ((i + 1) % 8 == 0) {
//                System.out.println();
//            }
//        }

        return rgbImage;
    }
}
