package net.ssignal.util;

import androidx.annotation.Nullable;

public class SSSocketException extends Exception {

    private String 提示文本;

    public SSSocketException(String message) {
        提示文本 = message;
    }

    @Nullable
    @Override
    public String getMessage() {
        return 提示文本;
    }

}
