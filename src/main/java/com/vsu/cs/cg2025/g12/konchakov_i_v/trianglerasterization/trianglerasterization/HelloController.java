package com.vsu.cs.cg2025.g12.konchakov_i_v.trianglerasterization.trianglerasterization;

import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

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
                    Color.RED, Color.WHITE, Color.BLUE);
        }
    }

    private void fillTriangle(GraphicsContext gc, double x1, double y1, double x2, double y2, double x3, double y3, javafx.scene.paint.Color c1, javafx.scene.paint.Color c2, javafx.scene.paint.Color c3) {
        class LinearEquation {
            private final Double k;
            private final Double b;
            private static final double DELTA = 1E-4;

            public LinearEquation(double x1, double y1, double x2, double y2) {
                if (Math.abs(x1 - x2) <= DELTA && Math.abs(y1 - y2) <= DELTA) {
                    k = null;
                    b = null;
                    return;
                }
                if (Math.abs(x1 - x2) <= DELTA) {
                    k = Double.MAX_VALUE;
                    b = x1;
                    return;
                }
                if (Math.abs(y1 - y2) <= DELTA) {
                    k = (double) 0;
                    b = y1;
                    return;
                }

                if (Math.abs(x1) <= DELTA) {
                    b = y1;
                } else if (Math.abs(x2) <= DELTA) {
                    b = y2;
                } else if (Math.abs(y1) <= DELTA) {
                    b = (double) 0;
                    k = (y2 - b) / x2;
                    return;
                } else if (Math.abs(y2) <= DELTA) {
                    b = (double) 0;
                    k = (y1 - b) / x1;
                    return;
                } else {
                    b = (y2*x1 - y1*x2) / (x1 - x2);
                }
                if (Math.abs(x1) > DELTA) {
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
                double[] bCoords = getBarycentric(x, y, x1, y1, x2, y2, x3, y3);
                Color c = interpolationColor(bCoords[0], bCoords[1], bCoords[2],
                        pointArray[0].getColor(), pointArray[1].getColor(), pointArray[2].getColor());
                pixelWriter.setColor((int) Math.round(x), (int) Math.round(y), c);
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
                double[] bCoords = getBarycentric(x, y, x1, y1, x2, y2, x3, y3);
                Color c = interpolationColor(bCoords[0], bCoords[1], bCoords[2],
                        pointArray[0].getColor(), pointArray[1].getColor(), pointArray[2].getColor());
                pixelWriter.setColor((int) Math.round(x), (int) Math.round(y), c);
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

    private javafx.scene.paint.Color interpolationColor(double alpha, double beta, double gamma,
                                                        Color c1, Color c2, Color c3) {
        double red = alpha * c1.getRed() + beta * c2.getRed() + gamma * c3.getRed();
        double green = alpha * c1.getGreen() + beta * c2.getGreen() + gamma * c3.getGreen();
        double blue = alpha * c1.getBlue() + beta * c2.getBlue() + gamma * c3.getBlue();

        red = clamp(red, 0, 1);
        green = clamp(green, 0, 1);
        blue = clamp(blue, 0, 1);

        return new Color(red, green, blue, 1);
    }

    private double clamp(double a, double left, double right) {
        if (a > right) {
            return right;
        }
        if (a < left) {
            return left;
        }
        return a;
    }

    private double[] getBarycentric(double x, double y,
                                    double x1, double y1,
                                    double x2, double y2,
                                    double x3, double y3) {
        double det = (x1 - x3)*(y2 - y3) - (x2 -x3)*(y1 - y3);
        double gamma = (x1*y2 - x1*y - x2*y1 + x2*y + x*y1 - x*y2) / det;
        double beta = (x1*y - x1*y3 - x*y1 + x*y3 + x3*y1 - x3*y) / det;
        double alpha = 1 - beta - gamma;
        return new double[]{alpha, beta, gamma};
    }
}