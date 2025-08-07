package com.network;

import java.io.*;
import java.util.Arrays;

public class Network {
    private final int[] NETWORK_LAYER_SIZES;
    private final int NETWORK_SIZE;
    private final int INPUT_SIZE;
    private final int OUTPUT_SIZE;
    private double[][][] weights;
    private double[][] biases;
    private double[][] outputs;
    private double[][] errorSignals;
    public Network(int... NETWORK_LAYER_SIZES) {
        this.NETWORK_LAYER_SIZES = NETWORK_LAYER_SIZES;
        this.NETWORK_SIZE = NETWORK_LAYER_SIZES.length;
        this.INPUT_SIZE = NETWORK_LAYER_SIZES[0];
        this.OUTPUT_SIZE = NETWORK_LAYER_SIZES[NETWORK_SIZE - 1];

        this.weights = NetworkTools.xavierInitialization(NETWORK_LAYER_SIZES);

        this.biases = new double[NETWORK_SIZE][];
        this.outputs = new double[NETWORK_SIZE][];
        this.errorSignals = new double[NETWORK_SIZE][];
        for(int i = 0; i < NETWORK_SIZE; i++){
            outputs[i] = new double[NETWORK_LAYER_SIZES[i]];

            if(i > 0){
                biases[i] = new double[NETWORK_LAYER_SIZES[i]];
                errorSignals[i] = new double[NETWORK_LAYER_SIZES[i]];
            }
        }
    }

    public Network(String path) throws IOException {
        File file = new File(path);

        if(!file.exists()) throw new FileNotFoundException("Network file not found: " + path);

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = reader.readLine();
        if(!line.startsWith("# Network size ")) throw new IOException("Bad header. Expected: # Network size");
        NETWORK_SIZE = Integer.parseInt(line.substring(15));

        line = reader.readLine();
        if(!line.startsWith("# Network layer sizes ")) throw new IOException("Bad header. Expected: # Network layer sizes");
        NETWORK_LAYER_SIZES = Arrays.stream(line.substring(22).split(" ")).mapToInt(Integer::parseInt).toArray();
        INPUT_SIZE = NETWORK_LAYER_SIZES[0];
        OUTPUT_SIZE = NETWORK_LAYER_SIZES[NETWORK_SIZE - 1];

        weights = new double[NETWORK_SIZE][][];
        biases = new double[NETWORK_SIZE][];
        outputs = new double[NETWORK_SIZE][];
        errorSignals = new double[NETWORK_SIZE][];
        for(int layer = 0; layer < NETWORK_SIZE; layer++){
            outputs[layer] = new double[NETWORK_LAYER_SIZES[layer]];
            errorSignals[layer] = new double[NETWORK_LAYER_SIZES[layer]];

            if(layer == 0) continue;

            if(!reader.readLine().equals("# Weights layer " + layer)) throw new IOException("Bad header. Expected: # Weights layer " + layer);

            weights[layer] = new double[NETWORK_LAYER_SIZES[layer - 1]][];
            for(int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZES[layer - 1]; prevNeuron++){
                weights[layer][prevNeuron] = Arrays.stream(reader.readLine().split(" ")).mapToDouble(Double::parseDouble).toArray();
            }

            if(!reader.readLine().equals("# Biases layer " + layer)) throw new IOException("Bad header. Expected: # Biases layer " + layer);

            biases[layer] = Arrays.stream(reader.readLine().split(" ")).mapToDouble(Double::parseDouble).toArray();
        }
    }

    public double[] calculate(double[] input) throws NetworkException {
        if(input.length != INPUT_SIZE) throw new NetworkException("Expected input size " + INPUT_SIZE + ", but found " + input.length);

        outputs[0] = Arrays.copyOf(input, INPUT_SIZE);
        for(int layer = 1; layer < NETWORK_SIZE; layer++){
            int neurons = NETWORK_LAYER_SIZES[layer];

            for(int neuron = 0; neuron < neurons; neuron++){
                int prevLayerNeurons = NETWORK_LAYER_SIZES[layer - 1];

                double sum = biases[layer][neuron];
                for(int prevNeuron = 0; prevNeuron < prevLayerNeurons; prevNeuron++){
                    sum += outputs[layer - 1][prevNeuron] * weights[layer][prevNeuron][neuron];
                }
                outputs[layer][neuron] = sigmoid(sum);
            }
        }

        return outputs[NETWORK_SIZE - 1];
    }

    private void calculateErrorSignals(double[] output, double[] target) throws NetworkException {
        if(output.length != OUTPUT_SIZE) throw new NetworkException("Expected output size " + OUTPUT_SIZE + ", but found " + output.length);
        if(target.length != OUTPUT_SIZE) throw new NetworkException("Expected target size " + OUTPUT_SIZE + ", but found " + target.length);

        for(int neuron = 0; neuron < OUTPUT_SIZE; neuron++){
            errorSignals[NETWORK_SIZE - 1][neuron] = (output[neuron] - target[neuron]) * output[neuron] * (1 - output[neuron]);
        }

        for(int layer = NETWORK_SIZE - 2; layer >= 1; layer--){
            for(int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron++){
                double weightedErrorSignalsSum = 0d;
                for(int nextNeuron = 0; nextNeuron < NETWORK_LAYER_SIZES[layer + 1]; nextNeuron++){
                    weightedErrorSignalsSum += weights[layer + 1][neuron][nextNeuron] * errorSignals[layer + 1][nextNeuron];
                }

                errorSignals[layer][neuron] = outputs[layer][neuron] * (1 - outputs[layer][neuron]) * weightedErrorSignalsSum;
            }
        }
    }

    private void updateWeightsAndBiases(double learningRate){
        for(int layer = 1; layer < NETWORK_SIZE; layer++){
            for(int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron++){
                biases[layer][neuron] -= learningRate * errorSignals[layer][neuron];

                for(int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZES[layer - 1]; prevNeuron++){
                    weights[layer][prevNeuron][neuron] -= learningRate * errorSignals[layer][neuron] * outputs[layer - 1][prevNeuron];
                }
            }
        }
    }

    public void train(double[] input, double[] target, double learningRate) throws NetworkException {
        if(input.length != INPUT_SIZE) throw new NetworkException("Expected inputs size " + INPUT_SIZE + ", but found " + input.length);

        double[] output = calculate(input);
        calculateErrorSignals(output, target);
        updateWeightsAndBiases(learningRate);
    }

    private double sigmoid(double sum) {
        return 1d / (1 + Math.exp(-sum));
    }

    public void save(String path) throws IOException {
        File file = new File(path);
        file.createNewFile();

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        writer.write("# Network size " + NETWORK_SIZE + "\n");
        writer.write("# Network layer sizes ");
        for(int size : NETWORK_LAYER_SIZES) writer.write(size + " ");
        writer.write("\n");

        for(int layer = 1; layer < NETWORK_SIZE; layer++){
            writer.write("# Weights layer " + layer + "\n");

            for(int prevNeuron = 0; prevNeuron < NETWORK_LAYER_SIZES[layer - 1]; prevNeuron++){
                for(int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron++){
                    writer.write(weights[layer][prevNeuron][neuron] + " ");
                }

                writer.write("\n");
            }

            writer.write("# Biases layer " + layer + "\n");
            for(int neuron = 0; neuron < NETWORK_LAYER_SIZES[layer]; neuron++){
                writer.write(biases[layer][neuron] + " ");
            }

            writer.write("\n");
        }

        writer.close();
    }
}