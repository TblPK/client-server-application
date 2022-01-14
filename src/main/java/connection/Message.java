package connection;

import java.io.Serializable;
import java.util.Set;

//The class that creates the message type
public class Message implements Serializable {
    private final String textMessage;
    private final MessageType typeMessage;
    private final Set<String> listOfUsers;

    public Message(MessageType typeMessage) {
        this.textMessage = null;
        this.typeMessage = typeMessage;
        this.listOfUsers = null;
    }

    public Message(MessageType typeMessage, String textMessage) {
        this.typeMessage = typeMessage;
        this.textMessage = textMessage;
        this.listOfUsers = null;
    }

    public Message(MessageType typeMessage, Set<String> listOfUsers) {
        this.textMessage = null;
        this.typeMessage = typeMessage;
        this.listOfUsers = listOfUsers;
    }

    public MessageType getTypeMessage() {
        return typeMessage;
    }

    public Set<String> getListOfUsers() {
        return listOfUsers;
    }

    public String getTextMessage() {
        return textMessage;
    }

}
