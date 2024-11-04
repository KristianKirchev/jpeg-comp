package com.kris.jpeg.comp;

import lombok.Getter;

@Getter
public class HuffNode {
    String charValue;
    int freq;
    HuffNode left;
    HuffNode right;

    public HuffNode(String charValue, int freq) {
        this.charValue = charValue;
        this.freq = freq;
        left = null;
        right = null;
    }

    public HuffNode(int freq) {
        charValue = "\0";
        this.freq = freq;
        left = null;
        right = null;
    }
}
