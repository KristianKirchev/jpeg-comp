package com.kris.jpeg.comp;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

@NoArgsConstructor
@Getter
public class TrigonometricProcessor {
    private final int BLOCK_SIZE = 8;
    private final double COEF1 = (1 / Math.sqrt(2));
    private final double COEF2 = 1;//(Math.sqrt( 2.0 / BLOCK_SIZE));
    private BufferedImage yDCTImage;
    private BufferedImage uDCTImage;
    private BufferedImage vDCTImage;

    private int[][] performDCT(int[][] block, int quantMatrixNum) {
        int[][] dctBlock = new int[BLOCK_SIZE][BLOCK_SIZE];
        for (int u = 0; u < BLOCK_SIZE; u++) {
            for (int v = 0; v < BLOCK_SIZE; v++) {
                double au = (u == 0) ? COEF1 : COEF2;
                double av = (v == 0) ? COEF1 : COEF2;

                double sum = 0.0;
                for (int x = 0; x < BLOCK_SIZE; x++) {
                    for (int y = 0; y < BLOCK_SIZE; y++) {
                        sum += (double)(block[x][y] - 128) *
                                Math.cos(((2 * x + 1) * u * Math.PI) / (2.0 * BLOCK_SIZE))
                                * Math.cos(((2 * y + 1) * v * Math.PI) / (2.0 * BLOCK_SIZE));
                    }
                }
                dctBlock[u][v] = (int)Math.round((( 2.0 / BLOCK_SIZE) * au * av * sum));
            }
        }
//        return dctBlock;
        return Quantizator.quantize(dctBlock, BLOCK_SIZE, quantMatrixNum);
    }

    public void processImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        yDCTImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        uDCTImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        vDCTImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        Raster raster = image.getRaster();

        for (int y = 0; y < height; y += BLOCK_SIZE) {
            for (int x = 0; x < width; x += BLOCK_SIZE) {
                int[][] yBlock = new int[BLOCK_SIZE][BLOCK_SIZE];
                int[][] uBlock = new int[BLOCK_SIZE][BLOCK_SIZE];
                int[][] vBlock = new int[BLOCK_SIZE][BLOCK_SIZE];

                for (int i = 0; i < BLOCK_SIZE; i++) {
                    for (int j = 0; j < BLOCK_SIZE; j++) {
                        if (x + j < width && y + i < height) {
                            int[] yuv = raster.getPixel(x + j, y + i, (int[]) null);

                            yBlock[i][j] = yuv[0];
                            uBlock[i][j] = yuv[1];
                            vBlock[i][j] = yuv[2];
                        }
                    }
                }

                int[][] dctY = performDCT(yBlock, 1);
                int[][] dctU = performDCT(uBlock, 2);
                int[][] dctV = performDCT(vBlock, 2);

                saveBlockToImage(yDCTImage, dctY, x, y);
                saveBlockToImage(uDCTImage, dctU, x, y);
                saveBlockToImage(vDCTImage, dctV, x, y);
            }
        }
    }

    private void saveBlockToImage(BufferedImage image, int[][] block, int startX, int startY) {
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                int x = startX + j;
                int y = startY + i;

                if (x < image.getWidth() && y < image.getHeight()) {
                    int value = clamp(block[i][j] + 128);

                    int grayscale = value << 16 | value << 8 | value;
                    image.setRGB(x, y, grayscale);
                }
            }
        }
    }

    //////////////////////////////////////////////////////

    private int[][] performIDCT(int[][] dctBlock, int quantMatrixNum) {

        dctBlock = Quantizator.dequantize(dctBlock, BLOCK_SIZE, quantMatrixNum);

        int[][] block = new int[BLOCK_SIZE][BLOCK_SIZE];

        for (int x = 0; x < BLOCK_SIZE; x++) {
            for (int y = 0; y < BLOCK_SIZE; y++) {
                double sum = 0.0;
                for (int u = 0; u < BLOCK_SIZE; u++) {
                    for (int v = 0; v < BLOCK_SIZE; v++) {
                        double au = (u == 0) ? COEF1 : COEF2;
                        double av = (v == 0) ? COEF1 : COEF2;
                        sum += au * av * dctBlock[u][v] *
                                Math.cos(((2 * x + 1) * u * Math.PI) / (2.0 * BLOCK_SIZE)) *
                                Math.cos(((2 * y + 1) * v * Math.PI) / (2.0 * BLOCK_SIZE));
                    }
                }

                block[x][y] = Math.round(Math.round((2.0 / BLOCK_SIZE) * sum)) + 128;
            }
        }
        return block;
    }

    public BufferedImage reconstructImage(BufferedImage yDCTImage, BufferedImage uDCTImage, BufferedImage vDCTImage) {
        int width = yDCTImage.getWidth();
        int height = yDCTImage.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y += BLOCK_SIZE) {
            for (int x = 0; x < width; x += BLOCK_SIZE) {
                int[][] dctY = extractBlockFromImage(yDCTImage, x, y);
                int[][] dctU = extractBlockFromImage(uDCTImage, x, y);
                int[][] dctV = extractBlockFromImage(vDCTImage, x, y);

                int[][] yBlock = performIDCT(dctY, 1);
                int[][] uBlock = performIDCT(dctU, 2);
                int[][] vBlock = performIDCT(dctV, 2);

                for (int i = 0; i < BLOCK_SIZE; i++) {
                    for (int j = 0; j < BLOCK_SIZE; j++) {
                        if (x + j < width && y + i < height) {
                            int yVal = clamp(yBlock[i][j]);
                            int uVal = clamp(uBlock[i][j]);
                            int vVal = clamp(vBlock[i][j]);

                            image.setRGB(x + j, y + i, new Color(yVal, uVal, vVal).getRGB());
                        }
                    }
                }
            }
        }

        return image;
    }

    private int[][] extractBlockFromImage(BufferedImage image, int startX, int startY) {
        int[][] block = new int[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {
            for (int j = 0; j < BLOCK_SIZE; j++) {
                int x = startX + j;
                int y = startY + i;

                if (x < image.getWidth() && y < image.getHeight()) {
                    int gray = new Color(image.getRGB(x, y)).getRed();
                    block[i][j] = gray;
                }
            }
        }
        return block;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

}
