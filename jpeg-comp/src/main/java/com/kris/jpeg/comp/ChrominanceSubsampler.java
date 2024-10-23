package com.kris.jpeg.comp;

import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

@NoArgsConstructor
public class ChrominanceSubsampler {

    private float[][] downsampleChannel(float[][] channel, int width, int height) {
        int newWidth = width / 2;
        int newHeight = height / 2;
        float[][] downsampledChannel = new float[newHeight][newWidth];

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int x2 = x * 2;
                int y2 = y * 2;
                downsampledChannel[y][x] = (channel[y2][x2] + channel[y2][x2 + 1] +
                        channel[y2 + 1][x2] + channel[y2 + 1][x2 + 1]) / 4.0f;
            }
        }

        return downsampledChannel;
    }

    public BufferedImage downsample(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        Raster raster = image.getRaster();

        float[][] U = new float[height][width];
        float[][] V = new float[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] yuv = raster.getPixel(x, y, (int[]) null);
                U[y][x] = yuv[1];
                V[y][x] = yuv[2];
            }
        }

        float[][] downsampledU = downsampleChannel(U, width, height);
        float[][] downsampledV = downsampleChannel(V, width, height);

        BufferedImage downsampledYUVImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int downsampledX = x / 2;
                int downsampledY = y / 2;

                // Check bounds
                if (downsampledY < downsampledU.length && downsampledX < downsampledU[0].length) {
                    int Y = raster.getPixel(x, y, (int[]) null)[0];
                    float u = downsampledU[downsampledY][downsampledX];
                    float v = downsampledV[downsampledY][downsampledX];

                    downsampledYUVImage.getRaster().setPixel(x, y, new int[]{Y, (int) u, (int) v});
                }

            }
        }

        return downsampledYUVImage;
    }

    public BufferedImage upsample(BufferedImage downsampledYUVImage) {
        int width = downsampledYUVImage.getWidth();
        int height = downsampledYUVImage.getHeight();

        Raster raster = downsampledYUVImage.getRaster();

        BufferedImage upsampledYUVImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster upsampledRaster = upsampledYUVImage.getRaster();

        float scaleFactor = 1.01f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] yuv = raster.getPixel(x, y, (int[]) null);
                int Y = yuv[0];

                float u = Math.min(yuv[1] * scaleFactor, 255);
                float v = Math.min(yuv[2] * scaleFactor, 255);

                upsampledRaster.setPixel(x, y, new int[]{Y, (int) u, (int) v});
            }
        }
        return upsampledYUVImage;
    }
}
