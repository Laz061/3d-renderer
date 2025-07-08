package com.github.laz061.renderer3d;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import java.awt.*;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        FlatDarkLaf.setup();

        JFrame frame = new JFrame("3D Renderer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout(5, 5)); // Added some gaps

        // horizontal rotation slider
        JSlider yawSlider = new JSlider(0, 360, 180);
        pane.add(yawSlider, BorderLayout.SOUTH);

        // vertical rotation sliders
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        RenderPanel renderPanel = new RenderPanel(yawSlider, pitchSlider);
        pane.add(renderPanel, BorderLayout.CENTER);

        // Control panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton tetrahedronButton = new JButton("Tetrahedron");
        controlPanel.add(tetrahedronButton);

        tetrahedronButton.addActionListener(e -> {
            renderPanel.setShape(createTetrahedron());
            renderPanel.repaint();
        });

        pane.add(controlPanel, BorderLayout.WEST);

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
