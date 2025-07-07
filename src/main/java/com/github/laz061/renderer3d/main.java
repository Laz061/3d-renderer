
package com.github.laz061.renderer3d;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // horizontal rotation slider
        JSlider yawSlider = new JSlider(0, 360, 180);
        pane.add(yawSlider, BorderLayout.SOUTH);

        // vertical rotation sliders
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        List<Triangle> Tris = createTetrahedron();

        // panel to display render
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                // fills the window black
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                double yawAngle = Math.toRadians(yawSlider.getValue());
                Matrix yawTransform = new Matrix(new double[] {
                        Math.cos(yawAngle), 0, -Math.sin(yawAngle),
                        0, 1, 0,
                        Math.sin(yawAngle), 0, Math.cos(yawAngle)
                });

                double pitchAngle = Math.toRadians(pitchSlider.getValue());
                Matrix pitchTransform = new Matrix(new double[] {
                        1, 0, 0,
                        0, Math.cos(pitchAngle), Math.sin(pitchAngle),
                        0, -Math.sin(pitchAngle), Math.cos(pitchAngle)
                });
                Matrix totalTransform = yawTransform.multiply(pitchTransform);

                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

                // z buffer to properly render faces that overlap
                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                // initialize array with extremely far away depths
                for (int q = 0; q < zBuffer.length; q++) {
                    zBuffer[q] = Double.NEGATIVE_INFINITY;
                }

                // draws four triangles forming tetrahedral
                for (Triangle t : Tris) {
                    g2.setColor(t.color);
                    Vertex v1 = totalTransform.transform(t.v1);
                    Vertex v2 = totalTransform.transform(t.v2);
                    Vertex v3 = totalTransform.transform(t.v3);

                    // Calculate normal vector
                    Vertex ab = new Vertex(v2.x - v1.x, v2.y - v1.y, v2.z - v1.z);
                    Vertex ac = new Vertex(v3.x - v1.x, v3.y - v1.y, v3.z - v1.z);
                    Vertex norm = new Vertex(
                            ab.y * ac.z - ab.z * ac.y,
                            ab.z * ac.x - ab.x * ac.z,
                            ab.x * ac.y - ab.y * ac.x);
                    double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
                    norm.x /= normalLength;
                    norm.y /= normalLength;
                    norm.z /= normalLength;

                    // Calculate lighting, since the light source (0, 0 ,1)
                    // it simplifies down to the z component of norm
                    double intensity = Math.abs(norm.z);
                    // 20% ambient light
                    intensity = Math.max(0.2, intensity);
                    Color shadedColor = getShade(t.color, intensity);

                    v1.x += getWidth() / 2;
                    v1.y += getHeight() / 2;
                    v2.x += getWidth() / 2;
                    v2.y += getHeight() / 2;
                    v3.x += getWidth() / 2;
                    v3.y += getHeight() / 2;

                    // calculates the parallelogram area using v3->v1 and v3->v2
                    double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) - (v1.x - v3.x) * (v2.y - v3.y);
                    if (Math.abs(triangleArea) < 1e-8)
                        continue; // Skip degenerate triangles

                    // compute rectangular bounds for triangle
                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1,
                            Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));
                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1,
                            Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    // loops through all pixels in the rectangle encapsulating the triangle
                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            // v3 -> p and v3 -> v2 area
                            double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;

                            // v1 -> p and v1 -> v3
                            double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;

                            // v2 -> p and v2 -> v1
                            double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;

                            // check if pixel in triangle
                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1) {
                                // barycentric interpolation for the depth of pixel
                                double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                                int zIndex = y * img.getWidth() + x;
                                // only paint pixel if it is the closer than the previous pixel
                                if (zBuffer[zIndex] < depth) {
                                    img.setRGB(x, y, shadedColor.getRGB());
                                    zBuffer[zIndex] = depth;
                                }
                            }
                        }
                    }
                }

                g2.drawImage(img, 0, 0, null);
            }
        };

        pane.add(renderPanel, BorderLayout.CENTER);

        yawSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());

        frame.setSize(400, 400);
        frame.setVisible(true);
    }

    private static List<Triangle> createTetrahedron() {
        List<Triangle> tris = new ArrayList<>();
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(-100, 100, -100),
                Color.WHITE));
        tris.add(new Triangle(new Vertex(100, 100, 100),
                new Vertex(-100, -100, 100),
                new Vertex(100, -100, -100),
                Color.RED));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(100, 100, 100),
                Color.GREEN));
        tris.add(new Triangle(new Vertex(-100, 100, -100),
                new Vertex(100, -100, -100),
                new Vertex(-100, -100, 100),
                Color.BLUE));

        return tris;
    }

    private static Color getShade(Color color, double shade) {
        // Convert to linear space, apply shade, then back to sRGB
        double redLinear = Math.pow(color.getRed() / 255.0, 2.4) * shade;
        double greenLinear = Math.pow(color.getGreen() / 255.0, 2.4) * shade;
        double blueLinear = Math.pow(color.getBlue() / 255.0, 2.4) * shade;

        int red = (int) (Math.pow(redLinear, 1 / 2.4) * 255);
        int green = (int) (Math.pow(greenLinear, 1 / 2.4) * 255);
        int blue = (int) (Math.pow(blueLinear, 1 / 2.4) * 255);

        // Clamp values to valid range
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        return new Color(red, green, blue);
    }

}
