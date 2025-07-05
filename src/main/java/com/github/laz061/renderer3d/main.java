
package com.github.laz061.renderer3d;

import java.awt.*;
import java.awt.geom.Path2D;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // horizontal roation slider
        JSlider yawSlider = new JSlider(0, 360, 180);
        pane.add(yawSlider, BorderLayout.SOUTH);

        // vertical rotation sliders
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

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

        // panel to display render results
        JPanel renderPanel = new JPanel() {
            public void paintComponent(Graphics g) {
                // fills the window black
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // moves to center
                g2.translate(getWidth() / 2, getHeight() / 2);

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

                // draws four triangles forming tetrahedral
                for (Triangle t : tris) {
                    g2.setColor(t.color);
                    Vertex v1 = totalTransform.transform(t.v1);
                    Vertex v2 = totalTransform.transform(t.v2);
                    Vertex v3 = totalTransform.transform(t.v3);
                    Path2D path = new Path2D.Double();
                    path.moveTo(v1.x, v1.y);
                    path.lineTo(v2.x, v2.y);
                    path.lineTo(v3.x, v3.y);
                    path.closePath();
                    g2.draw(path);
                }

            }
        };

        pane.add(renderPanel, BorderLayout.CENTER);

        yawSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());

        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}
