package com.example.project_dam;

import android.webkit.JavascriptInterface;

public interface WebAppInterface {

    @JavascriptInterface
    void loadMainPage();

    @JavascriptInterface
    void loadChatPage();

    @JavascriptInterface
    void openChannel(String videoId);

    @JavascriptInterface
    void onVideoClicked(String videoId);

    @JavascriptInterface
    void sendMessageToServer(String action, String data);

    @JavascriptInterface
    void enterRoom(String name);

    @JavascriptInterface
    void sendMessage(String message);

    @JavascriptInterface
    void loadImage();
}
