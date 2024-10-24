package com.kris.jpeg.comp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JpegCompressor {

    private final ColorSpaceProcessor colorSpaceProcessor;
    private final ChrominanceSubsampler chrominanceSubsampler;
    private final TrigonometricProcessor trigonometricProcessor;

    public JpegCompressor() {
        colorSpaceProcessor = new ColorSpaceProcessor();
        chrominanceSubsampler = new ChrominanceSubsampler();
        trigonometricProcessor = new TrigonometricProcessor();
    }

    public void run() {
        try {
        compress("src/main/resources/static/7202.jpg", true);
        decompress(/*"target/output-images/step-4-comp-dct.jpg", */true);
        } catch (IOException e) {
            System.err.println("Error loading or saving image: " + e.getMessage());
        }
    }

    private void debugCompress(BufferedImage yuvImage, BufferedImage downsampledImage) throws IOException {
        System.out.println("--------------------------------------------");
        System.out.println("|        Start debugging compression       |");
        System.out.println("--------------------------------------------");

        FileOutputStream outputFile = new FileOutputStream("target/output-images/step-1-comp-yuv.jpg");
        ImageIO.write(yuvImage, "jpg", outputFile);
        outputFile.close();
        System.out.println("YUV image successfully saved!");

        FileOutputStream downsampledOutput = new FileOutputStream("target/output-images/step-2-comp-downsampled.jpg");
        ImageIO.write(downsampledImage, "jpg", downsampledOutput);
        downsampledOutput.close();
        System.out.println("Downsampled image successfully saved!");

        FileOutputStream dctImageOutputY = new FileOutputStream("target/output-images/step-4-comp-Y-dct.jpg");
        ImageIO.write(trigonometricProcessor.getYDCTImage(), "jpg", dctImageOutputY);
        dctImageOutputY.close();
        FileOutputStream dctImageOutputU = new FileOutputStream("target/output-images/step-4-comp-U-dct.jpg");
        ImageIO.write(trigonometricProcessor.getUDCTImage(), "jpg", dctImageOutputU);
        dctImageOutputU.close();
        FileOutputStream dctImageOutputV = new FileOutputStream("target/output-images/step-4-comp-V-dct.jpg");
        ImageIO.write(trigonometricProcessor.getVDCTImage(), "jpg", dctImageOutputV);
        dctImageOutputV.close();
        System.out.println("DCT processed image successfully saved!");

        System.out.println("--------------------------------------------\n");
    }

    private void debugDecompress(BufferedImage idctImage, BufferedImage upsampledImage) throws IOException {
        System.out.println("--------------------------------------------");
        System.out.println("|       Start debugging decompression      |");
        System.out.println("--------------------------------------------");

        FileOutputStream idctOutput = new FileOutputStream("target/output-images/step-4-decomp-idct.jpg");
        ImageIO.write(idctImage, "jpg", idctOutput);
        idctOutput.close();
        System.out.println("IDCT processed image successfully saved!");

        FileOutputStream upsampledOutput = new FileOutputStream("target/output-images/step-2-decomp-upsampled.jpg");
        ImageIO.write(upsampledImage, "jpg", upsampledOutput);
        upsampledOutput.close();
        System.out.println("Upsampled image successfully saved!");

        System.out.println("--------------------------------------------\n");
    }

    public void compress(String name, boolean debug) throws IOException {
        System.out.println("--------------------------------------------");
        System.out.println("|             Start compression            |");
        System.out.println("--------------------------------------------");

        File inputFile = new File(name);
        Files.createDirectories(Paths.get("target/output-images"));

        BufferedImage rgbImage = ImageIO.read(inputFile);

        BufferedImage yuvImage = colorSpaceProcessor.convertRGBToYUV(rgbImage);
        System.out.println("Image successfully converted from RGB to YUV!");

        BufferedImage downsampledImage = chrominanceSubsampler.downsample(yuvImage);
        System.out.println("Image successfully downsampled!");

        trigonometricProcessor.processImage(downsampledImage);
        System.out.println("Successfully performed DCT!");

        System.out.println("--------------------------------------------\n");

        if(debug) {
            debugCompress(yuvImage, downsampledImage);
        }
    }

    public void decompress(/*String name ,*/boolean debug) throws IOException {
        System.out.println("--------------------------------------------");
        System.out.println("|            Start decompression           |");
        System.out.println("--------------------------------------------");

        BufferedImage idctImage = trigonometricProcessor.reconstructImage(trigonometricProcessor.getYDCTImage(),
                                                                            trigonometricProcessor.getUDCTImage(),
                                                                            trigonometricProcessor.getVDCTImage());
        System.out.println("Successfully performed idct!");

        BufferedImage upsampledImage = chrominanceSubsampler.upsample(idctImage);
        System.out.println("Image successfully upsampled!");

        BufferedImage rgbImage = colorSpaceProcessor.convertYUVToRGB(upsampledImage);
        System.out.println("YUV image successfully converted back to RGB");

        FileOutputStream outputFile = new FileOutputStream("target/output-images/step-1-decomp-rgb.jpg");
        ImageIO.write(rgbImage, "jpg", outputFile);
        outputFile.close();
        System.out.println("Decoded RGB image successfully saved!");

        System.out.println("--------------------------------------------\n");

        if(debug) {
            debugDecompress(idctImage, upsampledImage);
        }
    }
}
