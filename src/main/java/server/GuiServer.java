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

package server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class GuiServer {
    private final JTextArea infoField = new JTextArea(15, 40);
    private final JFrame frame = new JFrame("server");
    private final JButton buttonStartServer = new JButton("Start Server");
    private final JButton buttonStopServer = new JButton("Stop Server");
    private final JButton buttonSendMessage = new JButton("Send message");
    private final JPanel panelButtons = new JPanel();
    private final Server server;

    public GuiServer(Server server) {
        this.server = server;
    }

    protected void initFrameServer() {
        infoField.setEditable(false);
        infoField.setLineWrap(true);

        panelButtons.add(buttonStartServer);
        panelButtons.add(buttonStopServer);
        panelButtons.add(buttonSendMessage);
        setWorkingStatusButtons(false);

        frame.add(new JScrollPane(infoField), BorderLayout.CENTER);
        frame.add(panelButtons, BorderLayout.SOUTH);
        frame.pack(); //Optimal window size
        frame.setLocationRelativeTo(null); //The position of the window in the center
        frame.setVisible(true);

        //Closing the server and stopping the program
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                server.stopServer();
                System.exit(0);
            }
        });

        buttonStartServer.addActionListener(e -> {
            try {
                String port = JOptionPane.showInputDialog(frame, "Enter the server port (1025-65535):",
                        "The Server port", JOptionPane.QUESTION_MESSAGE);
                server.startServer(Integer.parseInt(port.trim()));
            } catch(Exception err) {
                JOptionPane.showMessageDialog(frame, "Maybe you've entered the characters.", "Incorrect server port",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonStopServer.addActionListener(e -> server.stopServer());

        buttonSendMessage.addActionListener(e -> {
            String messageFromSerer = JOptionPane.showInputDialog(frame, "Enter your message:",
                    "Message from the server", JOptionPane.QUESTION_MESSAGE);
            server.sendMsgFromServer(messageFromSerer);
        });
    }

    //If the server isn't working, then the "Stop" and the "Send" buttons are disabled
    // and the "Start" button is Enabled and vice versa
    protected void setWorkingStatusButtons(boolean isEnabled) {
        buttonStartServer.setEnabled(!isEnabled);
        buttonStopServer.setEnabled(isEnabled);
        buttonSendMessage.setEnabled(isEnabled);
    }

    public void addMsgToInfoField(String serviceMessage) {
        infoField.append(serviceMessage + ".\n");
    }
}