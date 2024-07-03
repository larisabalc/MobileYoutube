package com.example.project_dam;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatActivity extends AppCompatActivity implements WebAppInterface {

    private String name;
    private WebSocket webSocket;
    private WebView webView;

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        name = getIntent().getStringExtra("name");

        webView = new WebView(this);
        setContentView(webView);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowFileAccess(true);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(this, "Android");

        WebSocketManager.getInstance().changeListener(new ChatActivity.SocketListener());
        webSocket = WebSocketManager.getInstance().getWebSocket();

        webView.loadUrl("file:///android_asset/chat.html");
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        handleImageResult(data);
                    }
                }
            }
    );

    @Override
    @JavascriptInterface
    public void loadImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");

        imagePickerLauncher.launch(intent);
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
            runOnUiThread(() -> Toast.makeText(ChatActivity.this,
                    "Message sent!",
                    Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    @JavascriptInterface
    public void sendMessage(String message) {
        runOnUiThread(() -> {
            Toast.makeText(ChatActivity.this, "Text Message Send: " + message, Toast.LENGTH_SHORT).show();
            String javascriptCode = "addMessageToUI({name: 'You', message: '" + message + "'})";
            webView.evaluateJavascript(javascriptCode, null);
        });
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("message", message);

            webSocket.send(jsonObject.toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @JavascriptInterface
    public void openChannel(String channelId) {
    }

    @Override
    @JavascriptInterface
    public void onVideoClicked(String videoId) {
    }

    @Override
    @JavascriptInterface
    public void enterRoom(String name) {
    }

    private class SocketListener extends WebSocketListener {
        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            super.onOpen(webSocket, response);

            runOnUiThread(() -> Toast.makeText(ChatActivity.this,
                    "Socket Connection Successful!",
                    Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            super.onMessage(webSocket, text);

            runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    boolean isImage = jsonObject.has("image");

                    String javascriptCode;
                    if (isImage) {
                        javascriptCode = "addImageToUI(" + jsonObject.get("image") + ")";
                    } else {
                        javascriptCode = "addMessageToUI({ name: 'Server', message: '" + jsonObject.get("message") + "' })";
                    }
                    webView.evaluateJavascript(javascriptCode, null);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void handleImageResult(Intent data) {
        try {
            InputStream is = getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
            Bitmap image = BitmapFactory.decodeStream(is);

            sendImage(image);

            runOnUiThread(() -> Toast.makeText(ChatActivity.this,
                    "Image Sent Successfully!",
                    Toast.LENGTH_SHORT).show());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendImage(Bitmap originalImage) {
        int targetWidth = 300;
        int targetHeight = (int) (originalImage.getHeight() * (targetWidth / (double) originalImage.getWidth()));

        Bitmap resizedImage = Bitmap.createScaledBitmap(originalImage, targetWidth, targetHeight, false);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resizedImage.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        String base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", "You");
            jsonObject.put("image", base64String);

            webSocket.send(jsonObject.toString());

            runOnUiThread(() -> {
                try {
                    jsonObject.put("isSent", true);
                    Toast.makeText(ChatActivity.this,
                            "Image Sent Successfully!",
                            Toast.LENGTH_SHORT).show();

                    String jsCode = "addImageToUI(" + jsonObject + ")";
                    webView.evaluateJavascript(jsCode, null);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
