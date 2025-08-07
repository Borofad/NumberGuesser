package com.network;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MnistReader {
    public static double[][] loadImages(String path) throws IOException {
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(path));
        int magic = dataInputStream.readInt();
        int count = dataInputStream.readInt();
        int rows = dataInputStream.readInt();
        int cols = dataInputStream.readInt();

        double[][] images = new double[count][rows * cols];
        for(int i = 0; i < count; i++){
            for(int j = 0; j < rows * cols; j++){
                images[i][j] = (double) dataInputStream.readUnsignedByte() / 255;
            }
        }

        return images;
    }

    public static int[] loadLabels(String path) throws IOException{
        DataInputStream dataInputStream = new DataInputStream(new FileInputStream(path));
        int magic = dataInputStream.readInt();
        int count = dataInputStream.readInt();

        int[] labels = new int[count];
        for(int i = 0; i < count; i++){
            labels[i] = dataInputStream.readUnsignedByte();
        }

        return labels;
    }
}
