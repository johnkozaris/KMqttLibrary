package com.kozaris.android.k_mqtt;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Represents a { @link MqttClient} and the actions it has performed
 *
 */
public class Connection {

    //<editor-fold desc="Library Constants">
    public static final String CONNECTION_KEY = "CONNECTION_KEY";

    public static final String AUTO_CONNECT = "AUTO_CONNECT";
    public static final String CONNECTED = "CONNECTED";

    public static final String LOGGING_KEY = "LOGGING_ENABLED";


    /** Property name for the history field in {@link Connection} object for use with {@link java.beans.PropertyChangeEvent} **/
    static final String historyProperty = "history";

    /** Property name for the connection status field in {@link Connection} object for use with {@link java.beans.PropertyChangeEvent} **/
    static final String ConnectionStatusProperty = "connectionStatus";



    /** Empty String for comparisons **/
    public static final String empty = "";
    //</editor-fold>

    /** The clientId of the client associated with this <code>Connection</code> object **/
    private String clientId = null;

    /** The host that the { @link MqttClient} represented by this <code>Connection</code> is represented by **/
    private String host = null;

    /** The port on the server that this client is connecting to **/
    private int port = 0;

    /** {@link ConnectionStatus } of the { @link MqttClient} represented by this <code>Connection</code> object. Default value is {@link ConnectionStatus#NONE} **/
    private ConnectionStatus status = ConnectionStatus.NONE;

    /** Te history of the { @link MqttClient} represented by this <code>Connection</code> object **/
    private ArrayList<String> history = null;

    /** The { @link MqttClient} instance this class represents **/
    private MqttAndroidClient client = null;

    /** Collection of {@link java.beans.PropertyChangeListener} **/
    private final ArrayList<PropertyChangeListener> listeners = new ArrayList<>();

    /** The {@link Context} of the application this object is part of**/
    private Context context = null;

    /** The {@link MqttConnectOptions} that were used to connect this client **/
    private MqttConnectOptions mqttConnectOptions;

    /** True if this connection is secured using TLS **/
    private boolean tlsConnection = true;

    /** Persistence id, used by {@link Persistence} **/
    private long persistenceId = -1;


    /** The list of this connection's subscriptions **/
    private final Map<String, Subscription> subscriptions = new HashMap<>();

    private final ArrayList<ReceivedMessage> messageHistory =  new ArrayList<>();

    private final ArrayList<IReceivedMessageListener> receivedMessageListeners = new ArrayList<>();

    /**
     * Connections status for  a connection
     */
    public enum ConnectionStatus {

        /** Client is Connecting **/
        CONNECTING,
        /** Client is Connected **/
        CONNECTED,
        /** Client is Disconnecting **/
        DISCONNECTING,
        /** Client is Disconnected **/
        DISCONNECTED,
        /** Client has encountered an Error **/
        ERROR,
        /** Status is unknown **/
        NONE
    }


    /**
     * Creates a connection from persisted information in the database store, attempting
     * to create a {@link MqttClient} and the client Id.
     * @param clientId The id of the client
     * @param host the server which the client is connecting to
     * @param port the port on the server which the client will attempt to connect to
     * @param context the application context
     * @param tlsConnection true if the connection is secured by SSL
     * @return a new instance of <code>Connection</code>
     */
    public static Connection createConnection( String clientId, String host, int port, Context context, boolean tlsConnection){

        String uri;
        if(tlsConnection) {
            uri = "ssl://" + host + ":" + port;
        } else {
            uri = "tcp://" + host + ":" + port;
        }

        MqttAndroidClient client = new MqttAndroidClient(context, uri, clientId);
        return new Connection(clientId, host, port, context, client, tlsConnection);
    }

    public void updateConnection(String clientId, String host, int port, boolean tlsConnection){
        String uri;
        if(tlsConnection) {
            uri = "ssl://" + host + ":" + port;
        } else {
            uri = "tcp://" + host + ":" + port;
        }

        this.clientId = clientId;
        this.host = host;
        this.port = port;
        this.tlsConnection = tlsConnection;
        this.client = new MqttAndroidClient(context, uri, clientId);

    }


    /**
     * Creates a connection object with the server information and the client
     * hand which is the reference used to pass the client around activities
     * @param clientId The Id of the client
     * @param host The server which the client is connecting to
     * @param port The port on the server which the client will attempt to connect to
     * @param context The application context
     * @param client The MqttAndroidClient which communicates with the service for this connection
     * @param tlsConnection true if the connection is secured by SSL
     */
    private Connection(String clientId, String host,
                       int port, Context context, MqttAndroidClient client, boolean tlsConnection) {
        this.clientId = clientId;
        this.host = host;
        this.port = port;
        this.context = context;
        this.client = client;
        this.tlsConnection = tlsConnection;
        history = new ArrayList<>();
        String sb = "Client: " +
                clientId +
                " created";
        addAction(sb);
    }

    /**
     * Add an action to the history of the client
     * @param action the history item to add
     */
    void addAction(String action) {

        Object[] args = new String[1];
        DateFormat dateTimeFormatter = SimpleDateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        args[0] = dateTimeFormatter.format(new Date());

        String timestamp = context.getString(R.string.timestamp, args);
        history.add(action + timestamp);

        notifyListeners(new PropertyChangeEvent(this, historyProperty, null, null));
    }
    /**
     * Determines if the client is connected
     * @return is the client connected
     */
    public boolean isConnected() {
        return status == ConnectionStatus.CONNECTED;
    }

    /**
     * Changes the connection status of the client
     * @param connectionStatus The connection status of this connection
     */
    public void changeConnectionStatus(ConnectionStatus connectionStatus) {
        status = connectionStatus;
        notifyListeners((new PropertyChangeEvent(this, ConnectionStatusProperty, null, null)));
    }

    /**
     * A string representing the state of the client this connection
     * object represents
     *
     *
     * @return A string representing the state of the client
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(clientId);
        sb.append("\n ");

        switch (status) {

            case CONNECTED :
                sb.append(context.getString(R.string.connection_connected_to));
                break;
            case DISCONNECTED :
                sb.append(context.getString(R.string.connection_disconnected_from));
                break;
            case NONE :
                sb.append(context.getString(R.string.connection_unknown_status));
                break;
            case CONNECTING :
                sb.append(context.getString(R.string.connection_connecting_to));
                break;
            case DISCONNECTING :
                sb.append(context.getString(R.string.connection_disconnecting_from));
                break;
            case ERROR :
                sb.append(context.getString(R.string.connection_error_connecting_to));
        }
        sb.append(" ");
        sb.append(host);

        return sb.toString();
    }

    /**
     * Compares two connection objects for equality
     * this only takes account of the clientId
     * @param o The object to compare to
     * @return true if the client Id match
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Connection)) {
            return false;
        }

        Connection c = (Connection) o;

        return clientId.equals(c.clientId);

    }

    /**
     * Get the client Id for the client this object represents
     * @return the client id for the client this object represents
     */
    public String getId() {
        return clientId;
    }
    
    public ConnectionStatus getStatus(){return status;}
    
    /**
     * Get the host name of the server that this connection object is associated with
     * @return the host name of the server this connection object is associated with
     */
    public String getHostName() {

        return host;
    }

    /**
     * Gets the client which communicates with the org.eclipse.paho.android.service service.
     * @return the client which communicates with the org.eclipse.paho.android.service service
     */
    public MqttAndroidClient getClient() {
        return client;
    }

    /**
     * Add the connectOptions used to connect the client to the server
     * @param connectOptions the connectOptions used to connect to the server
     */
    public void addConnectionOptions(MqttConnectOptions connectOptions) {
        mqttConnectOptions = connectOptions;
    }

    /**
     * Get the connectOptions used to connect this client to the server
     * @return The connectOptions used to connect the client to the server
     */
    public MqttConnectOptions getConnectionOptions()
    {
        return mqttConnectOptions;
    }

    /**
     * Register a {@link PropertyChangeListener} to this object
     * @param listener the listener to register
     */
    public void registerChangeListener(PropertyChangeListener listener)
    {
        if (listeners.indexOf(listener)==-1)
        {
            listeners.add(listener);
        }
    }

    /**
     * Notify {@link PropertyChangeListener} objects that the object has been updated
     * @param propertyChangeEvent - The property Change event
     */
    private void notifyListeners(PropertyChangeEvent propertyChangeEvent)
    {
        for (PropertyChangeListener listener : listeners)
        {
            listener.propertyChange(propertyChangeEvent);
        }
    }

    /**
     * Gets the port that this connection connects to.
     * @return port that this connection connects to
     */
    public int getPort() {
        return port;
    }

    /**
     * Determines if the connection is secured using SSL, returning a C style integer value
     * @return 1 if SSL secured 0 if plain text
     */
    public int isSSL() {
        return tlsConnection ? 1 : 0;
    }

    /**
     * Assign a persistence ID to this object
     * @param id the persistence id to assign
     */
    public void assignPersistenceId(long id) {
        persistenceId = id;
    }

    /**
     * Returns the persistence ID assigned to this object
     * @return the persistence ID assigned to this object
     */
    public long persistenceId() {
        return persistenceId;
    }



    public void addNewSubscription(Subscription subscription) throws MqttException {
        if(!subscriptions.containsKey(subscription.getTopic())){
            try{
                String[] actionArgs = new String[1];
                actionArgs[0] = subscription.getTopic();
                final ActionListener callback = new ActionListener(this.context,
                        ActionListener.Action.SUBSCRIBE, this, actionArgs);
                this.getClient().subscribe(subscription.getTopic(), subscription.getQos(), null, callback);
                Persistence persistence = new Persistence(context);
                long rowId = persistence.persistSubscription(subscription);
                subscription.setPersistenceId(rowId);
                subscriptions.put(subscription.getTopic(), subscription);
            } catch (Persistence.PersistenceException pe){
                throw new MqttException(pe);
            }

        }
    }


    public void unsubscribe (Subscription subscription) throws MqttException {
        if(subscriptions.containsKey(subscription.getTopic())){
            this.getClient().unsubscribe(subscription.getTopic());
            subscriptions.remove(subscription.getTopic());
            Persistence persistence = new Persistence(context);
            persistence.deleteSubscription(subscription);
        }

    }

    public void setSubscriptions(ArrayList<Subscription> newSubs){
        for(Subscription sub : newSubs){
            subscriptions.put(sub.getTopic(), sub);
        }
    }

    public ArrayList<Subscription> getSubscriptions(){
        ArrayList<Subscription> subs = new ArrayList<>(subscriptions.values());
        return subs;
    }

    public void addReceivedMessageListener(IReceivedMessageListener listener){
      if(receivedMessageListeners.indexOf(listener)==-1)
      {
          receivedMessageListeners.add(listener);
      }
    }

    public void messageArrived(String topic, MqttMessage message){
        ReceivedMessage msg = new ReceivedMessage(topic, message);
        if (messageHistory.size()>20){
            messageHistory.clear();
        }
        messageHistory.add(msg);
        if(subscriptions.containsKey(topic)){
            subscriptions.get(topic).setLastMessage(new String(message.getPayload()));
        }

        for(IReceivedMessageListener listener : receivedMessageListeners){
            listener.onMessageReceived(msg);
        }

    }
    public ArrayList<ReceivedMessage> getMessages(){
        return messageHistory;
    }


    public interface IReceivedMessageListener {

        void onMessageReceived(ReceivedMessage message);
    }
}
