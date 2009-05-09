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
import javax.swing.SwingWorker;
import org.zamecnik.jacomo.bot.BotApplication;
import org.zamecnik.jacomo.stats.ChartsPanel;

/**
 * Main JaCoMo application frame.
 * @author Bohumir Zamecnik
 */
public class MainFrame extends JFrame {

    /**
     * MainFrame constructor.
     */
    public MainFrame() {
        setDefaultLookAndFeelDecorated(true);
        setTitle("JaCoMo");
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                PropertiesHelper.saveProperties();
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

            // use configuration dialog to get Jabber config data
            public void actionPerformed(ActionEvent e) {
                ConfigDialog dialog = new ConfigDialog(MainFrame.this);
                dialog.pack();
                dialog.setVisible(true);
                if (dialog.isConfirmed()) {
                    String server = dialog.getJabberServer();
                    String username = dialog.getJabberUsername();
                    PropertiesHelper.setJabberProperties(
                            server, username,
                            new String(dialog.getJabberPassword()));
                    jabberConfigServerLabel.setText(server);
                    jabberConfigUsernameLabel.setText(username);
                }
                dialog.clearJabberPassword();
            }
        });
        ActionListener toggleActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                final JacomoApplication app = JacomoApplication.getInstance();
                // database button - connect/disconnect to database
                if ("database".equals(e.getActionCommand())) {
                    final boolean selected = databaseButton.isSelected();
                    configButton.setEnabled(!selected);
                    // TODO: support progress - print status changes somewhere
                    if (selected) {
                        new SwingWorker<Boolean, Void>() {

                            // run in new thread
                            public Boolean doInBackground() {
                                return app.startDatabase();
                            }

                            // run in GUI thread after doInBackground() finished
                            @Override
                            public void done() {
                                boolean databaseStarted = false;
                                try {
                                    databaseStarted = get();
                                } catch (Exception ex) {
                                }
                                if (databaseStarted) {
                                    System.out.println("action listener (database): showing charts");
                                    chartsPanel.setStatsApp(app.getStatsApp());
                                    jabberButton.setEnabled(true);
                                    refreshButton.setEnabled(true);
                                } else {
                                    databaseButton.setSelected(false); // rollback
                                    configButton.setEnabled(true);
                                    System.out.println("action listener (database): rollback");
                                    return;
                                }
                            }
                        }.execute();
                    } else {
                        jabberButton.setEnabled(false);
                        refreshButton.setEnabled(false);
                        app.stopDatabase(); // TODO: run in SwingWorker
                        chartsPanel.setStatsApp(null);
                    }
                // jabber button - connect/disconnect to jabber
                } else if ("jabber".equals(e.getActionCommand())) {
                    final boolean selected = jabberButton.isSelected();
                    databaseButton.setEnabled(!selected);
                    // TODO: support progress - print status changes somewhere
                    final BotApplication botApp = app.getBotApp();
                    if (selected) {
                        new SwingWorker<Boolean, Void>() {

                            // run in a new thread
                            public Boolean doInBackground() {
                                return botApp.login();
                            }

                            // run in GUI thread after doInBackground() finished
                            @Override
                            public void done() {
                                boolean jabberStarted = false;
                                try {
                                    jabberStarted = get();
                                } catch (Exception ex) {
                                }
                                if (jabberStarted) {
                                    loggerButton.setEnabled(true);
                                } else {
                                    jabberButton.setSelected(false); // rollback
                                    databaseButton.setEnabled(true);
                                }
                            }
                        }.execute();
                    } else {
                        loggerButton.setEnabled(false);
                        botApp.logout(); // TODO: in SwingWorker
                    }
                // logger button - start/stop logger
                } else if ("logger".equals(e.getActionCommand())) {
                    boolean selected = loggerButton.isSelected();
                    jabberButton.setEnabled(!selected);
                    // TODO: use SwingWorker
                    BotApplication botApp = app.getBotApp();
                    if (selected) {
                        botApp.login();
                    } else {
                        botApp.logout();
                    }
                }
            }
        };
        // TODO: a second click on this button while connecting to the database
        // should cancel the action (SwingWorker.cancel())
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

            // reload charts
            public void actionPerformed(ActionEvent e) {
                // refresh charts
                chartsPanel.reloadData();
            // TODO: support progress - print status changes somewhere
            }
        });

        toolBar.add(configButton);
        toolBar.add(databaseButton);
        toolBar.add(jabberButton);
        toolBar.add(loggerButton);
        toolBar.add(refreshButton);
        mainPane.add(toolBar, BorderLayout.NORTH);

        JPanel contentsPanel = new JPanel();
        contentsPanel.setLayout(new BoxLayout(contentsPanel, BoxLayout.PAGE_AXIS));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.LINE_AXIS));

        infoPanel.add(new JLabel("Jabber server: "));
        jabberConfigServerLabel = new JLabel(System.getProperty("jacomo.jabberServer"));
        infoPanel.add(jabberConfigServerLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        infoPanel.add(new JLabel("Username: "));
        jabberConfigUsernameLabel = new JLabel(System.getProperty("jacomo.jabberUser"));
        infoPanel.add(jabberConfigUsernameLabel);
        infoPanel.add(Box.createHorizontalGlue());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        contentsPanel.add(infoPanel);
        chartsPanel = new ChartsPanel(this);
        contentsPanel.add(new JScrollPane(chartsPanel));
//        chartPanel.add(new JLabel("intervals"));
//        chartPanel.add(new JLabel("hour histogram"));
//        chartPanel.add(new JLabel("weekday histogram"));
        mainPane.add(contentsPanel, BorderLayout.CENTER);

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
    private ChartsPanel chartsPanel;
}
