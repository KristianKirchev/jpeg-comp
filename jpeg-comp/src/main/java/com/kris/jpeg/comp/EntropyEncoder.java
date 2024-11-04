package com.kris.jpeg.comp;

import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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

    private Map<String, String> codesY;
    private Map<String, String> codesU;
    private Map<String, String> codesV;

    private String encodedY;
    private String encodedU;
    private String encodedV;


    public void entropyEncoding(TrigonometricProcessor trigonometricProcessor) throws IOException {

        codesY = new HashMap<>();
        codesU = new HashMap<>();
        codesV = new HashMap<>();

        zigzagImageY = zigzagScan(trigonometricProcessor.getYDCTImage());
        zigzagImageU = zigzagScan(trigonometricProcessor.getUDCTImage());
        zigzagImageV = zigzagScan(trigonometricProcessor.getVDCTImage());

        rleImageY = rleProcess(zigzagImageY, "Y");
        rleImageU = rleProcess(zigzagImageU, "U");
        rleImageV = rleProcess(zigzagImageV, "V");

        encodedY = huffmanEncode(rleImageY, codesY, "Y");
        encodedU = huffmanEncode(rleImageU, codesU, "U");
        encodedV = huffmanEncode(rleImageV, codesV, "V");
    }

    public List<BufferedImage> entropyDecoding() {

        rleImageY = huffmanDecoding(codesY, encodedY);
        rleImageU = huffmanDecoding(codesU, encodedU);
        rleImageV = huffmanDecoding(codesV, encodedV);

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

    private String rleProcess(int[] dctChannel, String file) throws IOException {

        StringBuilder rle = new StringBuilder();

        int currEl = dctChannel[0];
        int repeat = 1;

        for (int i = 1; i < dctChannel.length; i++) {
            if (currEl == dctChannel[i]) {
                repeat++;
            }
            else {
                rle.append("|").append(currEl).append(repeat == 1 ? "" : ("," + repeat));
                currEl = dctChannel[i];
                repeat = 1;
            }
        }

        rle.append("|").append(currEl).append(repeat == 1 ? "" : ("," + repeat)).append("|");

        String fileName = "target/output-images/step-6-rleProcessed" + file + ".bin";

        FileWriter myWriter = new FileWriter(fileName);
        myWriter.write(String.valueOf(rle));
        myWriter.close();

        return String.valueOf(rle);
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

    ////////////////////////////////////////////////////////////////////////

    private void countFrequencies(String word, List<HuffNode> huffNodes) {
        Map<Character, Integer> frequencies = new HashMap<>();
        for (char charValue : word.toCharArray()) {
            if (!frequencies.containsKey(charValue)) {
                int freq = 0;
                for (char ch : word.toCharArray()) {
                    if (ch == charValue) {
                        freq++;
                    }
                }
                frequencies.put(charValue, freq);
                huffNodes.add(new HuffNode(String.valueOf(charValue), freq));
            }
        }
    }

    private HuffNode buildHuffTree(List<HuffNode> huffNodes) {
        while (huffNodes.size() > 1) {
            huffNodes.sort(Comparator.comparingInt(a -> a.freq));
            HuffNode left = huffNodes.removeFirst();
            HuffNode right = huffNodes.removeFirst();

            HuffNode merged = new HuffNode(left.freq + right.freq);
            merged.left = left;
            merged.right = right;

            huffNodes.add(merged);
        }
        return huffNodes.getFirst();
    }

    private void generateHuffmanCodes(HuffNode node, String currentCode, Map<String, String> codes) {
        if (node == null) {
            return;
        }

        if (!node.getCharValue().equals("\0")) {
            codes.put(node.getCharValue(), currentCode);
        }

        generateHuffmanCodes(node.left, currentCode + '0', codes);
        generateHuffmanCodes(node.right, currentCode + '1', codes);
    }

    private void huffmanEncoding(String word, List<HuffNode> huffNodes, Map<String, String> codes) {
        huffNodes.clear();
        countFrequencies(word, huffNodes);
        HuffNode root = buildHuffTree(huffNodes);
        generateHuffmanCodes(root, "", codes);

    }

    private String huffmanEncode(String rleImage, Map<String, String> codes, String file) throws IOException {

        List<HuffNode> huffNodes = new ArrayList<>();

        huffmanEncoding(rleImage, huffNodes, codes);
        StringBuilder encodedWord = new StringBuilder();
        for (char charValue : rleImage.toCharArray()) {
            encodedWord.append(codes.get(String.valueOf(charValue)));
        }

        String fileName = "target/output-images/step-6-huffEncoded" + file + ".bin";
        FileWriter myWriter = new FileWriter(fileName);
        myWriter.write(String.valueOf(encodedWord));
        myWriter.close();

        return String.valueOf(encodedWord);
    }

    public String huffmanDecoding(Map<String, String> codes, String encodedWord) {

        StringBuilder currentCode = new StringBuilder();
        StringBuilder decodedChars = new StringBuilder();

        Map<String, String> codeToChar = new HashMap<>();
        for (Map.Entry<String, String> entry : codes.entrySet()) {
            codeToChar.put(entry.getValue(), entry.getKey());
        }

        for (char bit : encodedWord.toCharArray()) {
            currentCode.append(bit);
            if (codeToChar.containsKey(currentCode.toString())) {
                decodedChars.append(codeToChar.get(currentCode.toString()));
                currentCode.setLength(0);
            }
        }

        return decodedChars.toString();
    }
}
