package io.skygear.skygear;

import android.content.Context;

import java.security.InvalidParameterException;

/**
 * Container for Skygear.
 */
public final class Container {
    private static final String TAG = "Skygear SDK";
    private static Container sharedInstance;

    final PersistentStore persistentStore;
    final Context context;
    final RequestManager requestManager;
    Configuration config;

    final AuthContainer auth;
    final PubsubContainer pubsub;
    final PushContainer push;
    final PublicDatabase publicDatabase;
    final Database privateDatabase;

    /**
     * Instantiates a new Container.
     *
     * @param context application context
     * @param config  configuration of the container
     */
    public Container(Context context, Configuration config) {
        this.context = context.getApplicationContext();
        this.config = config;
        this.requestManager = new RequestManager(context, config);
        this.pubsub = new PubsubContainer(this);
        this.push = new PushContainer(this);
        this.persistentStore = new PersistentStore(context);
        this.publicDatabase = Database.Factory.publicDatabase(this);
        this.privateDatabase = Database.Factory.privateDatabase(this);

        this.auth = new AuthContainer(this);

        if (this.persistentStore.currentUser != null) {
            this.requestManager.accessToken = this.persistentStore.currentUser.accessToken;
        }

        if (this.persistentStore.defaultAccessControl != null) {
            AccessControl.defaultAccessControl = this.persistentStore.defaultAccessControl;
        }
    }

    /**
     * Gets the Default container shared within the application.
     * This container is configured with default configuration. Better to configure it according to your usage.
     *
     * @param context application context
     * @return a default container
     */
    public static Container defaultContainer(Context context) {
        if (sharedInstance == null) {
            sharedInstance = new Container(context, Configuration.defaultConfiguration());
        }

        return sharedInstance;
    }

    /**
     * @return auth
     */
    public AuthContainer auth() {
        return auth;
    }

    /**
     * Gets the public database.
     *
     * @return the public database
     */
    public PublicDatabase publicDatabase() {
        return publicDatabase;
    }

    /**
     * Gets the private database.
     *
     * @return the private database
     * @throws AuthenticationException the authentication exception
     */
    public Database privateDatabase() throws AuthenticationException {
        if (this.auth.getCurrentUser() == null) {
            throw new AuthenticationException("Private database is only available for logged-in user");
        }

        return privateDatabase;
    }

    /**
     * Gets pubsubContainer.
     *
     * @return the pusbubContainer
     */
    public PubsubContainer pubsub() {
        return pubsub;
    }

    /**
     * Gets pushContainer.
     *
     * @return the pushContainer
     */
    public PushContainer push() {
        return push;
    }

    /**
     * Updates configuration of the container
     *
     * @param config configuration of the container
     */
    public void configure(Configuration config) {
        if (config == null) {
            throw new InvalidParameterException("Null configuration is not allowed");
        }

        this.config = config;
        this.requestManager.configure(config);
        this.pubsub.configure(config);
    }

    /**
     * Gets context.
     *
     * @return the application context
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Gets config.
     *
     * @return the config
     */
    public Configuration getConfig() {
        return config;
    }

    /**
     * Sets request timeout (in milliseconds).
     *
     * @param timeout the timeout
     */
    public void setRequestTimeout(int timeout) {
        this.requestManager.requestTimeout = timeout;
    }

    /**
     * Gets request timeout (in milliseconds).
     *
     * @return the request timeout
     */
    public int getRequestTimeout() {
        return this.requestManager.requestTimeout;
    }

    /**
     * Send a request.
     *
     * @param request the request
     */
    public void sendRequest(Request request) {
        this.requestManager.sendRequest(request);
    }

    /**
     * Call lambda function.
     *
     * @param name    the function name
     * @param handler the response handler
     */
    public void callLambdaFunction(String name, LambdaResponseHandler handler) {
        this.callLambdaFunction(name, null, handler);
    }

    /**
     * Call lambda function.
     *
     * @param name    the function name
     * @param args    the arguments
     * @param handler the response handler
     */
    public void callLambdaFunction(String name, Object[] args, LambdaResponseHandler handler) {
        LambdaRequest request = new LambdaRequest(name, args);
        request.responseHandler = handler;

        this.requestManager.sendRequest(request);
    }
}
