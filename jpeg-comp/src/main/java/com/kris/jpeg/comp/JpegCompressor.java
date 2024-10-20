package com.kris.jpeg.comp;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JpegCompressor {
    private final int quality;
    private final int blockSize;

    public JpegCompressor() {
        this.quality = 1;
        this.blockSize = 8;
    }

    public static void compress(String name) {
        try {
            // Load the image
            File inputFile = new File(name);
            BufferedImage rgbImage = ImageIO.read(inputFile);

            // Convert the image from RGB to YUV
            BufferedImage yuvImage = ColorSpaceProcessor.convertRGBToYUV(rgbImage);

            Files.createDirectories(Paths.get("target/output-images"));

            // Save the YUV image as a JPG
            FileOutputStream outputFile = new FileOutputStream("target/output-images/output_image_yuv.jpg");
            ImageIO.write(yuvImage, "jpg", outputFile);

            System.out.println("Image successfully converted and saved!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void decompress(String name) {
        try {
            // Load the YUV image
            File inputFile = new File(name);
            BufferedImage yuvImage = ImageIO.read(inputFile);

            // Convert the YUV image back to RGB
            BufferedImage rgbImage = ColorSpaceProcessor.convertYUVToRGB(yuvImage);

            // Save the RGB image as a JPG
            File outputFile = new File("target/output-images/output_image_rgb.jpg");
            ImageIO.write(rgbImage, "jpg", outputFile);

            System.out.println("YUV image successfully converted back to RGB and saved!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
