package websockets.endpoints;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import websockets.message.Message;
import websockets.message.MessageDecoder;
import websockets.message.MessageEncoder;

/**
 *  Exception Handler
 */
class AlreayUsedUsername extends Exception {
    private static final long serialVersionUID = 1L;
    
    public AlreayUsedUsername() {
        super("Already used username.");
    }
    public AlreayUsedUsername(String message) {
        super(message);
    }
}

@ServerEndpoint(
    value="/chat/{username}", 
    decoders = MessageDecoder.class, 
    encoders = MessageEncoder.class
)
public class ChatEndPoint {
 
    private static Set<ChatEndPoint> chatEndPoint = new CopyOnWriteArraySet<>();
    private static HashMap<String, String> users = new HashMap<>();
    private static Set<String> usernameSet = new HashSet<String>();
    
    private Session session;
 
    @OnOpen
    public void onOpen(
            Session session,
            @PathParam("username") String username
            ) throws AlreayUsedUsername, IOException, EncodeException {
        
        System.out.printf("Connecting... %s : %s\n", username, session.getId());
        
        // Check User
        if( usernameSet.contains(username) ) {
            System.out.printf("Alreay used username. %s : %s\n", username, session.getId());
            throw new AlreayUsedUsername();
        }
        System.out.printf("Connected!!! %s : %s\n", username, session.getId());
        usernameSet.add(username);
        
        // Add User
        users.put(session.getId(), username);
        
        // Save My Session
        this.session = session;
        
        // Save My EndPoint
        chatEndPoint.add(this);
 
        // Make Message
        Message message = new Message();
        message.setFrom("System");
        message.setContent("Connected!");
        
        // Send Message
        broadcast(message);
    }
    
    @OnClose
    public void onClose(Session session) throws IOException, EncodeException {
        chatEndPoint.remove(this);

        // Make Message
        Message message = new Message();
        message.setFrom(users.get(session.getId()));
        message.setContent("Disconnected!");

        // Send Message
        broadcast(message);
    }
 
    @OnError
    public void onError(Session session, Throwable throwable)  throws IOException, EncodeException {
        System.out.println("error: "+ throwable.getMessage());
        
        // Make Message
        Message message = new Message();
        message.setFrom("System");
        message.setContent(throwable.getMessage());
        
        // Send Message
        broadcast(message);
    }
    
    /**
     * message:
     *  -> Input        - JSON String
     *  -> Decoder      - String to JSON(Message)
     *  -> onMessage    - Message
     *  -> Encoder      - JSON(Message) to String
     *  -> Output       - JSON String
     */
    @OnMessage
    public void onMessage(Session session, Message message) throws IOException, EncodeException {
        System.out.println("receive message: "+ message.toString());
        
        String to = message.getTo();
        String from = users.get(session.getId());

        // Set Message
        message.setFrom(from);
        
        if( to != null ) {
            // Specific Target
            sendTo(to, message);
        } else {
            // Broadcast
            broadcast(message);
        }
    }
    
    private static void sendTo(String to, Message message) throws IOException, EncodeException {
        chatEndPoint.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    if( to.equals(users.get(endpoint.session.getId())) ) {
                        endpoint.session.getBasicRemote().sendObject(message);
                    }
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private static void broadcast(Message message) throws IOException, EncodeException {
        chatEndPoint.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    endpoint.session.getBasicRemote().sendObject(message);
                    
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}