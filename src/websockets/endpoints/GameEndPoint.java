package websockets.endpoints;

import java.io.IOException;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/game")
public class GameEndPoint {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected ... " + session.getId());

    }

    @OnMessage
    public String onMessage(String message, Session session) {

        switch (message) {

        case "quit":

            try {

                session.close(new CloseReason(CloseCodes.NORMAL_CLOSURE, "Game ended"));

            } catch (IOException e) {

                throw new RuntimeException(e);

            }

            break;

        }

        return message;

    }

 

    @OnClose

    public void onClose(Session session, CloseReason closeReason) {

        System.out.println(String.format("Session %s closed because of %s", session.getId(), closeReason));

    }
}
