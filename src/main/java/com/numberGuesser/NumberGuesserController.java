package com.numberGuesser;

import com.network.Network;
import com.network.NetworkException;
import com.network.NetworkTools;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class NumberGuesserController {
    @FXML
    private Pane canvas;
    @FXML
    private Button pencilButton;
    @FXML
    private Button eraserButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button clearButton;
    private double mouseX, mouseY, mouseSceneX, mouseSceneY;
    private Timeline drawingCircle;
    private Timeline erasingCircle;
    private boolean isPencil, isEraser;
    private Network network;
    @FXML
    public void initialize() throws IOException {
        drawingCircle = new Timeline(new KeyFrame(Duration.millis(5), e -> drawCircle()));
        drawingCircle.setCycleCount(Animation.INDEFINITE);

        erasingCircle = new Timeline(new KeyFrame(Duration.millis(5), e -> eraseCircle()));
        erasingCircle.setCycleCount(Animation.INDEFINITE);

        isPencil = true;
        isEraser = false;

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(canvas.widthProperty());
        clip.heightProperty().bind(canvas.heightProperty());
        canvas.setClip(clip);

        network = new Network("src\\main\\resources\\com\\networks\\network.txt");
    }

    public void pencilButtonClicked(ActionEvent actionEvent) {
        isEraser = false;
        isPencil = true;
    }

    public void eraserButtonClicked(ActionEvent actionEvent) {
        isPencil = false;
        isEraser = true;
    }

    public void mousePressed(MouseEvent mouseEvent) {
        mouseX = mouseEvent.getX();
        mouseY = mouseEvent.getY();

        mouseSceneX = mouseEvent.getSceneX();
        mouseSceneY = mouseEvent.getSceneY();

        if(isPencil) {
            drawCircle();

            drawingCircle.play();
        }else if(isEraser){
            eraseCircle();

            erasingCircle.play();
        }
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        mouseX = mouseEvent.getX();
        mouseY = mouseEvent.getY();

        mouseSceneX = mouseEvent.getSceneX();
        mouseSceneY = mouseEvent.getSceneY();
    }

    public void drawCircle(){
        Circle circle = new Circle(7, Color.WHITE);
        circle.setCenterX(mouseX);
        circle.setCenterY(mouseY);
        circle.setMouseTransparent(true);
        canvas.getChildren().add(circle);
    }
    private void eraseCircle() {
        List<Node> hits = canvas.getChildren().stream()
                .filter(n -> n instanceof Circle && n.contains(n.sceneToLocal(mouseSceneX, mouseSceneY)))
                .toList();

        canvas.getChildren().removeAll(hits);
    }
    public void mouseExited(MouseEvent mouseEvent) {
        if(isPencil) {
            drawingCircle.stop();
        }else if(isEraser){
            erasingCircle.stop();
        }
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        if(isPencil) {
            drawingCircle.stop();
        }else if(isEraser){
            erasingCircle.stop();
        }
    }

    public void clearButtonClicked(ActionEvent actionEvent) {
        canvas.getChildren().clear();
    }

    public void guessButtonClicked(ActionEvent actionEvent) throws NetworkException {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image snapshot = canvas.snapshot(params, null);

        int height = (int) snapshot.getHeight();
        int width = (int) snapshot.getWidth();
        double[][] res = new double[height][width];

        PixelReader reader = snapshot.getPixelReader();
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Color color = reader.getColor(col, row);

                res[row][col] = color.equals(Color.BLACK) ? 0 : 255;
            }
        }

        double[][] rescaled = ImageTools.rescale(res, 28, 28);
        double[] input = NetworkTools.transform2D(rescaled);

        double[] output = network.calculate(input);
        int guess = 0;
        for (int i = 0; i < output.length; i++) {
            if (output[i] > output[guess]) guess = i;
        }

        System.out.println(guess);
    }
}