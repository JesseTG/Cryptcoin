package jtg.cse260.cryptcoin.node;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.starkeffect.cryptcoin.protocol.Message;

import jtg.cse260.cryptcoin.node.MessageHandler;

public class MessageHandler
{
    private static MessageHandler instance;
    
    static {
        instance = new MessageHandler();
    }
    
    private MessageHandler() {
        
    }

    /**
     * Returns the lone MessageHandler instance.
     * 
     * @return The MessageHandler
     */
    public static MessageHandler getInstance() {
        return instance;
    }

    /**
     * Operation handleMessage Acts upon any message that relies on data shared
     * between nodes (e.g. available
     * 
     * @param messageOut -
     * @param messageIn -
     * @param message -
     */
    public void handleMessage(ObjectOutputStream messageOut, ObjectInputStream messageIn, Message message) {}
}
