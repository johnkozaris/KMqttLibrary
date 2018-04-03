package com.kozaris.android.k_mqtt;


import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Date;

public class Subscription {
    private String topic;
    private int qos;
    private String lastMessage;
    private String clientId;
    private long persistenceId;
    private boolean enableNotifications;

    public Subscription(String topic, int qos, String clientId, boolean enableNotifications){
        this.topic = topic;
        this.qos = qos;
        this.clientId = clientId;
        this.enableNotifications = enableNotifications;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getclientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public long getPersistenceId() {
        return persistenceId;
    }

    public void setPersistenceId(long persistenceId) {
        this.persistenceId = persistenceId;
    }

    public boolean isEnableNotifications() {
        return enableNotifications;
    }


    @Override
    public String toString() {
        return "Subscription{" +
                "topic='" + topic + '\'' +
                ", qos=" + qos +
                ", clientId='" + clientId + '\'' +
                ", persistenceId='" + persistenceId + '\'' +
                ", enableNotifications='" + enableNotifications + '\''+
                '}';
    }




}
