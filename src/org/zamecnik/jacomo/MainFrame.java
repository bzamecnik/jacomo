package org.zamecnik.jacomo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import org.zamecnik.jacomo.bot.BotApp;

/**
 *
 * @author Bohouš
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setDefaultLookAndFeelDecorated(true);
        setTitle("JaCoMo");
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                JacomoApplication.getInstance().dispose();
                setVisible(false);
                dispose();
            }
        });

        Container mainPane = getContentPane();
        mainPane.setLayout(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        configButton = new JButton("Configure");
        configButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ConfigDialog dialog = new ConfigDialog(MainFrame.this);
                dialog.pack();
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    String server = dialog.getJabberServer();
                    String username = dialog.getJabberUsername();
                    JacomoApplication.setJabberProperties(
                        server, username,
                        new String(dialog.getJabberPassword())
                        );
                    jabberConfigServerLabel.setText(server);
                    jabberConfigUsernameLabel.setText(username);
                }
                dialog.clearJabberPassword();
            }
        });
        ActionListener toggleActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                JacomoApplication app = JacomoApplication.getInstance();
                if ("database".equals(e.getActionCommand())) {
                    boolean selected = databaseButton.isSelected();
                    configButton.setEnabled(!selected);
                    // connect/disconnect to database
                    // TODO: use SwingWorker
                    // TODO: support progress - print status changes somewhere
                    if (selected) {
                        if (!app.startDatabase()){
                            databaseButton.setSelected(false); // rollback
                            configButton.setEnabled(true);
                            return;
                        }
                    } else {
                        app.stopDatabase();
                    }
                    jabberButton.setEnabled(selected);
                    refreshButton.setEnabled(selected);
                } else if ("jabber".equals(e.getActionCommand())) {
                    boolean selected = jabberButton.isSelected();
                    databaseButton.setEnabled(!selected);
                    // connect/disconnect to jabber here
                    // TODO: use SwingWorker
                    // TODO: support progress - print status changes somewhere
                    BotApp botApp = app.getBotApp();
                    if (selected) {
                        if (!botApp.startJabber()) {
                            jabberButton.setSelected(false); // rollback
                            databaseButton.setEnabled(true);
                            return;
                        }
                    } else {
                        botApp.stopJabber();
                    }
                    loggerButton.setEnabled(selected);
                } else if ("logger".equals(e.getActionCommand())) {
                    boolean selected = loggerButton.isSelected();
                    jabberButton.setEnabled(!selected);
                    // start/stop logger here
                    // TODO: use SwingWorker
                    BotApp botApp = app.getBotApp();
                    if (selected) {
                        botApp.startLogging();
                    } else {
                        botApp.stopLogging();
                    }
                }
            }
        };
        databaseButton = new JToggleButton("Database");
        databaseButton.setActionCommand("database");
        databaseButton.addActionListener(toggleActionListener);

        jabberButton = new JToggleButton("Jabber");
        jabberButton.setEnabled(false);
        jabberButton.setActionCommand("jabber");
        jabberButton.addActionListener(toggleActionListener);

        loggerButton = new JToggleButton("Logger");
        loggerButton.setEnabled(false);
        loggerButton.setActionCommand("logger");
        loggerButton.addActionListener(toggleActionListener);

        refreshButton = new JButton("Refresh charts");
        refreshButton.setEnabled(false);
        refreshButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                // TODO: refresh charts
                // TODO: use SwingWorker
                // TODO: support progress - print status changes somewhere
            }
        });

        toolBar.add(configButton);
        toolBar.add(databaseButton);
        toolBar.add(jabberButton);
        toolBar.add(loggerButton);
        toolBar.add(refreshButton);
        mainPane.add(toolBar, BorderLayout.NORTH);

        JPanel chartPanel = new JPanel();
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.PAGE_AXIS));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.LINE_AXIS));

        infoPanel.add(new JLabel("Jabber server: "));
        jabberConfigServerLabel = new JLabel();
        infoPanel.add(jabberConfigServerLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        infoPanel.add(new JLabel("Username: "));
        jabberConfigUsernameLabel = new JLabel();
        infoPanel.add(jabberConfigUsernameLabel);
        infoPanel.add(Box.createHorizontalGlue());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));


        chartPanel.add(infoPanel);
        chartPanel.add(new JLabel("intervals"));
        chartPanel.add(new JLabel("hour histogram"));
        chartPanel.add(new JLabel("weekday histogram"));
        mainPane.add(new JScrollPane(chartPanel), BorderLayout.CENTER);

        pack();
        setVisible(true);
    }
    private JButton configButton;
    private JToggleButton databaseButton;
    private JToggleButton jabberButton;
    private JToggleButton loggerButton;
    private JButton refreshButton;
    private JLabel jabberConfigServerLabel;
    private JLabel jabberConfigUsernameLabel;
}
