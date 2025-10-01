package com.vsu.cs.cg2025.g12.konchakov_i_v.trianglerasterization.trianglerasterization;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;

import java.awt.*;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    private void fillTriangle(double x1, double y1, double x2, double y2, double x3, double y3, Color c1, Color c2, Color c3) {

    }
}