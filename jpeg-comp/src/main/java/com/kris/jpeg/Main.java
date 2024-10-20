package com.kris.jpeg;

import com.kris.jpeg.comp.JpegCompressor;

public class Main {
    public static void main(String[] args) {
        JpegCompressor.compress("src/main/resources/static/input.jpeg");
        JpegCompressor.decompress("target/output-images/output_image_yuv.jpg");
    }
}
