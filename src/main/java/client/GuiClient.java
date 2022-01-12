/*
MIT License

Copyright (c) 2022 Sapaev Murat

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

public class GuiClient {
    private final JTextArea usersField = new JTextArea(20, 20);
    private final JTextArea msgField = new JTextArea(20, 20);
    private final JFrame frame = new JFrame("Chat");
    private final JTextField textField = new JTextField(40);
    private final JButton buttonConnect = new JButton("Connect");
    private final JButton buttonDisconnect = new JButton("Disconnect");
    private final JButton buttonClearChat = new JButton("Clear Chat");
    private final JPanel panelWorking = new JPanel();
    private final Client client;

    public GuiClient(Client client) {
        this.client = client;
    }

    protected void initFrameClient() {
        msgField.setEditable(false);
        msgField.setLineWrap(true);

        usersField.setEditable(false);
        usersField.setLineWrap(true);

        panelWorking.add(textField);
        panelWorking.add(buttonConnect);
        panelWorking.add(buttonDisconnect);
        panelWorking.add(buttonClearChat);
        setWorkingStatusButtons(false);

        frame.add(new JScrollPane(msgField), BorderLayout.CENTER);
        frame.add(new JScrollPane(usersField), BorderLayout.EAST);
        frame.add(panelWorking, BorderLayout.SOUTH);
        frame.pack(); //Optimal window size
        frame.setLocationRelativeTo(null); //The position of the window in the center
        frame.setVisible(true);

        //Disconnecting from the server and stopping the program
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                client.disconnectFromServer();
                System.exit(0);
            }
        });

        buttonConnect.addActionListener(e -> client.connectToServer());

        buttonDisconnect.addActionListener(e -> client.disconnectFromServer());

        buttonClearChat.addActionListener(e -> msgField.setText(""));

        textField.addActionListener(e -> {
            client.sendMessageOnServer(textField.getText());
            textField.setText("");
        });
    }

    //If the client isn't connected, then the "Disconnect" button and the "textField" are disabled
    // and the "Connect" button is Enabled and vice versa
    protected void setWorkingStatusButtons(boolean isEnabled) {
        buttonConnect.setEnabled(!isEnabled);
        buttonDisconnect.setEnabled(isEnabled);
        textField.setEnabled(isEnabled);
    }

    protected void addMsgToMsgField(String text) {
        msgField.append(text + "\n");
    }

    protected void refreshUsersField(Set<String> listOfUsers) {
        usersField.setText("");
        if(listOfUsers.size() > 0) {
            usersField.append("Total connected users: " + listOfUsers.size() + "\n");
            listOfUsers.forEach(user -> usersField.append(user + "\n"));
        }
    }

    protected String getServerAddressFromOptionPane() {
        String addressServer = JOptionPane.showInputDialog(frame, "Enter the server address:", "The server address",
                JOptionPane.QUESTION_MESSAGE);
        return addressServer.trim();
    }

    protected int getPortServerFromOptionPane() {
        String port = JOptionPane.showInputDialog(frame, "Enter the server port:", "The server port",
                JOptionPane.QUESTION_MESSAGE);
        return Integer.parseInt(port.trim());
    }

    protected String getNameUser() {
        String nameUser = JOptionPane.showInputDialog(frame, "Enter the user name:", "User name",
                JOptionPane.QUESTION_MESSAGE);
        return nameUser.replaceAll("\\s+", " "); //Remove the extra spaces in the nameUser
    }

    protected void errorDialogWindow(String text) {
        JOptionPane.showMessageDialog(frame, text, "Error", JOptionPane.ERROR_MESSAGE);
    }

}
