package com.example.project_dam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.Manifest;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class MainActivity extends AppCompatActivity implements WebAppInterface {
    private WebView webView;
    private WebSocket webSocket;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
        }

        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);

        WebSocketManager.getInstance().initiateSocketConnection( new MainActivity.SocketListener());
        webSocket = WebSocketManager.getInstance().getWebSocket();
        WebSocketManager.getInstance().setWebSocket(webSocket);
        webView.setWebViewClient(new WebViewClient());

        webView.addJavascriptInterface(this, "Android");

        webView.loadUrl("file:///android_asset/index.html");
    }

    private class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            super.onOpen(webSocket, response);

            runOnUiThread(() -> Toast.makeText(MainActivity.this,
                    "Socket Connection Successful!",
                    Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket, text);

            runOnUiThread(() -> {
                try {
                    JSONObject json = new JSONObject(text);
                    String name = json.getString("name");
                    String message = json.getString("message");

                    if ("Server".equals(name)) {
                        if (message.startsWith("Video title clicked: ")) {
                            String videoId = message.substring("Video title clicked: ".length());
                            onVideoClicked(videoId);
                        } else if (message.startsWith("Searched for video: ")) {
                            String javascriptCode = "search();";
                            webView.evaluateJavascript(javascriptCode, null);
                        } else if ((message.startsWith("Videos loaded"))) {
                            String javascriptCode = "fetchRandomVideosFromPlaylist(3)";
                            webView.evaluateJavascript(javascriptCode, null);
                        } else if ((message.startsWith("Chat page loaded"))) {
                            loadChatPage();
                        } else if ((message.startsWith("Main page loaded"))) {
                            loadMainPage();
                        } else if (message.startsWith("Displayed videos:")) {
                            String videosString = message.split("Displayed videos: ")[1].trim();
                            String javascriptCode = "displayVideos(" + videosString + ");";
                            webView.evaluateJavascript(javascriptCode, null);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    @JavascriptInterface
    public void sendMessageToServer(String action, String data) {
        JSONObject jsonMessage = new JSONObject();
        try {
            jsonMessage.put("action", action);
            jsonMessage.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        String message = jsonMessage.toString();
        if (webSocket != null && webSocket.send(message)) {
            runOnUiThread(() -> Toast.makeText(MainActivity.this,
                    "Message sent!",
                    Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    @JavascriptInterface
    public void loadMainPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    @JavascriptInterface
    public void loadChatPage() {
        Intent intent = new Intent(this, JoinChatActivity.class);
        startActivity(intent);
    }

    @Override
    @JavascriptInterface
    public void onVideoClicked(String videoId) {
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra("videoId", videoId);
        startActivity(intent);
    }

    @Override
    @JavascriptInterface
    public void enterRoom(String name) {
    }

    @Override
    @JavascriptInterface
    public void openChannel(String channelId){
    }

    @Override
    @JavascriptInterface
    public void sendMessage(String message) {
    }

    @Override
    @JavascriptInterface
    public void loadImage() {
    }

}