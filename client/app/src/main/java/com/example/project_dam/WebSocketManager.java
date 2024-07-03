package com.example.project_dam;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketManager {
    private static WebSocketManager instance;
    private OkHttpClient client;
    private WebSocket webSocket;
    private final String SERVER_PATH = "ws://10.0.2.2:6557";

    private WebSocketManager() {
        client = new OkHttpClient();
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public OkHttpClient getOkHttpClient() {
        return client;
    }

    public void initiateSocketConnection(WebSocketListener socketListener) {
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, socketListener);
    }

    public void changeListener(WebSocketListener newSocketListener) {
        if (webSocket != null) {
            webSocket.close(1000, "Changing listener");

            Request request = new Request.Builder().url(SERVER_PATH).build();
            webSocket = client.newWebSocket(request, newSocketListener);
        }
    }
}
