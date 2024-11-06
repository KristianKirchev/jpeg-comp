package com.kris.jpeg.comp;

import lombok.NoArgsConstructor;

import java.awt.image.BufferedImage;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        System.out.println("FINAL IMAGE SIZE: " + (int)(Math.ceil(encodedY.length() / 8.0) + Math.ceil(encodedU.length() / 8.0) + Math.ceil(encodedV.length() / 8.0)) + " BYTES!!!");
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

//    private String rleProcess(int[] dctChannel, String file) throws IOException {
//
//        StringBuilder rle = new StringBuilder();
//
//        int currEl = dctChannel[0];
//        int repeat = 1;
//
//        for (int i = 1; i < dctChannel.length; i++) {
//            if (currEl == dctChannel[i]) {
//                repeat++;
//            }
//            else {
//                rle.append("|").append(currEl).append(repeat == 1 ? "" : repeat == 2 ? "," : ("," + repeat));
//                currEl = dctChannel[i];
//                repeat = 1;
//            }
//        }
//
//        rle.append("|").append(currEl).append(repeat == 1 ? "" : ("," + repeat)).append("|");
//
//        String fileName = "target/output-images/step-6-rleProcessed" + file + ".bin";
//
//        FileWriter myWriter = new FileWriter(fileName);
//        myWriter.write(String.valueOf(rle));
//        myWriter.close();
//
//        return String.valueOf(rle);
//    }

//    private int[] inverseRleProcess(String rleString) {
//
//        List<Integer> result = new ArrayList<>();
//
//        String[] sepParts = rleString.split("[|]");
//
//        for (String sepPart : sepParts) {
//            if (sepPart.isEmpty()) {
//                continue;
//            }
//
//            if (!sepPart.contains(",")) {
//                result.add(Integer.parseInt(sepPart));
//            }
//            else {
//                String[] split = sepPart.split("[,]");
//
//                int repeat;
//
//                if (split.length == 1) {
//                    repeat = 2;
//                }
//                else {
//                    repeat = Integer.parseInt(split[1]);
//                }
//
//                for (int i = 0; i < repeat; i++) {
//                    result.add(Integer.parseInt(split[0]));
//                }
//            }
//        }
//
//        return result.stream().mapToInt(Integer::intValue).toArray();
//    }

    private String rleProcess(int[] dctChannel, String file) throws IOException {

        StringBuilder rle = new StringBuilder();

        int currEl = dctChannel[0];
        int repeat = 1;
        String prevEl = "";

        for (int i = 1; i < dctChannel.length; i++) {
            if (currEl == dctChannel[i] && i != dctChannel.length - 1) {
                repeat++;
            }
            else if (currEl != dctChannel[i] || i == dctChannel.length - 1) {
                if (currEl == dctChannel[i]) {
                    repeat++;
                }

                StringBuilder convertedEl = new StringBuilder();

                int finalRepeat = repeat;
                String.valueOf(currEl).codePoints().map(x -> {
                    if (!Character.isDigit(x)) {
                        return x;
                    }

                    return ((finalRepeat == 1) ? ('a' - '0') : ('A' - '0')) + x;
                }).forEach(x -> convertedEl.append((char)x));

                if (!prevEl.isEmpty() && String.valueOf(convertedEl).charAt(0) != '-' && prevEl.equals(prevEl.toLowerCase()) && String.valueOf(convertedEl).equals(String.valueOf(convertedEl).toLowerCase())) {
                    rle.append("|");
                }
                rle.append((repeat == 1) ? "" : repeat).append(convertedEl);

                prevEl = convertedEl.toString();

                currEl = dctChannel[i];
                repeat = 1;
            }
        }


        String fileName = "target/output-images/step-6-rleProcessed" + file + ".bin";

        FileWriter myWriter = new FileWriter(fileName);
        myWriter.write(String.valueOf(rle));
        myWriter.close();

        return String.valueOf(rle);
    }

    private List<Integer> addValueToResult(String repeatPart, String encodedEl) {
        List<Integer> result = new ArrayList<>();

        int repeat = Integer.parseInt(repeatPart);


        StringBuilder decodedEl = new StringBuilder();
        for (int i = 0; i < encodedEl.length(); i++) {
            char ch = encodedEl.charAt(i);

            if (Character.isUpperCase(ch)) {
                decodedEl.append((char) (ch - ('A' - '0')));
            } else if (Character.isLowerCase(ch)) {
                decodedEl.append((char) (ch - ('a' - '0')));
            } else {
                decodedEl.append(ch);
            }

        }

        int value = Integer.parseInt(String.valueOf(decodedEl));

        for (int i = 0; i < repeat; i++) {
            result.add(value);
        }

        return result;
    }

    private int[] inverseRleProcess(String rleString) {
        List<Integer> result = new ArrayList<>();

        String[] split = rleString.split("\\|");

        Pattern pattern = Pattern.compile("(\\d*)(-?[A-Z]|-?[a-z]*)");

        for(String s : split) {
            Matcher matcher = pattern.matcher(s);
//            System.out.println(result);
//            System.out.println("\n\n\nSPLIT S: " + s);

            while (matcher.find()) {

                if (matcher.group(2).isEmpty()) continue;

                if (!matcher.group(1).isEmpty() && matcher.group(2).equals(matcher.group(2).toUpperCase())) {
                    StringBuilder dig = new StringBuilder();
                    StringBuilder el = new StringBuilder();

                    for (char c : (matcher.group(1) + matcher.group(2)).toCharArray()) {
//                        System.out.println("c: " + c + "digit: " + Character.isDigit(c));

                        dig.append(Character.isDigit(c) ? c : "");
                        el.append(Character.isUpperCase(c) || c == '-' ? c : "");
                    }

                    result.addAll(addValueToResult(String.valueOf(dig), String.valueOf(el)));
                }
                else  if (matcher.group(1).isEmpty()  && matcher.group(2).equals(matcher.group(2).toLowerCase())) {
                    StringBuilder el = new StringBuilder();

                    for (char c : matcher.group(2).toCharArray()) {
                        el.append(Character.isLowerCase(c) || c == '-' ? c : "");
                    }
                    result.addAll(addValueToResult("1", String.valueOf(el)));
                }
            }
        }

        return result.stream().mapToInt(Integer::intValue).toArray();
    }

//    private int[] inverseRleProcess(String rleString) {
//        List<Integer> result = new ArrayList<>();
//
//        String[] split = rleString.split("\\|");
//
//        Pattern pattern = Pattern.compile("(\\d+?-?[A-Z]*)?|(-?[a-z]+)?");
//
//        System.out.println(Arrays.toString(split));
//
//        for(String s : split) {
//            Matcher matcher = pattern.matcher(s);
//
//            System.out.println("SPLIT S: " + s);
//
//            while(matcher.find()) {
////                String repeatPart = "";
////                String encodedEl = "";
//
//                if (matcher.group(2) != null && matcher.group(2).toLowerCase().equals(matcher.group(2))) {
//                    addValueToResult("1", matcher.group(2), result);
//                }
//                else if ((matcher.group(1) != null && matcher.group(2) != null) && (matcher.group(1) + matcher.group(2)).toLowerCase().equals(matcher.group(1) + matcher.group(2))) {
//                    StringBuilder dig = new StringBuilder();
//                    StringBuilder el = new StringBuilder();
//                    for (char c : matcher.group(1).toCharArray()) {
//                        dig.append((Character.isDigit(c)) ? c : "");
//                        el.append((Character.isUpperCase(c)) ? c : "");
//                    }
//
//                    addValueToResult(String.valueOf(dig), String.valueOf(el), result);
//                }
//                else if (matcher.group(1) != null){
//                    StringBuilder dig = new StringBuilder();
//                    StringBuilder el = new StringBuilder();
//                    StringBuilder elSing = new StringBuilder();
//
//                    boolean upper = false;
//
//                    for (char c : matcher.group(1).toCharArray()) {
//                        if (Character.isUpperCase(c)) {
//                            upper = true;
//                        }
//
//                        el.append((c == '-' && !upper) ? c : "");
//                        elSing.append((c == '-' && upper) ? c : "");
//                        dig.append((Character.isDigit(c)) ? c : "");
//                        el.append((Character.isUpperCase(c)) ? c : "");
//                        elSing.append((Character.isLowerCase(c)) ? c : "");
//                    }
//
//                    addValueToResult(String.valueOf(dig), String.valueOf(el), result);
//                    addValueToResult("1", String.valueOf(elSing), result);
//                }
//
////                System.out.println("REPEAT: " + repeatPart + " EL: " + encodedEl);
//
////                if (encodedEl == null) continue;
//
////                System.out.println("a");
////
////                int repeat = (repeatPart != null) ? Integer.parseInt(repeatPart) : 1;
////                System.out.println("b");
////
////                StringBuilder decodedEl = new StringBuilder();
////                for (int i = 0; i < encodedEl.length(); i++) {
////                    char ch = encodedEl.charAt(i);
////
////                    if (Character.isUpperCase(ch)) {
////                        decodedEl.append((char) (ch - ('A' - '0')));
////                    } else if (Character.isLowerCase(ch)) {
////                        decodedEl.append((char) (ch - ('a' - '0')));
////                        break;
////                    } else {
////                        decodedEl.append(ch);
////                    }
////                }
////                System.out.println("c");
////
////                int value = Integer.parseInt(decodedEl.toString());
////
////                System.out.println("d");
////
////                for (int i = 0; i < repeat; i++) {
////                    result.add(value);
////                }
////                System.out.println("e");
//            }
//        }
//
//        return result.stream().mapToInt(Integer::intValue).toArray();
//    }

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
