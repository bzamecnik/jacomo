package org.zamecnik.jacomo.stats;

import java.awt.BorderLayout;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Bohouš
 */
public class Gui {

    private static void createAndShowGUI() {
        //JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("JaCoMo - stats");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());
        //pane.setLayout(new GridLayout(3, 0));

        pane.add(new JLabel("count"), BorderLayout.PAGE_START);
        pane.add(new HistogramPanel(), BorderLayout.CENTER);
        pane.add(new JLabel("intervals"), BorderLayout.PAGE_END);

        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                createAndShowGUI();
            }
        });
    }
}
