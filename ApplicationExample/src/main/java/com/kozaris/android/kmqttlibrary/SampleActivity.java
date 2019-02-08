package com.kozaris.android.kmqttlibrary;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.kozaris.android.k_mqtt.ActionListener;
import com.kozaris.android.k_mqtt.Connection;
import com.kozaris.android.k_mqtt.MqttCallbackHandler;
import com.kozaris.android.k_mqtt.MqttClient;
import com.kozaris.android.k_mqtt.ReceivedMessage;
import com.kozaris.android.k_mqtt.Subscription;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SampleActivity extends AppCompatActivity implements Connection.IReceivedMessageListener {

    Connection mqttConnection;
    private final ChangeListener changeListener = new ChangeListener();
    ListView listviewMessages;
    private String[] messages = new String[]{};
    ArrayAdapter<String> messagesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        Button btnConnect= findViewById(R.id.buttonConnect);
        Button btnSubscribe= findViewById(R.id.buttonSubscribe);
        Button btnPublish= findViewById(R.id.buttonPublish);
        Button btnSecondActivity= findViewById(R.id.buttonSecAct);
        listviewMessages = findViewById(R.id.listviewMessages);
        messagesAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
        listviewMessages.setAdapter(messagesAdapter);
        btnConnect.setOnClickListener(v -> Connect());
        btnPublish.setOnClickListener(v -> Publish());
        btnSubscribe.setOnClickListener(v -> Subscribe());
        btnSecondActivity.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),SampleActivity2.class)));
    }


    private void Connect() {
         if (MqttClient.getInstance(this) != null) {
                 mqttConnection = MqttClient.getInstance(this).getConnection();
            }
            else{
             MqttConnectOptions conOptions = new MqttConnectOptions();
             conOptions.setConnectionTimeout(80);
             conOptions.setKeepAliveInterval(200);
             conOptions.setCleanSession(true);
             String ClientId="TestMQTTClientkuaesf98375ef";
             int Qos=0;
             String ServerHostName="iot.eclipse.org";
             int ServerPort=1883;
             boolean TlsConnection=false;
             //Persist and connect
             mqttConnection = Connection.createConnection(ClientId,ServerHostName, ServerPort, this,TlsConnection);
             mqttConnection.registerChangeListener(changeListener);
             mqttConnection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);
             mqttConnection.getClient().setTraceCallback(new MqttClient.MqttTraceCallback());
             mqttConnection.addConnectionOptions(conOptions);
             MqttClient.getInstance(this).setConnection(this,mqttConnection);
         }
        RegisterActivityAsReceiveListener();

        //Connect
        if (mqttConnection != null) {
            final ActionListener callback = new ActionListener(this, ActionListener.Action.CONNECT, mqttConnection);
            mqttConnection.getClient().setCallback(new MqttCallbackHandler(this));
            try {
                mqttConnection.getClient().connect(mqttConnection.getConnectionOptions(), null, callback);
            } catch (MqttException e) {
                Log.e(this.getClass().getCanonicalName(),
                        "MqttException occurred", e);
                mqttConnection.changeConnectionStatus(Connection.ConnectionStatus.ERROR);
            }
        }
    }

    private void RegisterActivityAsReceiveListener() {
        mqttConnection = MqttClient.getInstance(this).getConnection();
        mqttConnection.addReceivedMessageListener(this);
    }

    private void Subscribe() {
        Connection con = MqttClient.getInstance(this).getConnection();
        if (con.isConnected()) {
            Subscription subscription = new Subscription("/TestTopic", 0, con.getId(), true);
            try {
                con.addNewSubscription(subscription);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void Publish() {

        Connection con = MqttClient.getInstance(this).getConnection();
        if(con.isConnected()) {
            try {
                con.getClient().publish("/TestTopic", new MqttMessage("hello".getBytes()));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onMessageReceived(ReceivedMessage message) {
        Log.d("MainActivity", message.getTopic() + " " + message.getMessage() + " " + message.getTimestamp());
        messagesAdapter.add("Topic: "+message.getTopic()+" Payload: "+ message.getMessage());
       listviewMessages.setAdapter(messagesAdapter);
    }

    private class ChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            //Update the ui if the connection options change (address etc)
        }
    }


}
