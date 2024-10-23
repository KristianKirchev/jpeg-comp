package com.kris.jpeg.comp;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Quantizator {

    private static final int[][] LUMINANCE_QUANTIZATION_MATRIX = {
            {6, 4, 4, 6, 10, 16, 20, 24},
            {5, 5, 6, 8, 10, 23, 24, 22},
            {6, 5, 6, 10, 16, 23, 28, 22},
            {6, 7, 9, 12, 20, 35, 32, 25},
            {7, 9, 15, 22, 27, 44, 41, 31},
            {10, 14, 22, 26, 32, 42, 45, 37},
            {20, 26, 31, 35, 41, 48, 48, 40},
            {29, 37, 38, 39, 45, 40, 41, 40}
    };

    private static final int[][] CHROMINANCE_QUANTIZATION_MATRIX = {
            {10, 8, 9, 9, 9, 8, 10, 9},
            {9, 9, 10, 10, 10, 11, 12, 17},
            {13, 12, 12, 12, 12, 20, 16, 16},
            {14, 17, 18, 20, 23, 23, 22, 20},
            {25, 25, 25, 25, 25, 25, 25, 25},
            {25, 25, 25, 25, 25, 25, 25, 25},
            {25, 25, 25, 25, 25, 25, 25, 25},
            {25, 25, 25, 25, 25, 25, 25, 25}
    };


    private static int[][] pickMatrix(int number) {
        if (number == 1) {
            return LUMINANCE_QUANTIZATION_MATRIX;
        }
        return CHROMINANCE_QUANTIZATION_MATRIX;
    }

    public static int[][] quantize(int[][] dctBlock, int blockSize, int matrixNum) {

        int[][] quantizationMatrix = pickMatrix(matrixNum);
        int[][] quantizedBlock = new int[blockSize][blockSize];

        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                quantizedBlock[i][j] = Math.round((float) dctBlock[i][j] / quantizationMatrix[i][j]);
            }
        }

        return quantizedBlock;
    }

    public static int[][] dequantize(int[][] quantizedBlock, int blockSize, int matrixNum) {

        int[][] quantizationMatrix = pickMatrix(matrixNum);

        int[][] dctBlock = new int[blockSize][blockSize];

        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                dctBlock[i][j] = (quantizedBlock[i][j] - 128) * quantizationMatrix[i][j];
            }
        }

        return dctBlock;
    }

}
