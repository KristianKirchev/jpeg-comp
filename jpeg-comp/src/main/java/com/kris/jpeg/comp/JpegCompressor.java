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
        compress("src/main/resources/static/tiger.jpeg");
        decompress(/*"target/output-images/step-4-comp-dct.jpg"*/);
    }

    public void compress(String name) {
        try {
            File inputFile = new File(name);
            BufferedImage rgbImage = ImageIO.read(inputFile);

            BufferedImage yuvImage = colorSpaceProcessor.convertRGBToYUV(rgbImage);

            Files.createDirectories(Paths.get("target/output-images"));

            FileOutputStream outputFile = new FileOutputStream("target/output-images/step-1-comp-yuv.jpg");
            ImageIO.write(yuvImage, "jpg", outputFile);

            System.out.println("Image successfully converted and saved!");

            ////////////////////////////////////////////////////

            BufferedImage downsampledImage = chrominanceSubsampler.downsample(yuvImage);

            File downsampledOutput = new File("target/output-images/step-2-comp-downsampled.jpg");
            ImageIO.write(downsampledImage, "jpg", downsampledOutput);
            System.out.println("Image successfully downsampled and saved!");

            ////////////////////////////////////////////////////

            trigonometricProcessor.processImage(downsampledImage);

            File dctImageOutputY = new File("target/output-images/step-4-comp-Y-dct.jpg");
            ImageIO.write(trigonometricProcessor.getYDCTImage(), "jpg", dctImageOutputY);
            File dctImageOutputU = new File("target/output-images/step-4-comp-U-dct.jpg");
            ImageIO.write(trigonometricProcessor.getUDCTImage(), "jpg", dctImageOutputU);
            File dctImageOutputV = new File("target/output-images/step-4-comp-V-dct.jpg");
            ImageIO.write(trigonometricProcessor.getVDCTImage(), "jpg", dctImageOutputV);

            System.out.println("Successfully performed dct and saved!");

        } catch (IOException e) {
            System.err.println("Error loading or saving image: " + e.getMessage());
        }
    }

    public void decompress(/*String name*/) {
        try {
            BufferedImage idctImage = trigonometricProcessor.reconstructImage(trigonometricProcessor.getYDCTImage(),
                                                                                trigonometricProcessor.getUDCTImage(),
                                                                                trigonometricProcessor.getVDCTImage());

            File idctOutput = new File("target/output-images/step-4-decomp-idct.jpg");
            ImageIO.write(idctImage, "jpg", idctOutput);
            System.out.println("Successfully performed idct and saved!");

            ////////////////////////////////////////////

            BufferedImage upsampledImage = chrominanceSubsampler.upsample(idctImage);

            File upsampledOutput = new File("target/output-images/step-2-decomp-upsampled.jpg");
            ImageIO.write(upsampledImage, "jpg", upsampledOutput);
            System.out.println("Image successfully upsampled and saved!");

            ////////////////////////////////////////////

            BufferedImage rgbImage = colorSpaceProcessor.convertYUVToRGB(upsampledImage);

            File outputFile = new File("target/output-images/step-1-decomp-rgb.jpg");
            ImageIO.write(rgbImage, "jpg", outputFile);

            System.out.println("YUV image successfully converted back to RGB and saved!");

        } catch (IOException e) {
            System.err.println("Error loading or saving image: " + e.getMessage());
        }
    }
}
