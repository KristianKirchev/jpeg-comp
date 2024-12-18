package com.kris.jpeg.comp;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Quantizator {

//    private static final int[][] LUMINANCE_QUANTIZATION_MATRIX = {
//            {6, 4, 4, 6, 10, 16, 20, 24},
//            {5, 5, 6, 8, 10, 23, 24, 22},
//            {6, 5, 6, 10, 16, 23, 28, 22},
//            {6, 7, 9, 12, 20, 35, 32, 25},
//            {7, 9, 15, 22, 27, 44, 41, 31},
//            {10, 14, 22, 26, 32, 42, 45, 37},
//            {20, 26, 31, 35, 41, 48, 48, 40},
//            {29, 37, 38, 39, 45, 40, 41, 40}
//    };
    private static final int[][] LUMINANCE_QUANTIZATION_MATRIX = {
            {16, 11, 10, 16, 24, 40, 51, 61},
            {12, 12, 14, 19, 26, 58, 60, 55},
            {14, 13, 16, 24, 40, 57, 69, 56},
            {14, 17, 22, 29, 51, 87, 80, 62},
            {18, 22, 37, 56, 68, 109, 103, 77},
            {24, 35, 55, 64, 81, 104, 113, 92},
            {49, 64, 78, 87, 103, 121, 120, 101},
            {72, 92, 95, 98, 112, 100, 103, 99}
    };

//    private static final int[][] CHROMINANCE_QUANTIZATION_MATRIX = {
//            {10, 8, 9, 9, 9, 8, 10, 9},
//            {9, 9, 10, 10, 10, 11, 12, 17},
//            {13, 12, 12, 12, 12, 20, 16, 16},
//            {14, 17, 18, 20, 23, 23, 22, 20},
//            {25, 25, 25, 25, 25, 25, 25, 25},
//            {25, 25, 25, 25, 25, 25, 25, 25},
//            {25, 25, 25, 25, 25, 25, 25, 25},
//            {25, 25, 25, 25, 25, 25, 25, 25}
//    };
    private static final int[][] CHROMINANCE_QUANTIZATION_MATRIX = {
            {17, 18, 25, 47, 99, 99, 99, 99},
            {18, 21, 26, 66, 99, 99, 99, 99},
            {24, 26, 56, 99, 99, 99, 99, 99},
            {47, 66, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
            {99, 99, 99, 99, 99, 99, 99, 99},
    };


    private static int[][] pickMatrix(int number) {
        if (number == 1) {
            return LUMINANCE_QUANTIZATION_MATRIX;
        }
        return CHROMINANCE_QUANTIZATION_MATRIX;
    }

    public static int[][] quantize(int[][] dctBlock, int height, int width, int matrixNum) {

        int[][] quantizationMatrix = pickMatrix(matrixNum);
        int[][] quantizedBlock = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                quantizedBlock[i][j] = Math.round((float) dctBlock[i][j] / quantizationMatrix[i][j]);
            }
        }

        return quantizedBlock;
    }

    public static int[][] dequantize(int[][] quantizedBlock, int height, int width, int matrixNum) {

        int[][] quantizationMatrix = pickMatrix(matrixNum);

        int[][] dctBlock = new int[height][width];

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                dctBlock[i][j] = (quantizedBlock[i][j] - 128) * quantizationMatrix[i][j];
            }
        }

        return dctBlock;
    }

}
