package com.vsu.cs.cg2025.g12.konchakov_i_v.trianglerasterization.trianglerasterization;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

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
    java.awt.Color c1 = new java.awt.Color(255, 0, 0);
    java.awt.Color c2 = new java.awt.Color(255, 138, 0);
    java.awt.Color c3 = new java.awt.Color(255, 234, 0);

    Color color1 = new Color((double) c1.getRed() /255, (double) c1.getGreen() /255, (double) c1.getBlue() /255, 1);
    Color color2 = new Color((double) c2.getRed() /255, (double) c2.getGreen() /255, (double) c2.getBlue() /255, 1);
    Color color3 = new Color((double) c3.getRed() /255, (double) c3.getGreen() /255, (double) c3.getBlue() /255, 1);

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
                    color1, color2, color3);
        }
    }

    private void fillTriangle(GraphicsContext gc, double x1, double y1, double x2, double y2, double x3, double y3, javafx.scene.paint.Color c1, javafx.scene.paint.Color c2, javafx.scene.paint.Color c3) {
        class LinearEquation {
            private final Double k;
            private final Double b;

            public LinearEquation(double x1, double y1, double x2, double y2) {
                if (Double.valueOf(x1).equals(x2) && Double.valueOf(y1).equals(y2)) {
                    k = null;
                    b = null;
                    return;
                }
                if (Double.valueOf(x1).equals(x2)) {
                    k = Double.MAX_VALUE;
                    b = x1;
                    return;
                }
                if (Double.valueOf(y1).equals(y2)) {
                    k = (double) 0;
                    b = y1;
                    return;
                }

                if (Double.valueOf(x1).equals(0.0)) {
                    b = y1;
                } else if (Double.valueOf(x2).equals(0.0)) {
                    b = y2;
                } else if (Double.valueOf(y1).equals(0.0)) {
                    b = (double) 0;
                    k = (y2 - b) / x2;
                    return;
                } else if (Double.valueOf(y2).equals(0.0)) {
                    b = (double) 0;
                    k = (y1 - b) / x1;
                    return;
                } else {
                    b = (y2*x1 - y1*x2) / (x1 - x2);
                }
                if (!Double.valueOf(x1).equals(0.0)) {
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
        }

        class ColoredPoint extends Point2D {
            final javafx.scene.paint.Color color;

            public ColoredPoint(double v, double v1, javafx.scene.paint.Color c) {
                super(v, v1);
                color = c;
            }

            public javafx.scene.paint.Color getColor() {
                return color;
            }
        }

        PixelWriter pixelWriter = gc.getPixelWriter();
        ColoredPoint p1 = new ColoredPoint(x1, y1, c1);
        ColoredPoint p2 = new ColoredPoint(x2, y2, c2);
        ColoredPoint p3 = new ColoredPoint(x3, y3, c3);
        ColoredPoint[] pointArray = new ColoredPoint[]{p1, p2, p3};
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
                Vector vector = new Vector(x, y,
                        pointArray[0].getX(), pointArray[0].getY(),
                        pointArray[1].getX(), pointArray[1].getY(),
                        pointArray[2].getX(), pointArray[2].getY());
                pixelWriter.setColor((int) Math.round(x), (int) Math.round(y), interpolationColor(vector, pointArray[0].getColor(), pointArray[1].getColor(), pointArray[2].getColor()));
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
                Vector vector = new Vector(x, y,
                        pointArray[0].getX(), pointArray[0].getY(),
                        pointArray[1].getX(), pointArray[1].getY(),
                        pointArray[2].getX(), pointArray[2].getY());
                pixelWriter.setColor((int) Math.round(x), (int) Math.round(y), interpolationColor(vector, pointArray[0].getColor(), pointArray[1].getColor(), pointArray[2].getColor()));
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

    private javafx.scene.paint.Color interpolationColor(Vector v,
                                                        Color c1, Color c2, Color c3) {
        double red = v.getX() * c1.getRed() + v.getY() * c2.getRed() + v.getZ() * c3.getRed();
        double green = v.getY() * c1.getGreen() + v.getY() * c2.getGreen() + v.getZ() * c3.getGreen();
        double blue = v.getY() * c1.getBlue() + v.getY() * c2.getBlue() + v.getZ() * c3.getBlue();

        red = clamp(red, 0, 1);
        green = clamp(green, 0, 1);
        blue = clamp(blue, 0, 1);


        return new Color(red, green, blue, 1);
    }

    class Vector {
        private double x;
        private double y;
        private double z;

        public Vector(double curX, double curY,
                      double x1, double y1,
                      double x2, double y2,
                      double x3, double y3) {
            setBarycentric(curX, curY, x1, y1, x2, y2, x3, y3);
        }

        private void setBarycentric(double curX, double curY,
                                    double x1, double y1,
                                    double x2, double y2,
                                    double x3, double y3) {
            z = ((curX - x1)*(y2 - y1) - (curY - y1)*(x2 - x1)) / ((x3 - x1)*(y2 - y1) - (y3 - y1)*(x2 - x1));
            y = (curY - y1 - z * (y3 - y1)) / (y2 - y1);
            x = 1 - y - z;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getZ() {
            return z;
        }
    }

    private double clamp(double a, double left, double right) {
        if (a - right > 0) {
            return right;
        }
        if (a - left < 0) {
            return left;
        }
        return a;
    }
}