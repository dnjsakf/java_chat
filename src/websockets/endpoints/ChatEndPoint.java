package websockets.endpoints;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import websockets.WebSocket;
import websockets.common.Const;
import websockets.common.JsonDecoder;
import websockets.common.JsonEncoder;
import websockets.exceptions.AlreadyUsedException;
import websockets.models.ErrorMessage;
import websockets.models.Message;


@ServerEndpoint(
    value="/chat/{room}",
    subprotocols = { "chat-ws" },
    decoders = JsonDecoder.class, 
    encoders = JsonEncoder.class,
    configurator = ChatEndPoint.Configurator.class
)
public class ChatEndPoint {
 
    private static Set<ChatEndPoint> chatEndPoints = new CopyOnWriteArraySet<>();
    private static HashMap<String, String> users = new HashMap<>();
    
    private Session session = null;
    private String room = null;
    private String username = null;
    
    // For Development
    public static void main(String[] args) {
        WebSocket.runServer();
    }
    
    public String getRoom() {
        return this.room;
    }
    public String getUsername() {
        return this.username;
    }

    public static Map<String, String> getQueryMap(String query) {
        Map<String, String> map = new HashMap<>();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] nameval = param.split("=");
                map.put(nameval[0], nameval[1]);
            }
        }
        return map;
    }

    public void clearDatas() {
        users.remove(session.getId());
        chatEndPoints.remove(this);
        
        this.session = null;
        this.room = null;
        this.username = null;
    }
 
    @OnOpen
    public void onOpen(
            final Session session,
            final @PathParam("room") String room
        ) throws AlreadyUsedException, IOException, EncodeException {
        
        Map<String, String> queryParams = getQueryMap(session.getQueryString());
        String username = queryParams.get("username");
        
        System.out.printf("Connecting... %s : %s : %s : %s\n", room, username, session.getId(), queryParams.toString());
        
        // Check User
        synchronized (chatEndPoints) {
            synchronized (users) {
                Iterator<ChatEndPoint> iter = chatEndPoints.iterator();
                
                while(iter.hasNext()) {
                    ChatEndPoint other = iter.next();
                                        
                    if( other.getUsername().equals(username) ) {
                        throw new AlreadyUsedException(Const.ERROR_ALREADY_USED_NAME_MESSAGE);
                    }
                }
                
                users.put(session.getId(), username);
            }
            chatEndPoints.add(this);
        }

        // Save Session
        session.setMaxIdleTimeout(5*60*1000);
        session.setMaxBinaryMessageBufferSize(10*1024*1024);
        
        this.session = session;
        this.room = room;
        this.username = username;
        
        // Create Message
        Message message = new Message();
        message.setFrom(Const.AGENT_NAME);
        message.setContent("Connected!");
        
        System.out.printf("Connected!!! %s : %s\n", username, session.getId());
        
        // Send Message
        // broadcast(message);
    }
    
    @OnClose
    public void onClose(final Session session, final CloseReason reason) throws IOException, EncodeException {
        // Create Message
        Message message = new Message();
        message.setFrom(Const.AGENT_NAME);
        message.setContent("Disconnected!");
        
        // Clear Data
        clearDatas();

        // Send Message
        broadcast(message);
        
        // Closed Session
        session.close(reason);
    }
 
    @OnError
    public void onError(final Session session, final Throwable throwable)  throws IOException, EncodeException {
        if( !session.isOpen() ) {
            return;
        }
        
        boolean closing = Const.ERROR_CLOSING;
        String content = throwable.getMessage();
        
        System.out.printf("[%s] error: %s\n", this.username, content);
        
        // Create Error Message
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setFrom(Const.AGENT_NAME);
        errorMessage.setContent(content);
        
        // Set Error Message
        if( throwable instanceof AlreadyUsedException  ) {
            errorMessage.setCode(Const.ERROR_ALREADY_USED_CODE);
            errorMessage.setError(Const.ERROR_ALREADY_USED_MESSAGE);
            
            closing = true;
        } else {
            errorMessage.setCode(Const.ERROR_CODE);
            errorMessage.setError(Const.ERROR_MESSAGE);
        }
        
        // Return Message
        session.getBasicRemote().sendObject(errorMessage);
        
        // Closed Session
        if( closing ) {
            session.close();
        }
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
    public void onMessage(final Session session, final Message message) throws IOException, EncodeException {
        System.out.printf("[%s] receive message: %s\n", this.username, message.toString());
        
        String to = message.getTo();
//        String from = users.get(session.getId());

        // Set Message
        message.setFrom(this.username);
        
        if( to != null ) {
            // Specific Target
        } else {
            broadcast(message);
        }
    }
    
    private void broadcast(Message message) throws IOException, EncodeException {
        chatEndPoints.forEach(endpoint -> {
            synchronized (endpoint) {
                try {
                    System.out.printf("Send To %s.\n", users.get(endpoint.session.getId()));
                    
                    endpoint.session.getBasicRemote().sendObject(message);
                    
                } catch (IOException | EncodeException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    public static class Configurator extends ServerEndpointConfig.Configurator {
        @Override
        public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            T endpoint = super.getEndpointInstance(endpointClass);

            if (endpoint instanceof ChatEndPoint) {
                return endpoint;
            }
            
            throw new InstantiationException(
                MessageFormat.format(
                    "Expected instanceof \"{0}\". Got instanceof \"{1}\".",
                    ChatEndPoint.class, 
                    endpoint.getClass()
                )
            );
        }
    }
}