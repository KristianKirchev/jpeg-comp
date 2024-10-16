package com.kris.jpeg_comp.comp;

import java.awt.*;
import java.awt.image.PixelGrabber;

public class ColorSpaceConverter {

    public Image imageObj;

    public int imageHeight;

    public int imageWidth;

    public ColorSpaceConverter(Image image)
    {
        imageObj = image;
        imageWidth = image.getWidth(null);
        imageHeight = image.getHeight(null);
    }

    ////////////////////////////////////////////////////////////////

    private int[] grabPixels() throws AWTException, InterruptedException {
        int[] values = new int[imageWidth * imageHeight];
        PixelGrabber grabber = new PixelGrabber(imageObj.getSource(), 0, 0, imageWidth, imageHeight, values, 0, imageWidth);

        if (!grabber.grabPixels()) {
            throw new AWTException("Failed to grab pixels. Status: " + grabber.status());
        }

        return values;
    }


    private int[][] extractColorArray(int colorShift) throws AWTException, InterruptedException {
        int[] values = grabPixels();
        int[][] colorArray = new int[imageWidth][imageHeight];

        int index = 0;
        for (int y = 0; y < imageHeight; ++y) {
            for (int x = 0; x < imageWidth; ++x) {
                colorArray[x][y] = (values[index] >> colorShift) & 0xFF;
                ++index;
            }
        }

        return colorArray;
    }


    public int[][] getRedArray() throws AWTException, InterruptedException {
        return extractColorArray(16);
    }

    public int[][] getGreenArray() throws AWTException, InterruptedException {
        return extractColorArray(8);
    }

    public int[][] getBlueArray() throws AWTException, InterruptedException {
        return extractColorArray(0);
    }

    ////////////////////////////////////////////////////////////////

    private int[][] convertRGBToYCbCr(int[][] redArray, int[][] greenArray, int[][] blueArray,
                                      float rFactor, float gFactor, float bFactor, float offset) {
        int[][] result = new int[imageWidth][imageHeight];

        for (int i = 0; i < imageWidth; i++) {
            for (int j = 0; j < imageHeight; j++) {
                result[i][j] = (int) ((rFactor * redArray[i][j]) +
                        (gFactor * greenArray[i][j]) +
                        (bFactor * blueArray[i][j]) + offset);
            }
        }

        return result;
    }


    private int[][] convertYCbCrToRGB(int[][] Y, int[][] Cb, int[][] Cr,
                                      float yFactor, float crFactor, float cbFactor) {

        float cOffset = 0.128f;

        int[][] result = new int[imageWidth][imageHeight];

        for (int i = 0; i < imageWidth; i++) {
            for (int j = 0; j < imageHeight; j++) {
                result[i][j] = (int) ((yFactor * Y[i][j]) +
                        (crFactor * (Cr[i][j] - cOffset)) +
                        (cbFactor * (Cb[i][j] - cOffset)));
            }
        }

        return result;
    }


    public int[][] convertRGBtoY(int[][] redArray, int[][] greenArray, int[][] blueArray) {
        return convertRGBToYCbCr(redArray, greenArray, blueArray, 0.299f, 0.587f, 0.114f, 0.0f);
    }

    public int[][] convertRGBtoCb(int[][] redArray, int[][] greenArray, int[][] blueArray) {
        return convertRGBToYCbCr(redArray, greenArray, blueArray, -0.169f, -0.331f, 0.5f, 128.0f);
    }

    public int[][] convertRGBtoCr(int[][] redArray, int[][] greenArray, int[][] blueArray) {
        return convertRGBToYCbCr(redArray, greenArray, blueArray, 0.500f, -0.419f, -0.081f, 128.0f);
    }


    public int[][] convertYCbCrToR(int[][] Y, int[][] Cb, int[][] Cr) {
        return convertYCbCrToRGB(Y, Cb, Cr, 1.0f, 0.0f, 1.403f);
    }

    public int[][] convertYCbCrToG(int[][] Y, int[][] Cb, int[][] Cr) {
        return convertYCbCrToRGB(Y, Cb, Cr, 1.0f, -0.344f, -0.714f);
    }

    public int[][] convertYCbCrToB(int[][] Y, int[][] Cb, int[][] Cr) {
        return convertYCbCrToRGB(Y, Cb, Cr, 1.0f, 1.773f, 0.0f);
    }

    ////////////////////////////////////////////////////////////////

    public int[] convertRGBtoArray(int[][] R, int[][] G, int[][] B, boolean log) {
        int index = 0;
        int[] array = new int[imageWidth * imageHeight];

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                array[index] = (0 << 24) |
                        (R[x][y] << 16) |
                        (G[x][y] << 8) |
                        (B[x][y]);
                index++;
            }

            if (log) {
                System.out.print("Blocks: " + index + "\r");
            }
        }

        return array;
    }

    public int[] convertGrayToArray(int[][] grayArray, boolean log) {
        int[] pixelArray = new int[imageWidth * imageHeight];
        int index = 0;

        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int grayValue = grayArray[x][y];
                pixelArray[index] = (0xFF << 24) |
                        (grayValue << 16) |
                        (grayValue << 8) |
                        grayValue;
                index++;
            }

            if (log) {
                System.out.print("Blocks: " + index + "\r");
            }
        }

        return pixelArray;
    }
}
