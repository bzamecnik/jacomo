package org.zamecnik.jacomo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

/**
 * A dialog for setting some configuration data. Currently it is used to
 * configure Jabber account information. If the dialog was confirmend
 * ({@link #isConfirmed()} == true) the data can be retrieved using following
 * functions:
 * {@link #getJabberServer()}, {@link #getJabberUsername()},
 * {@link #getJabberPassword()}.
 *
 * @author Bohumir Zamecnik
 */
public class ConfigDialog extends JDialog implements ActionListener {
    /**
     * ConfigDialog contructor.
     * @param frame parent Frame which will own this dialog
     */
    public ConfigDialog(Frame frame) {
        super(frame, "Configuration", true);

        jabberServerTextField = new JTextField(
                System.getProperty("jacomo.jabberServer"));
        jabberUsernameTextField = new JTextField(
                System.getProperty("jacomo.jabberUser"));
        jabberPasswordField = new JPasswordField(
                System.getProperty("jacomo.jabberPassword"));
        contactFilterTextArea = new JTextArea(
                System.getProperty("jacomo.jabberContactsFilter"), 15, 20);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        final JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        getRootPane().setDefaultButton(okButton);

        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
        JPanel jabberPane = new JPanel();
        jabberPane.setLayout(new BoxLayout(jabberPane, BoxLayout.PAGE_AXIS));
        jabberPane.add(new JLabel("Jabber server:"));
        jabberPane.add(jabberServerTextField);
        jabberPane.add(new JLabel("Username:"));
        jabberPane.add(jabberUsernameTextField);
        jabberPane.add(new JLabel("Password:"));
        jabberPane.add(jabberPasswordField);
        jabberPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 5, 10),
                BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Jabber credentials")));
        mainPane.add(jabberPane);

        JPanel contactFilterPane = new JPanel();
        contactFilterPane.setLayout(new BoxLayout(contactFilterPane,
                BoxLayout.PAGE_AXIS));
        contactFilterPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(5, 10, 10, 10),
                BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Jabber contact filter")));
        contactFilterTextArea.setToolTipText(
                "Each line is a match critirium. Contacts containg " +
                "anything from here will be excluded from logging.");
        contactFilterPane.add(new JScrollPane(contactFilterTextArea));
        mainPane.add(contactFilterPane);

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.LINE_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(okButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(cancelButton);

        Container contentPane = getContentPane();
        contentPane.add(mainPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.PAGE_END);
    }

    /**
     * Dialog button click handler.
     * @param e action event
     */
    public void actionPerformed(ActionEvent e) {
        // OK button clicked, set the
        if ("OK".equals(e.getActionCommand())) {
            confirmed = true;
            jabberServer = jabberServerTextField.getText();
            jabberUsername = jabberUsernameTextField.getText();
            jabberPassword = jabberPasswordField.getPassword();
            contactFilter = contactFilterTextArea.getText();
            PropertiesHelper.setJabberProperties(
                            jabberServer, jabberUsername,
                            new String(jabberPassword));
            System.setProperty("jacomo.jabberContactsFilter", contactFilter);
//            System.out.println("jacomo.jabberContactsFilter:\n" +
//                    System.getProperty("jacomo.jabberContactsFilter", ""));
        }
        setVisible(false);
    }

    private JTextField jabberServerTextField;
    private JTextField jabberUsernameTextField;
    private JPasswordField jabberPasswordField;
    private JTextArea contactFilterTextArea;

    private String jabberServer = "";
    private String jabberUsername = "";
    private char[] jabberPassword;
    private boolean confirmed;
    private String contactFilter = "";

    /**
     * Get Jabber server.
     * @return jabber server
     */
    public String getJabberServer() {
        return jabberServer;
    }

    /**
     * Get Jabber username.
     * @return jabber username
     */
    public String getJabberUsername() {
        return jabberUsername;
    }

    /**
     * Get Jabber password. Char array is returned instead of String for
     * security reasons (it can be cleaned).
     * @return jabber password
     */
    public char[] getJabberPassword() {
        // Security note (TODO):
        // There is still a hole in setting default value for password and
        // in saving the password to a config file on disk.
        return (jabberPassword != null) ? jabberPassword : new char[0];
    }

    /**
     * Clear Jabber password configured using the dialog.
     */
    public void clearJabberPassword() {
        if (jabberPassword != null) {
            Arrays.fill(jabberPassword, '0');
        }
    }

    /**
     * Check if the dialog was confirmed and getting the data configured is
     * possible.
     * @return true if it was confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * Get contact filter criteria.
     * @return contact filter criteria
     */
    public String getContactFilter() {
        return contactFilter;
    }
}
