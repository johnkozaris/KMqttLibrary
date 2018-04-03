package com.kozaris.android.k_mqtt;

import android.content.Context;
import android.util.Log;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * Handles call backs from the MQTT Client
 *
 */
public class MqttCallbackHandler implements MqttCallback {

    /** {@link Context} for the application used to format and import external strings**/
    private final Context context;
    /** Client handle to reference the connection that this handler is attached to**/


    private static final String TAG = "MqttCallbackHandler";

    /**
     * Creates an <code>MqttCallbackHandler</code> object
     * @param context The application's context
     */
    public MqttCallbackHandler(Context context)
    {
        this.context = context;
    }

    /**
     *
     * @param cause connection lost cause
     */
    @Override
    public void connectionLost(Throwable cause) {
        if (cause != null) {
            Log.d(TAG, "Connection Lost: " + cause.getMessage());
            Connection c = MqttClient.getInstance(context).getConnection();
            c.addAction("Connection Lost");
            c.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);
            Log.d(TAG,context.getString(R.string.connection_lost, c.getId(), c.getHostName()) );
        }
    }

    /**
     *
     * @param topic message topic
     * @param message message payload
     * @throws Exception throws exception
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        //Get connection object associated with this object
        Connection c = MqttClient.getInstance(context).getConnection();
        c.messageArrived(topic, message);
        //get the string from strings.xml and format
        String messageString = context.getString(R.string.messageRecieved, new String(message.getPayload()), topic+";qos:"+message.getQos()+";retained:"+message.isRetained());

        Log.i(TAG, messageString);

        //update client history
        c.addAction(messageString);

    }

    /**
     *
     * @param token see paho.eclipse
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // Do nothing
    }

}

