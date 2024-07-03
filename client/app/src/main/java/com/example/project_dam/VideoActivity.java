package com.example.project_dam;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class VideoActivity extends AppCompatActivity implements WebAppInterface {

    WebSocket webSocket;
    WebView webView;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String videoId = getIntent().getStringExtra("videoId");

        webView = new WebView(this);
        setContentView(webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(this, "Android");

        WebSocketManager.getInstance().changeListener(new VideoActivity.SocketListener());
        webSocket = WebSocketManager.getInstance().getWebSocket();

        webView.loadUrl("file:///android_asset/video.html?videoId=" + videoId);
    }

    private class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            super.onOpen(webSocket, response);

            runOnUiThread(() -> Toast.makeText(VideoActivity.this,
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
                        if (message.startsWith("Opened channel: ")) {
                            String chanelId = message.substring("Opened channel: ".length());
                            openChannel(chanelId);
                        } else if ((message.startsWith("Loaded info for video: "))) {
                            String javascriptCode = "fetchVideoInformation()";
                            webView.evaluateJavascript(javascriptCode, null);
                        } else if ((message.startsWith("Comments fetched"))) {
                            String javascriptCode = "fetchComments()";
                            webView.evaluateJavascript(javascriptCode, null);
                        } else if ((message.startsWith("Fetched number of likes"))) {
                            String javascriptCode = "updateLikeCount()";
                            webView.evaluateJavascript(javascriptCode, null);
                        } else if ((message.startsWith("Chat page loaded"))) {
                            loadChatPage();
                        } else if ((message.startsWith("Main page loaded"))) {
                            loadMainPage();
                        } if ((message.startsWith("Video liked"))) {
                            String javascriptCode = "like()";
                            webView.evaluateJavascript(javascriptCode, null);
                        } else if ((message.startsWith("Commented on video"))) {
                            String javascriptCode = "comment()";
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
    public void openChannel(String channelId){
        Intent intent = new Intent(this, ChannelActivity.class);
        intent.putExtra("channelId", channelId);
        startActivity(intent);
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
            runOnUiThread(() -> Toast.makeText(VideoActivity.this,
                    "Message sent!",
                    Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    @JavascriptInterface
    public void onVideoClicked(String videoId) {
    }

    @Override
    @JavascriptInterface
    public void enterRoom(String name) {
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
