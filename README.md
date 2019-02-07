# K_Mqtt_Library
An MQTT Android Client that treats mqtt connections as singletons so they can be passed bettween activities. You can connect,subscribe,publish and receive messages anywhere on the android project. Independently of the activity lifecycle.


>This library uses the Eclipse Paho Android Service.As well as code from the Paho example project  
>[https://www.eclipse.org/paho/clients/android/](https://www.eclipse.org/paho/clients/android/)  
>[https://github.com/eclipse/paho.mqtt.android](https://github.com/eclipse/paho.mqtt.android)


## Installation
Gradle Dependency.
```
compile 'com.kozaris.android:k-mqtt:1.0.1'
```


## How to use  
For a complete working sample check ApplicationExample
### 1. Create The Connection Object
```java
String ClientId="TestMQTTClient";
int Qos=0;
String ServerHostName="host.hostname.org";
int ServerPort=1883;
boolean TlsConnection=false;
//Initialize Connection object
Connection  mqttConnection = Connection.createConnection(ClientId,ServerHostName, ServerPort, this,TlsConnection);
MqttConnectOptions conOptions = new MqttConnectOptions();
conOptions.setConnectionTimeout(80);
conOptions.setKeepAliveInterval(200);
conOptions.setCleanSession(true);
 mqttConnection.addConnectionOptions(conOptions);
//Property changed Listener
mqttConnection.registerChangeListener(changeListener);
mqttConnection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);
mqttConnection.getClient().setTraceCallback(new MqttClient.MqttTraceCallback());
//Register the Activity as a Message Receiver if required (only if it receives mqtt messages)
mqttConnection.addReceivedMessageListener(this);
```
### 2. Initialize the Static Class
```java
//Initializes and Persists to a database
MqttClient.getInstance(this).setConnection(this,mqttConnection);
```
### 3. Connect anywhere
```java
//Retrieve the Connection Object
Connection con = MqttClient.getInstance(this).getConnection();
//connect
    if (con != null) {
            final ActionListener callback = new ActionListener(this, ActionListener.Action.CONNECT, con);
            con.getClient().setCallback(new MqttCallbackHandler(this));
            try {
                con.getClient().connect(con.getConnectionOptions(), null, callback);
            } catch (MqttException e) {
                Log.e(this.getClass().getCanonicalName(),
                        "MqttException occurred", e);
                mqttConnection.changeConnectionStatus(Connection.ConnectionStatus.ERROR);
            }
        }
```
### 4. Publish anywhere
```java
//Retrieve the Connection Object
Connection con = MqttClient.getInstance(this).getConnection();
        if(con.isConnected()) {
            try {
                con.getClient().publish("/TestTopic", new MqttMessage("hello".getBytes()));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
```
### 5. Subscribe anywhere
```java
Connection con = MqttClient.getInstance(this).getConnection();
        if (con.isConnected()) {
            Subscription subscription = new Subscription("/TestTopic", 0, con.getId(), true);
            try {
                con.addNewSubscription(subscription);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
```
## AndroidManifest
```xml
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
