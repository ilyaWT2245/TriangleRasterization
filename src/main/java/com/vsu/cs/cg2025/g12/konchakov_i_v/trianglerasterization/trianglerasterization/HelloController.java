package com.vsu.cs.cg2025.g12.konchakov_i_v.trianglerasterization.trianglerasterization;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class HelloController {
    @FXML
    private AnchorPane anchorPane;

    @FXML
    private Canvas canvas;

    ArrayList<Point2D> points = new ArrayList<>();

    @FXML
    protected void initialize() {
        anchorPane.prefWidthProperty().addListener((ov, oldValue, newValue) -> canvas.setWidth(newValue.doubleValue()));
        anchorPane.prefHeightProperty().addListener((ov, oldValue, newValue) -> canvas.setHeight(newValue.doubleValue()));

        canvas.setOnMouseClicked(event -> {
            switch (event.getButton()) {
                case PRIMARY -> handlePrimaryClick(canvas.getGraphicsContext2D(), event);
            }
        });
    }

    private void handlePrimaryClick(GraphicsContext graphicsContext, MouseEvent event) {
        if (points.size() >= 3) {

            graphicsContext.clearRect(0,0, canvas.getHeight(), canvas.getWidth());
            points = new ArrayList<>();
        }
        final int POINT_RADIUS = 3;
        graphicsContext.fillOval(
                event.getX() - POINT_RADIUS, event.getY() - POINT_RADIUS,
                2 * POINT_RADIUS, 2 * POINT_RADIUS);
        points.add(new Point2D(event.getX(), event.getY()));
        if (points.size() == 3) {
            fillTriangle(graphicsContext,
                    points.get(0).getX(), points.get(0).getY(),
                    points.get(1).getX(), points.get(1).getY(),
                    points.get(2).getX(), points.get(2).getY(),
                    Color.BLACK, Color.BLACK, Color.BLACK);
        }
    }

    private void fillTriangle(GraphicsContext gc, double x1, double y1, double x2, double y2, double x3, double y3, Color c1, Color c2, Color c3) {
        PixelWriter pixelWriter = gc.getPixelWriter();
        Point2D p1 = new Point2D(x1, y1);
        Point2D p2 = new Point2D(x2, y2);
        Point2D p3 = new Point2D(x3, y3);
        Point2D[] pointArray = new Point2D[]{p1, p2, p3};
        sortByY(pointArray);

        LinearEquation eq01 = new LinearEquation(pointArray[0].getX(), pointArray[0].getY(), pointArray[1].getX(), pointArray[1].getY());
        LinearEquation eq12 = new LinearEquation(pointArray[1].getX(), pointArray[1].getY(), pointArray[2].getX(), pointArray[2].getY());
        LinearEquation eq02 = new LinearEquation(pointArray[0].getX(), pointArray[0].getY(), pointArray[2].getX(), pointArray[2].getY());

        for (double y = pointArray[0].getY(); y < pointArray[1].getY(); y++) {
            double xBoundary1 = eq01.getX(y);
            double xBoundary2 = eq02.getX(y);

            if (xBoundary1 > xBoundary2) {
                double temp = xBoundary1;
                xBoundary1 = xBoundary2;
                xBoundary2 = temp;
            }

            for (double x = xBoundary1; x <= xBoundary2; x++) {
                pixelWriter.setColor((int) Math.round(x), (int) Math.round(y), javafx.scene.paint.Color.BLACK);
            }
        }
        for (double y = pointArray[1].getY(); y < pointArray[2].getY(); y++) {
            double xBoundary1 = eq12.getX(y);
            double xBoundary2 = eq02.getX(y);

            if (xBoundary1 > xBoundary2) {
                double temp = xBoundary1;
                xBoundary1 = xBoundary2;
                xBoundary2 = temp;
            }

            for (double x = xBoundary1; x <= xBoundary2; x++) {
                pixelWriter.setColor((int) Math.round(x), (int) Math.round(y), javafx.scene.paint.Color.BLACK);
            }
        }
    }

    private void sortByY(Point2D[] array) {
        Arrays.sort(array, new Comparator<Point2D>() {
            @Override
            public int compare(Point2D o1, Point2D o2) {
                if (o1.getY() > o2.getY()) {
                    return 1;
                } else if (o1.getY() < o2.getY()) {
                    return -1;
                }
                return 0;
            }
        });
    }

    private class LinearEquation {
        private final Double k;
        private final Double b;

        public LinearEquation(double x1, double y1, double x2, double y2) {
            if (x1 == x2 && y1 == y2) {
                k = null;
                b = null;
                return;
            }
            if (x1 == x2) {
                k = Double.MAX_VALUE;
                b = x1;
                return;
            }
            if (y1 == y2) {
                k = (double) 0;
                b = y1;
                return;
            }

            if (x1 == 0) {
                b = y1;
            } else if (x2 == 0) {
                b = y2;
            } else if (y1 == 0) {
                b = (double) 0;
                k = (y2 - b) / x2;
                return;
            } else if (y2 == 0) {
                b = (double) 0;
                k = (y1 - b) / x1;
                return;
            } else {
                b = (y2 - y2 * (x2/x1)) / (1 - (x2/x1));
            }
            if (x1 != 0) {
                k = (y1 - b) / x1;
            } else {
                k = (y2 - b) / x2;
            }
        }

        public double getX(double y) {
            if (b == null || k == null) {
                return y;
            }
            if (k.equals((double) 0)) {
                return b;
            }
            if (k.equals(Double.MAX_VALUE)) {
                return b;
            }
            return (y - b) / k;
        }

        public Double getK() {
            return k;
        }
    }
}