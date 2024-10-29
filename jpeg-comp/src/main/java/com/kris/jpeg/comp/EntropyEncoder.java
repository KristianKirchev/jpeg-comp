package com.kris.jpeg.comp;

import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class EntropyEncoder {
    private int[] entropyEncodedImageY;
    private int[] entropyEncodedImageU;
    private int[] entropyEncodedImageV;

    private int matrixHeight;
    private int matrixWidth;


    public void entropyEncoding(TrigonometricProcessor trigonometricProcessor) {
        entropyEncodedImageY = zigzagScan(trigonometricProcessor.getYDCTImage());
        entropyEncodedImageU = zigzagScan(trigonometricProcessor.getUDCTImage());
        entropyEncodedImageV = zigzagScan(trigonometricProcessor.getVDCTImage());
    }

    public List<BufferedImage> entropyDecoding() {
        return List.of(inverseZigzagScan(entropyEncodedImageY), inverseZigzagScan(entropyEncodedImageU), inverseZigzagScan(entropyEncodedImageV));
    }

    private int[] zigzagScan(BufferedImage image) {
        matrixHeight = image.getHeight();
        matrixWidth = image.getWidth();
        List<Integer> result = new ArrayList<>();

        //1. ->     2. \/   3. |/   4. /|
        int scenario = 1;
        int rows = 0, cols = 0;

        while (result.size() < matrixHeight * matrixWidth) {
            result.add(image.getRGB(cols, rows));

            if(scenario == 1) {
                cols++;
                if(rows == 0) {
                    scenario = 3;
                }
                else if(rows == matrixHeight - 1) {
                    scenario = 4;
                }
            }
            else if(scenario == 2) {
                rows++;

                if(cols == 0) {
                    scenario = 4;
                }
                else if(cols == matrixWidth - 1) {
                    scenario = 3;
                }
            }
            else if(scenario == 3) {
                rows++;
                cols--;
                if(cols == 0 && rows < matrixHeight - 1) {
                    scenario = 2;
                }
                else if(cols >= 0 && rows == matrixHeight - 1) {
                    scenario = 1;
                }
            }
            else if(scenario == 4) {
                rows--;
                cols++;
                if(rows == 0 && cols < matrixWidth - 1) {
                    scenario = 1;
                }
                else if(rows >= 0 && cols == matrixWidth - 1) {
                    scenario = 2;
                }
            }
        }
        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    private BufferedImage inverseZigzagScan(int[] data) {

        BufferedImage result = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_RGB);

        //1. ->     2. \/   3. |/   4. /|
        int scenario = 1;
        int rows = 0, cols = 0;

        for (int pixel : data) {
            result.setRGB(cols, rows, pixel);

            if(scenario == 1) {
                cols++;
                if(rows == 0) {
                    scenario = 3;
                }
                else if(rows == matrixHeight - 1) {
                    scenario = 4;
                }
            }
            else if(scenario == 2) {
                rows++;

                if(cols == 0) {
                    scenario = 4;
                }
                else if(cols == matrixWidth - 1) {
                    scenario = 3;
                }
            }
            else if(scenario == 3) {
                rows++;
                cols--;
                if(cols == 0 && rows < matrixHeight - 1) {
                    scenario = 2;
                }
                else if(cols >= 0 && rows == matrixHeight - 1) {
                    scenario = 1;
                }
            }
            else if(scenario == 4) {
                rows--;
                cols++;
                if(rows == 0 && cols < matrixWidth - 1) {
                    scenario = 1;
                }
                else if(rows >= 0 && cols == matrixWidth - 1) {
                    scenario = 2;
                }
            }
        }
        return result;
    }
}
