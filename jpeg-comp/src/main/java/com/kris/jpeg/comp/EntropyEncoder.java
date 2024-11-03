package com.kris.jpeg.comp;

import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class EntropyEncoder {
    private int[] zigzagImageY;
    private int[] zigzagImageU;
    private int[] zigzagImageV;

    private String rleImageY;
    private String rleImageU;
    private String rleImageV;

    private int matrixHeight;
    private int matrixWidth;


    public void entropyEncoding(TrigonometricProcessor trigonometricProcessor) {
        zigzagImageY = zigzagScan(trigonometricProcessor.getYDCTImage());
        zigzagImageU = zigzagScan(trigonometricProcessor.getUDCTImage());
        zigzagImageV = zigzagScan(trigonometricProcessor.getVDCTImage());

        rleImageY = rleProcess(zigzagImageY);
        rleImageU = rleProcess(zigzagImageU);
        rleImageV = rleProcess(zigzagImageV);

    }

    public List<BufferedImage> entropyDecoding() {
        zigzagImageY = inverseRleProcess(rleImageY);
        zigzagImageU = inverseRleProcess(rleImageU);
        zigzagImageV = inverseRleProcess(rleImageV);

        return List.of(inverseZigzagScan(inverseRleProcess(rleImageY)), inverseZigzagScan(zigzagImageU), inverseZigzagScan(zigzagImageV));
    }

    ////////////////////////////////////////////////////////////////////////

    private List<Integer> helpIteratePixels(int rows, int cols, int scenario) {
        switch (scenario) {
            case 1: {
                cols++;
                if (rows == 0) {
                    scenario = 3;
                } else if (rows == matrixHeight - 1) {
                    scenario = 4;
                }
                break;
            }
            case 2: {
                rows++;

                if (cols == 0) {
                    scenario = 4;
                } else if (cols == matrixWidth - 1) {
                    scenario = 3;
                }
                break;
            }
            case 3: {
                rows++;
                cols--;
                if (cols == 0 && rows < matrixHeight - 1) {
                    scenario = 2;
                } else if (cols >= 0 && rows == matrixHeight - 1) {
                    scenario = 1;
                }
                break;
            }
            case 4: {
                rows--;
                cols++;
                if (rows == 0 && cols < matrixWidth - 1) {
                    scenario = 1;
                } else if (rows >= 0 && cols == matrixWidth - 1) {
                    scenario = 2;
                }
                break;
            }
        }

        return List.of(rows, cols, scenario);
    }

    private int[] zigzagScan(BufferedImage image) {
        matrixHeight = image.getHeight();
        matrixWidth = image.getWidth();
        List<Integer> result = new ArrayList<>();

        //1. ->     2. \/   3. |/   4. /|
        int scenario = 1;
        int rows = 0, cols = 0;

        while (result.size() < matrixHeight * matrixWidth) {
            result.add(((image.getRGB(cols, rows) >> 16) & 0xFF) - 128);

            List<Integer> vars = helpIteratePixels(rows, cols, scenario);

            rows = vars.get(0);
            cols = vars.get(1);
            scenario = vars.get(2);
        }

        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    private BufferedImage inverseZigzagScan(int[] data) {

        BufferedImage result = new BufferedImage(matrixWidth, matrixHeight, BufferedImage.TYPE_INT_RGB);

        //1. ->     2. \/   3. |/   4. /|
        int scenario = 1;
        int rows = 0, cols = 0;

        for (int pixel : data) {
            int value = pixel + 128;

            int grayscale = value << 16 | value << 8 | value;

            result.setRGB(cols, rows, grayscale);

            List<Integer> vars = helpIteratePixels(rows, cols, scenario);

            rows = vars.get(0);
            cols = vars.get(1);
            scenario = vars.get(2);
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////

    private String rleProcess(int[] dctChannel) {

        String rle = "";

        int currEl = dctChannel[0];
        int repeat = 1;

        for (int i = 1; i < dctChannel.length; i++) {
            if (currEl == dctChannel[i]) {
                repeat++;
            }
            else {
                rle += "|" + currEl + (repeat == 1 ? "" : ("," + repeat));
                currEl = dctChannel[i];
                repeat = 1;
            }
        }

        rle += "|" + currEl + (repeat == 1 ? "" : ("," + repeat));

        return rle + "|";
    }

    private int[] inverseRleProcess(String rleString) {

        List<Integer> result = new ArrayList<>();

        String[] sepParts = rleString.split("[|]");

        for (String sepPart : sepParts) {
            if (sepPart.isEmpty()) {
                continue;
            }

            if (!sepPart.contains(",")) {
                result.add(Integer.parseInt(sepPart));
            }
            else {
                String[] split = sepPart.split("[,]");

                for (int i = 0; i < Integer.parseInt(split[1]); i++) {
                    result.add(Integer.parseInt(split[0]));
                }
            }
        }

        return result.stream().mapToInt(Integer::intValue).toArray();
    }
}
