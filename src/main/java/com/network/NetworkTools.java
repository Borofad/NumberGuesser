package com.network;

import java.io.IOException;

public class NetworkTools {
    public static double[][][] xavierInitialization(int[] NETWORK_LAYER_SIZES){
        int NETWORK_SIZE = NETWORK_LAYER_SIZES.length;
        double[][][] weights = new double[NETWORK_SIZE][][];
        for(int i = 1; i < NETWORK_SIZE; i++){
            double a = Math.sqrt(6.0 / (NETWORK_LAYER_SIZES[i - 1] + NETWORK_LAYER_SIZES[i]));
            weights[i] = createRandom2DArray(   NETWORK_LAYER_SIZES[i - 1],
                                                NETWORK_LAYER_SIZES[i],
                                                -4 * a,
                                                4 * a);
        }

        return weights;
    }

    public static double[][] createRandom2DArray(int rows, int cols, double lowerBound, double upperBound){
        double[][] res = new double[rows][];
        for(int i = 0; i < rows; i++){
            res[i] = createRandomArray(cols, lowerBound, upperBound);
        }

        return res;
    }

    public static double[] createRandomArray(int length, double lowerBound, double upperBound){
        double[] res = new double[length];
        for(int i = 0; i < length; i++){
            res[i] = Math.random() * (upperBound - lowerBound) + lowerBound;
        }

        return res;
    }

    public static double[] transform2D(double[][] arr){
        int rows = arr.length;
        int cols = arr[0].length;
        double[] res = new double[rows * cols];
        for(int row = 0; row < rows; row++){
            for(int col = 0; col < cols; col++){
                res[row * cols + col] = arr[row][col];
            }
        }

        return res;
    }

    public static void trainMnist(Network network, int epochs, double learningRate) throws IOException, NetworkException {
        double[][] trainImages = MnistReader.loadImages("src\\main\\resources\\com\\mnist\\train-images.idx3-ubyte");
        int[] trainLabels = MnistReader.loadLabels("src\\main\\resources\\com\\mnist\\train-labels.idx1-ubyte");

        for(int epoch = 0; epoch < epochs; epoch++) {
            System.out.println("Epoch " + epoch);
            for (int i = 0; i < trainImages.length; i++) {
                double[] target = new double[10];
                target[trainLabels[i]] = 1;

                network.train(trainImages[i], target, learningRate);

                if ((i + 1) % 3000 == 0) System.out.println((i + 1) + " images processed");
            }
        }
    }

    public static void checkNetwork(Network network) throws IOException, NetworkException {
        double[][] testImages = MnistReader.loadImages("src\\main\\resources\\com\\mnist\\t10k-images.idx3-ubyte");
        int[] testLabels = MnistReader.loadLabels("src\\main\\resources\\com\\mnist\\t10k-labels.idx1-ubyte");

        int[][] guesses = new int[10][10];
        int[] frequencyOfNumbers = new int[10];
        int right = 0;
        for(int i = 0; i < testImages.length; i++){
            frequencyOfNumbers[testLabels[i]]++;

            double[] output = network.calculate(testImages[i]);

            int maxIdx = 0;
            for(int j = 0; j < output.length; j++){
                if(output[j] > output[maxIdx]) maxIdx = j;
            }

            guesses[testLabels[i]][maxIdx]++;

            if(maxIdx == testLabels[i]) right++;

            if((i + 1) % 1000 == 0) System.out.println((i + 1) + " images tested");
        }

        System.out.println("Percentage of right " + (double) 100 * right / testImages.length + "%");

        for(int expected = 0; expected < 10; expected++){
            System.out.println("----------------------------------------");
            System.out.println("Number " + expected + " statistic");

            int wrongCount = 0;
            for(int networkGuess = 0; networkGuess < 10; networkGuess++){
                if(expected != networkGuess) wrongCount += guesses[expected][networkGuess];

                System.out.println(guesses[expected][networkGuess] + " times network guessed " + networkGuess);
            }

            System.out.println("Wrongly guessed " + wrongCount + " of " + frequencyOfNumbers[expected] + " times(" + (double) 100 * wrongCount / frequencyOfNumbers[expected] + "%)");
        }

        System.out.println("----------------------------------------");
    }
}