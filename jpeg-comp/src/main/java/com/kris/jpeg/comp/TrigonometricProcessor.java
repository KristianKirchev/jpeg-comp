package com.kris.jpeg.comp;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.List;

@NoArgsConstructor
@Getter
public class TrigonometricProcessor {
    private final int BLOCK_SIZE = 8;
    private final double COEF1 = (1 / Math.sqrt(2));
    private final double COEF2 = 1;
    private BufferedImage yDCTImage;
    private BufferedImage uDCTImage;
    private BufferedImage vDCTImage;

    private int[][] performDCT(int[][] block, int height, int width, int quantMatrixNum) {
        int[][] dctBlock = new int[height][width];
        for (int u = 0; u < height; u++) {
            for (int v = 0; v < width; v++) {
                double au = (u == 0) ? COEF1 : COEF2;
                double av = (v == 0) ? COEF1 : COEF2;

                double sum = 0.0;
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        sum += (double)(block[y][x] - 128) *
                                Math.cos(((2 * x + 1) * u * Math.PI) / (2.0 * width))
                                * Math.cos(((2 * y + 1) * v * Math.PI) / (2.0 * height));
                    }
                }
                dctBlock[u][v] = (int)Math.round((( 2.0 / (Math.sqrt(height * width))) * au * av * sum));
            }
        }
        return Quantizator.quantize(dctBlock, height, width, quantMatrixNum);
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

                int actualBlockWidth = Math.min(BLOCK_SIZE, width - x);
                int actualBlockHeight = Math.min(BLOCK_SIZE, height - y);

                int[][] yBlock = new int[actualBlockHeight][actualBlockWidth];
                int[][] uBlock = new int[actualBlockHeight][actualBlockWidth];
                int[][] vBlock = new int[actualBlockHeight][actualBlockWidth];

                for (int i = 0; i < actualBlockHeight; i++) {
                    for (int j = 0; j < actualBlockWidth; j++) {
                        if (x + j < width && y + i < height) {
                            int[] yuv = raster.getPixel(x + j, y + i, (int[]) null);

                            yBlock[i][j] = yuv[0];
                            uBlock[i][j] = yuv[1];
                            vBlock[i][j] = yuv[2];
                        }
                    }
                }

                int[][] dctY = performDCT(yBlock, actualBlockHeight, actualBlockWidth, 1);
                int[][] dctU = performDCT(uBlock, actualBlockHeight, actualBlockWidth,2);
                int[][] dctV = performDCT(vBlock, actualBlockHeight, actualBlockWidth,2);

                saveBlockToImage(yDCTImage, dctY, actualBlockHeight, actualBlockWidth, x, y);
                saveBlockToImage(uDCTImage, dctU, actualBlockHeight, actualBlockWidth, x, y);
                saveBlockToImage(vDCTImage, dctV, actualBlockHeight, actualBlockWidth, x, y);
            }
        }
    }

    private void saveBlockToImage(BufferedImage image, int[][] block, int height, int width, int startX, int startY) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
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

    private int[][] performIDCT(int[][] dctBlock, int height, int width, int quantMatrixNum) {

        dctBlock = Quantizator.dequantize(dctBlock, height, width, quantMatrixNum);

        int[][] block = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double sum = 0.0;
                for (int u = 0; u < height; u++) {
                    for (int v = 0; v < width; v++) {
                        double au = (u == 0) ? COEF1 : COEF2;
                        double av = (v == 0) ? COEF1 : COEF2;
                        sum += au * av * dctBlock[u][v] *
                                Math.cos(((2 * x + 1) * u * Math.PI) / (2.0 * width)) *
                                Math.cos(((2 * y + 1) * v * Math.PI) / (2.0 * height));
                    }
                }

                block[y][x] = Math.round(Math.round((2.0 / (Math.sqrt(width * height))) * sum)) + 128;
            }
        }
        return block;
    }

    public BufferedImage reconstructImage(List<BufferedImage> DCTData) {
        int width = DCTData.getFirst().getWidth();
        int height = DCTData.get(0).getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y += BLOCK_SIZE) {
            for (int x = 0; x < width; x += BLOCK_SIZE) {
                int actualBlockWidth = Math.min(BLOCK_SIZE, width - x);
                int actualBlockHeight = Math.min(BLOCK_SIZE, height - y);

                int[][] dctY = extractBlockFromImage(DCTData.get(0), actualBlockHeight, actualBlockWidth, x, y);
                int[][] dctU = extractBlockFromImage(DCTData.get(1), actualBlockHeight, actualBlockWidth,x, y);
                int[][] dctV = extractBlockFromImage(DCTData.get(2), actualBlockHeight, actualBlockWidth,x, y);

                int[][] yBlock = performIDCT(dctY, actualBlockHeight, actualBlockWidth, 1);
                int[][] uBlock = performIDCT(dctU, actualBlockHeight, actualBlockWidth, 2);
                int[][] vBlock = performIDCT(dctV, actualBlockHeight, actualBlockWidth, 2);

                for (int i = 0; i < actualBlockHeight; i++) {
                    for (int j = 0; j < actualBlockWidth; j++) {
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

    private int[][] extractBlockFromImage(BufferedImage image, int height, int width, int startX, int startY) {
        int[][] block = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
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
