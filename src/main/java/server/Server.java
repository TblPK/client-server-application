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

import connection.Connection;
import connection.Message;
import connection.MessageType;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Server {
    private static GuiServer gui;
    private static volatile boolean isServerRunning = false;
    private static Map<String, Connection> listOfUsers;
    private ServerSocket serverSocket;

    public static void main(String[] args) {
        Server server = new Server();
        listOfUsers = new HashMap<>();
        gui = new GuiServer(server);
        gui.initFrameServer();

        while(true) {
            //Waiting 'true' from the 'startServer' method
            if(isServerRunning) {
                server.acceptServer();
            }
        }
    }

    protected void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            gui.setWorkingStatusButtons(true);
            isServerRunning = true;
            gui.addMsgToInfoField("The server is running. Server port: " + serverSocket.getLocalPort());
        } catch(Exception e) {
            gui.addMsgToInfoField("Error: " + e.getLocalizedMessage());
        }
    }

    protected void stopServer() {
        if(isServerRunning) {
            try {
                for(Map.Entry<String, Connection> user: listOfUsers.entrySet()) {
                    try {
                        user.getValue().close();
                    } catch(Exception err) {
                        gui.addMsgToInfoField("Error: " + err.getMessage());
                    }
                }
                serverSocket.close();
                listOfUsers.clear();
                gui.setWorkingStatusButtons(false);
                isServerRunning = false;
                gui.addMsgToInfoField("The server is stopped: " + serverSocket.getLocalPort());
            } catch(Exception e) {
                gui.addMsgToInfoField("Error: " + e.getMessage());
            }
        }

    }

    //The endless cycle of accepting connections and sending a new socket instance to a new thread
    private void acceptServer() {
        while(true) {
            try {
                Socket socket = serverSocket.accept();
                new ServerThread(socket).start();
            } catch(Exception e) {
                gui.addMsgToInfoField("Accepting: " + e.getMessage());
                gui.setWorkingStatusButtons(false);
                isServerRunning = false;
                break;
            }
        }
    }

    private void sendMsgAllUsers(Message message) {
        for(Map.Entry<String, Connection> user: listOfUsers.entrySet()) {
            try {
                user.getValue().send(message);
            } catch(Exception e) {
                gui.addMsgToInfoField("Error: " + e.getMessage() + " to " + user.getValue());
            }
        }
    }

    protected void sendMsgFromServer(String text) {
        sendMsgAllUsers(new Message(MessageType.MESSAGE_FROM_SERVER, text));
    }

    //The class-thread that starts when the server accepts a new socket connection from the 'acceptServer' method
    private class ServerThread extends Thread {

        private final Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        @Override //Starts with the start() method from the 'acceptServer' method
        public void run() {
            gui.addMsgToInfoField("The user's connected: " + socket.getRemoteSocketAddress());
            try {
                Connection connection = new Connection(socket);
                String userName = requestAndAddingUser(connection);
                messagingBetweenUsers(connection, userName);
            } catch(Exception e) {
                gui.addMsgToInfoField("Error: " + e.getMessage() + " from " + socket.getRemoteSocketAddress());
            }
        }

        private String requestAndAddingUser(Connection connection) throws Exception {
            while(true) {

                //Sending a new message-request to the user
                connection.send(new Message(MessageType.REQUEST_NAME_USER));
                Message Message = connection.receive();
                String userName = Message.getTextMessage();

                if(Message.getTypeMessage() == MessageType.USER_NAME && !userName.isBlank() && !listOfUsers.containsKey(
                        userName) && !userName.equalsIgnoreCase("server")) {
                    listOfUsers.put(userName, connection);
                    Set<String> listUser = new TreeSet<>(listOfUsers.keySet());
                    //Sending the list of connected users to the user
                    connection.send(new Message(MessageType.NAME_ACCEPTED, listUser));
                    //Sending the message about a new user to all users
                    sendMsgAllUsers(new Message(MessageType.NEW_USER_ADDED, userName));
                    return userName;

                } else {
                    connection.send(new Message(MessageType.NAME_USED));
                }
            }
        }

        private void messagingBetweenUsers(Connection connection, String userName) {
            while(true) {
                try {

                    //Receiving a new message-request from the user
                    Message message = connection.receive();

                    if(message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                        String textMessage = userName + ": " + message.getTextMessage();
                        sendMsgAllUsers(new Message(MessageType.TEXT_MESSAGE, textMessage));

                    } else if(message.getTypeMessage() == MessageType.DISCONNECT_USER) {
                        connection.send(new Message(MessageType.CONFIRM_DISCONNECT));
                        listOfUsers.remove(userName);
                        sendMsgAllUsers(new Message(MessageType.REMOVED_USER, userName));
                        gui.addMsgToInfoField("The user's disconnected: " + socket.getRemoteSocketAddress());
                        connection.close();
                        break;

                    } else {
                        gui.addMsgToInfoField("Error: TypeMessage isn't correct");
                    }
                } catch(Exception e) {
                    gui.addMsgToInfoField("Error: " + e.getMessage() + " from " + socket.getRemoteSocketAddress());
                    break;
                }
            }
        }
    }
}