package websockets.at.htl;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat/{username}")
@ApplicationScoped
public class ChatSocket {

    Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("Username") String username) {
        sessions.put(username, session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("username") String username, Throwable throwable) {
        sessions.remove(username);
        broadcast("User " + username + " left on error: " + throwable);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("username") String username) {
        if (message.equalsIgnoreCase(("_ready_"))) {
            broadcast("User " + username + " joined");
        } else {
            broadcast(">> " + username + ": " + message);
        }
    }

    private void broadcast(String message) {
        sessions.values().forEach(s -> {
            s.getAsyncRemote().sendObject(message, result -> {
                System.out.println("Unable to send message: " + result.getException());
            });
        });
    }
}
