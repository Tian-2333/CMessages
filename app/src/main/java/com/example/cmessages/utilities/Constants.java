package com.example.cmessages.utilities;

import java.security.PublicKey;
import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_NICKNAME = "nickname";
    public static final String KEY_IS_SIGNED_IN = "isSignIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN ="fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVED_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String, String> remoteMsgHeaders  = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){
        if (remoteMsgHeaders == null){
             remoteMsgHeaders = new HashMap<>();
             remoteMsgHeaders.put(
                     REMOTE_MSG_AUTHORIZATION,
                     "key = AAAAfAJV8pY:APA91bEsWqnUjKfPuvk2iPrtKO7je8SC23WGOyUcetvL5cpTtEKwkiOdaG6-CIjSbZZhFkDNBFfFNm5txyI_ZfdsXw5R5KcBdrQW6JyZPivc1KRxRFzJVkVZyaMIDXSdRYH8wWWxxQA6"
             );
             remoteMsgHeaders.put(
                     REMOTE_MSG_CONTENT_TYPE,
                     "application/json"
             );
        }
        return remoteMsgHeaders;
    }
}
