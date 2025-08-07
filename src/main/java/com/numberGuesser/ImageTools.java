package com.numberGuesser;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageTools {
    public static void saveImage(Image rescaledImaged, File file) throws IOException {
        BufferedImage img = SwingFXUtils.fromFXImage(rescaledImaged, null);
        ImageIO.write(img, "png", file);
    }

    public static Image convertToImage(double[][] arr) {
        int height = arr.length;
        int width = arr[0].length;

        WritableImage res = new WritableImage(width, height);
        PixelWriter writer = res.getPixelWriter();
        for(int row = 0; row < height; row++){
            for(int col = 0; col < width; col++){
                double norm = arr[row][col] / 255;

                Color color = new Color(norm, norm, norm, 1);
                writer.setColor(col, row, color);
            }
        }

        return res;
    }

    public static double[][] convertToArray(Image img) {
        int height = (int) img.getHeight();
        int  width = (int) img.getWidth();
        double[][] res = new double[height][width];

        PixelReader reader = img.getPixelReader();
        for(int row = 0; row < height; row++){
            for(int col = 0; col < width; col++){
                Color color = reader.getColor(col, row);

                res[row][col] = color.equals(Color.BLACK) ? 0 : 255;
            }
        }

        return res;
    }

    public static double[][] rescale(double[][] origImage, int newHeight, int newWidth){
        int origHeight = origImage.length;
        int origWidth = origImage[0].length;
        double[][] newImage = new double[newHeight][newWidth];

        double heightScale = (double) origHeight / newHeight;
        double widthScale = (double) origWidth / newWidth;
        for(int row = 0; row < newHeight; row++){
            double upperBorder = row * heightScale;
            double lowerBorder = (row + 1) * heightScale;

            for(int col = 0; col < newWidth; col++){
                double leftBorder = col * widthScale;
                double rightBorder = (col + 1) * widthScale;

                int minRow = (int) upperBorder;
                int maxRow = (int) lowerBorder;
                int minCol = (int) leftBorder;
                int maxCol = (int) rightBorder;

                double sum = 0, area = 0;
                for(int origRow = minRow; origRow <= maxRow; origRow++){
                    if(origRow == origHeight) continue;

                    double heightOverlap = Math.min(lowerBorder, origRow + 1) - Math.max(upperBorder, origRow);

                    for(int origCol = minCol; origCol <= maxCol; origCol++){
                        if(origCol == origWidth) continue;

                        double widthOverlap = Math.min(rightBorder, origCol + 1) - Math.max(leftBorder, origCol);

                        double overlap = heightOverlap * widthOverlap;
                        sum += origImage[origRow][origCol] * overlap;
                        area += overlap;
                    }
                }

                newImage[row][col] = Math.min(255, sum / area);
            }
        }

        return newImage;
    }
}
