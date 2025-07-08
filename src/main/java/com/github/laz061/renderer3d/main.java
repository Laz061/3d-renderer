
package com.github.laz061.renderer3d;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.Border;

import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout(5, 5)); // Added some gaps

        // horizontal rotation slider
        JSlider yawSlider = new JSlider(0, 360, 180);
        pane.add(yawSlider, BorderLayout.SOUTH);

        // vertical rotation sliders
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        List<Triangle> Tris = createTetrahedron();

        RenderPanel renderPanel = new RenderPanel(Tris, yawSlider, pitchSlider);
        pane.add(renderPanel, BorderLayout.CENTER);

        yawSlider.addChangeListener(e -> renderPanel.repaint());
        pitchSlider.addChangeListener(e -> renderPanel.repaint());

        frame.pack();
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
}
