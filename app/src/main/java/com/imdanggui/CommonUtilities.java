package com.imdanggui;

import android.content.Context;
import android.content.Intent;

public final class CommonUtilities {
	
	// give your server registration url here
    static final String SERVER_URL = StartActivity.domain + "gcm_server_php/register.php";

    // Google project id
    static final String SENDER_ID = "1075666493400";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "IMDANGGUI";
    static final String DISPLAY_MESSAGE_ACTION =
            "com.imdanggui.DISPLAY_MESSAGE";

    static final String EXTRA_MESSAGE = "message";

    static void displayMessage(Context context, String message) {
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}
