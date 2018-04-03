package com.kozaris.android.k_mqtt;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttTraceHandler;

import java.util.List;

public class MqttClient {
    /** Singleton instance of <code>MqttClient</code>**/
    private static MqttClient instance = null;

    /** Active {@link Connection} object **/
    private Connection activeConnection = null;

    /** {@link Persistence} object used to save, delete and restore connection**/
    private Persistence persistence = null;

    /**
     * Create an MqttClient object
     * @param context Applications context
     */
    private MqttClient(Context context){
        // If there is state, attempt to restore it
        persistence = new Persistence(context);
        try {
            List<Connection> connectionList = persistence.restoreConnections(context);
            if (connectionList.size()==0) {return;}
            activeConnection= connectionList.get(connectionList.size()-1);
        } catch (Persistence.PersistenceException e){
            e.printStackTrace();
        }
    }

    /**
     * Returns an already initialised instance of <code>MqttClient</code>, if MqttClient has yet to be created, it will
     * create and return that instance.
     * @param context The applications context used to create the <code>MqttClient</code> object if it is not already initialised
     * @return <code>MqttClient</code> instance
     */
    public synchronized static MqttClient getInstance(Context context){
        if(instance ==  null){
            instance = new MqttClient(context);
        }
        return instance;
    }


    public Connection findConnectionByHost(Context context,String hostname){
        try {
            List<Connection> connectionList = persistence.restoreConnections(context);
            for(Connection connection : connectionList) {
                if (connection.getHostName().equalsIgnoreCase(hostname)){
                    return connection;
                }
            }
            return null;
        } catch (Persistence.PersistenceException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds a {@link Connection} object to the  MqttClient
     * @param connection {@link Connection} to add
     */
    public void setConnection(Context context,Connection connection){
        Connection dbConnection= findConnectionByHost(context,connection.getHostName());
        if (dbConnection!=null)
        {
            connection.assignPersistenceId(dbConnection.persistenceId());
            updateConnection(connection);
        }else
        {
            addConnectionToDB(connection);
        }
        activeConnection=connection;

    }

    private void addConnectionToDB(Connection connection){
        try {
            persistence.persistConnection(connection);
        } catch (Persistence.PersistenceException e) {
            //error persisting well lets just swallow this
            e.printStackTrace();
        }

    }

    /**
     * Get  the connection associated with this <code>MqttClient</code> object.
     * @return <code>Connection</code> of MqttClient
     */
    public Connection getConnection(){
        return activeConnection;
    }


    /**
     * Get  the connection associated with this <code>MqttClient</code> object.
     * @return <code>Connection</code> of MqttClient
     */

    public List<Connection> getConnectionList(Context context){
        try {
            return persistence.restoreConnections(context);
        } catch (Persistence.PersistenceException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Removes connection from the MqttClient
     */
    public void removeConnection(){
        persistence.deleteConnection(activeConnection);
        activeConnection=null;
    }

    /**
     * Updates an existing connection within the MqttClient
     * as well as in the persisted model
     * @param connection connection to be updated.
     */
    private void updateConnection(Connection connection){
        activeConnection=connection;
        persistence.updateConnection(connection);
    }

    public static class MqttTraceCallback implements MqttTraceHandler {

        public void traceDebug(java.lang.String arg0, java.lang.String arg1) {
            Log.i(arg0, arg1);
        }

        public void traceError(java.lang.String arg0, java.lang.String arg1) {
            Log.e(arg0, arg1);
        }

        public void traceException(java.lang.String arg0, java.lang.String arg1,
                                   java.lang.Exception arg2) {
            Log.e(arg0, arg1, arg2);
        }

    }

}


