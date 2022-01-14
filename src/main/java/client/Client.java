package client;

import connection.Connection;
import connection.Message;
import connection.MessageType;

import java.net.Socket;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());
    private static GuiClient gui;
    private static Set<String> listOfUsers;
    private static volatile boolean isConnection = false;
    private Connection connection;

    public static void main(String[] args) {
        Client client = new Client();
        listOfUsers = new TreeSet<>();
        gui = new GuiClient(client);
        gui.initFrameClient();

        while(true) {
            //Waiting 'true' from the 'connectToServer' method
            if(isConnection) {
                try {
                    client.nameUserRegistration();
                    client.receiveMessageFromServer();
                } catch(Exception e) {
                    logger.log(Level.INFO, e.getMessage());
                    gui.errorDialogWindow("Error: the connection's been lost. Please, try reconnecting.");
                    gui.setWorkingStatusButtons(false);
                    isConnection = false;
                }
            }
        }
    }

    protected void connectToServer() {
        try {
            String addressServer = gui.getServerAddressFromOptionPane();
            int port = gui.getPortServerFromOptionPane();
            Socket socket = new Socket(addressServer, port);
            connection = new Connection(socket);
            gui.setWorkingStatusButtons(true);
            isConnection = true;
        } catch(Exception e) {
            logger.log(Level.INFO, e.getMessage());
            gui.errorDialogWindow("Invalid address or port. Please, try again.");
        }
    }

    protected void disconnectFromServer() {
        if(isConnection) {
            try {
                connection.send(new Message(MessageType.DISCONNECT_USER));
                gui.setWorkingStatusButtons(false);
                isConnection = false;
                listOfUsers.clear();
                gui.refreshUsersField(listOfUsers);
            } catch(Exception e) {
                gui.refreshUsersField(listOfUsers);
                logger.log(Level.INFO, e.getMessage());
                gui.errorDialogWindow("Error: when disconnecting. Try reconnecting.");
            }
        }
    }

    protected void sendMessageOnServer(String text) {
        try {
            connection.send(new Message(MessageType.TEXT_MESSAGE, text));
        } catch(Exception e) {
            logger.log(Level.INFO, e.getMessage());
            gui.errorDialogWindow("Error: sending a new message. Try reconnecting.");
        }
    }

    protected void nameUserRegistration() throws Exception {
        while(true) {

            //Waiting for a message-request from the server
            Message message = connection.receive();

            if(message.getTypeMessage() == MessageType.REQUEST_NAME_USER) {
                String nameUser = gui.getNameUser();
                connection.send(new Message(MessageType.USER_NAME, nameUser));

            } else if(message.getTypeMessage() == MessageType.NAME_USED) {
                gui.errorDialogWindow("This name is used.");

            } else if(message.getTypeMessage() == MessageType.NAME_ACCEPTED) {
                listOfUsers.addAll(message.getListOfUsers());
                break;

            } else {
                gui.errorDialogWindow("Error: TypeMessage isn't correct");
            }

        }
    }

    protected void receiveMessageFromServer() throws Exception {
        while(true) {

            //Waiting for a message-request from the server
            Message message = connection.receive();

            if(message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                gui.addMsgToMsgField(message.getTextMessage());

            } else if(message.getTypeMessage() == MessageType.MESSAGE_FROM_SERVER) {
                gui.addMsgToMsgField("Server: " + message.getTextMessage());

            } else if(message.getTypeMessage() == MessageType.NEW_USER_ADDED) {
                listOfUsers.add(message.getTextMessage());
                gui.refreshUsersField(listOfUsers);
                gui.addMsgToMsgField("Server: " + message.getTextMessage() + " is connected");

            } else if(message.getTypeMessage() == MessageType.REMOVED_USER) {
                listOfUsers.remove(message.getTextMessage());
                gui.refreshUsersField(listOfUsers);
                gui.addMsgToMsgField("Server: " + message.getTextMessage() + " is disconnected");
            } else if(message.getTypeMessage() == MessageType.CONFIRM_DISCONNECT) {
                break;
            } else {
                gui.errorDialogWindow("Error: TypeMessage isn't correct");
            }
        }
    }
}
