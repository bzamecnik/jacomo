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
import javax.swing.JTextField;

/**
 *
 * @author Bohouš
 */
public class ConfigDialog extends JDialog implements ActionListener {
    public ConfigDialog(Frame frame) {
        super(frame, "Configuration", true);

        jabberServerTextField = new JTextField(
                System.getProperty("jacomo.jabberServer"));
        jabberUsernameTextField = new JTextField(
                System.getProperty("jacomo.jabberUser"));
        jabberPasswordField = new JPasswordField(
                System.getProperty("jacomo.jabberPassword"));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        final JButton okButton = new JButton("OK");
        okButton.setActionCommand("OK");
        okButton.addActionListener(this);
        getRootPane().setDefaultButton(okButton);

        JPanel mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.PAGE_AXIS));
        mainPane.add(new JLabel("Jabber server:"));
        mainPane.add(jabberServerTextField);
        mainPane.add(new JLabel("Username:"));
        mainPane.add(jabberUsernameTextField);
        mainPane.add(new JLabel("Password:"));
        mainPane.add(jabberPasswordField);
        mainPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

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

    public void actionPerformed(ActionEvent e) {
        if ("OK".equals(e.getActionCommand())) {
            confirmed = true;
            jabberServer = jabberServerTextField.getText();
            jabberUsername = jabberUsernameTextField.getText();
            jabberPassword = jabberPasswordField.getPassword();
        }
        setVisible(false);
    }

    private JTextField jabberServerTextField;
    private JTextField jabberUsernameTextField;
    private JPasswordField jabberPasswordField;

    private String jabberServer = "";
    private String jabberUsername = "";
    private char[] jabberPassword;
    private boolean confirmed;

    /**
     * @return the jabberServer
     */
    public String getJabberServer() {
        return jabberServer;
    }

    /**
     * @return the jabberUsername
     */
    public String getJabberUsername() {
        return jabberUsername;
    }

    /**
     * @return the jabberPassword
     */
    public char[] getJabberPassword() {
        return (jabberPassword != null) ? jabberPassword : new char[0];
    }

    public void clearJabberPassword() {
        if (jabberPassword != null) {
            Arrays.fill(jabberPassword, '0');
        }
    }

    /**
     * @return the confirmed
     */
    public boolean isConfirmed() {
        return confirmed;
    }
}
