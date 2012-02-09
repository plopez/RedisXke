package fr.xebia.xke.redis.valve.todo;

import fr.xebia.xke.redis.valve.done.RedisSession;
import org.apache.catalina.*;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.util.LifecycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

import java.io.IOException;

public class RedisSessionManager extends ManagerBase implements Lifecycle {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisSessionManager.class);

    // Redis connexion
    protected String host = "localhost";
    protected int port = 6379;
    protected int database = 0;
    protected String password = null;
    protected int timeout = Protocol.DEFAULT_TIMEOUT;
    protected JedisPool connectionPool;

    protected RedisSerializer serializer;


    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    @Override
    public void addLifecycleListener(LifecycleListener lifecycleListener) {
        lifecycle.addLifecycleListener(lifecycleListener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    @Override
    public void removeLifecycleListener(LifecycleListener lifecycleListener) {
        lifecycle.removeLifecycleListener(lifecycleListener);
    }

    @Override
    public void start() throws LifecycleException {
        for (Valve valve : getContainer().getPipeline().getValves()) {
            if (valve instanceof RedisSessionValve) {
                RedisSessionValve handlerValve = (RedisSessionValve) valve;
                handlerValve.setRedisSessionManager(this);
                LOGGER.info("Attached to RedisSessionValve");
                break;
            }
        }

        try {
            initializeSerializer();
        } catch (Exception e) {
            LOGGER.info("Unable to load serializer", e);
            throw new LifecycleException(e);
        }

        LOGGER.info("Will expire sessions after " + getMaxInactiveInterval() + " seconds");

        initializeRedisPool();

        setDistributable(true);

        lifecycle.fireLifecycleEvent(START_EVENT, null);
    }

    @Override
    public void stop() throws LifecycleException {
        try {
            connectionPool.destroy();
        } catch (Exception e) {
            // Do nothing.
        }

        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
    }

    private void initializeSerializer() {
        serializer = new RedisSerializer();

        Loader loader = null;

        if (container != null) {
            loader = container.getLoader();
        }

        ClassLoader classLoader = null;

        if (loader != null) {
            classLoader = loader.getClassLoader();
        }
        serializer.setClassLoader(classLoader);
    }

    @Override
    public int getRejectedSessions() {
        // Nothing to do
        return 0;
    }

    @Override
    public void setRejectedSessions(int i) {
        // Do nothing
    }

    @Override
    public void load() throws ClassNotFoundException, IOException {
        // Nothing to do
    }

    @Override
    public void unload() throws IOException {
       // Nothing to do
    }

    // Gestion du pool Jedis
    protected Jedis acquireConnection() {
        // TODO
        return null;
    }

    protected void returnConnection(Jedis jedis, Boolean error) {
        // TODO
    }

    protected void initializeRedisPool() {
        // TODO
    }

    // Interaction avec le pool
    public void save(Session session) throws IOException {
        // TODO
    }
    protected RedisSession loadSessionFromRedis(String id) throws IOException {
        // TODO
        return null;
    }
    public void remove(Session session) {
        // TODO
    }


    // Gestion de la session par le manager
    @Override
    public Session createSession(String id) {
        // TODO

        return null;
    }

    @Override
    public Session createEmptySession() {
        return new RedisSession(this)  ;
    }

    @Override
    public void add(Session session) {
        try {
            save(session);
        } catch (IOException ex) {
            LOGGER.warn("Unable to add to session manager store: " + ex.getMessage());
            throw new RuntimeException("Unable to add to session manager store.", ex);
        }
    }

    @Override
    public Session findSession(String id) throws IOException {
        //TODO Uncomment
//        RedisSession session;
//
//        if (id == null) {
//            session = null;
//            currentSessionIsPersisted.set(false);
//        } else if (id.equals(currentSessionId.get())) {
//            session = currentSession.get();
//        } else {
//            session = loadSessionFromRedis(id);
//
//            if (session != null) {
//                currentSessionIsPersisted.set(true);
//            }
//        }
//
//        currentSession.set(session);
//        currentSessionId.set(id);
//
//        return session;
        //TODO remove
        return null;
    }

    public void afterRequest() {
        //TODO Uncomment
//        RedisSession redisSession = currentSession.get();
//        if (redisSession != null) {
//            currentSession.remove();
//            currentSessionId.remove();
//            currentSessionIsPersisted.remove();
//            LOGGER.debug("Session removed from ThreadLocal :" + redisSession.getIdInternal());
//        }
    }



    
    
}
