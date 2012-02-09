package fr.xebia.xke.redis.valve.done;

import org.apache.catalina.*;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.util.LifecycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class RedisSessionManager extends ManagerBase implements Lifecycle {
    protected byte[] NULL_SESSION = "null".getBytes();

    private static Logger LOGGER = LoggerFactory.getLogger(RedisSessionManager.class);
    protected String host = "ec2-46-137-132-97.eu-west-1.compute.amazonaws.com";
    protected int port = 6969;
    protected int database = 0;
    protected String password = null;
    protected int timeout = Protocol.DEFAULT_TIMEOUT;
    protected JedisPool connectionPool;

    //protected RedisSessionValve handlerValve;
    protected ThreadLocal<RedisSession> currentSession = new ThreadLocal<RedisSession>();
    protected ThreadLocal<String> currentSessionId = new ThreadLocal<String>();
    protected ThreadLocal<Boolean> currentSessionIsPersisted = new ThreadLocal<Boolean>();
    protected RedisSerializer serializer;

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRejectedSessions() {
        // Essentially do nothing.
        return 0;
    }

    public void setRejectedSessions(int i) {
        // Do nothing.
    }

    protected Jedis acquireConnection() {
        Jedis jedis = connectionPool.getResource();

        if (getDatabase() != 0) {
            jedis.select(getDatabase());
        }

        return jedis;
    }

    protected void returnConnection(Jedis jedis, Boolean error) {
        if (error) {
            connectionPool.returnBrokenResource(jedis);
        } else {
            connectionPool.returnResource(jedis);
        }
    }

    @Override
    public void load() throws ClassNotFoundException, IOException {

    }

    @Override
    public void unload() throws IOException {

    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }


    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    @Override
    public void start() throws LifecycleException {
        for (Valve valve : getContainer().getPipeline().getValves()) {
            if (valve instanceof RedisSessionValve) {
                RedisSessionValve handlerValve = (RedisSessionValve) valve;
                handlerValve.setRedisSessionManager(this);

                LOGGER.info("Attached to RedisSessionHandlerValve");
                break;
            }
        }

        try {
            initializeSerializer();
        } catch (ClassNotFoundException e) {
            LOGGER.error("Unable to load serializer", e);
            throw new LifecycleException(e);
        } catch (InstantiationException e) {
            LOGGER.info("Unable to load serializer", e);
            throw new LifecycleException(e);
        } catch (IllegalAccessException e) {
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

    @Override
    public Session createSession() {
        RedisSession session = (RedisSession) createEmptySession();

        // Initialize the properties of the new session and return it
        session.setNew(true);
        session.setValid(true);
        session.setCreationTime(System.currentTimeMillis());
        session.setMaxInactiveInterval(getMaxInactiveInterval());

        String sessionId;
        String jvmRoute = getJvmRoute();

        Boolean error = true;
        Jedis jedis = null;

        try {
            jedis = acquireConnection();

            // Ensure generation of a unique session identifier.
            do {
                sessionId = generateSessionId();

                if (jvmRoute != null) {
                    sessionId += '.' + jvmRoute;
                }
            } while (jedis.setnx(sessionId.getBytes(), NULL_SESSION) != 1L); // 1 = key set; 0 = key already existed

            /* Even though the key is set in Redis, we are not going to flag
 the current thread as having had the session persisted since
 the session isn't actually serialized to Redis yet.
 This ensures that the save(session) at the end of the request
 will serialize the session into Redis with 'set' instead of 'setnx'. */

            error = false;

            session.setId(sessionId);
            session.tellNew();
        } finally {
            if (jedis != null) {
                returnConnection(jedis, error);
            }
        }

        return session;
    }

    @Override
    public Session createEmptySession() {
        return new RedisSession(this);
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
        RedisSession session;

        if (id == null) {
            session = null;
            currentSessionIsPersisted.set(false);
        } else if (id.equals(currentSessionId.get())) {
            session = currentSession.get();
        } else {
            session = loadSessionFromRedis(id);

            if (session != null) {
                currentSessionIsPersisted.set(true);
            }
        }

        currentSession.set(session);
        currentSessionId.set(id);

        return session;
    }

    // Utilities
    public void clear() {
        Jedis jedis = null;
        Boolean error = true;
        try {
            jedis = acquireConnection();
            jedis.flushDB();
            error = false;
        } finally {
            if (jedis != null) {
                returnConnection(jedis, error);
            }
        }
    }

    public int getSize() throws IOException {
        Jedis jedis = null;
        Boolean error = true;
        try {
            jedis = acquireConnection();
            int size = jedis.dbSize().intValue();
            error = false;
            return size;
        } finally {
            if (jedis != null) {
                returnConnection(jedis, error);
            }
        }
    }

    public String[] keys() throws IOException {
        Jedis jedis = null;
        Boolean error = true;
        try {
            jedis = acquireConnection();
            Set<String> keySet = jedis.keys("*");
            error = false;
            return keySet.toArray(new String[keySet.size()]);
        } finally {
            if (jedis != null) {
                returnConnection(jedis, error);
            }
        }
    }

    public RedisSession loadSessionFromRedis(String id) throws IOException {
        RedisSession session;

        Jedis jedis = null;
        Boolean error = true;

        try {
            LOGGER.debug("Attempting to load session " + id + " from Redis");

            jedis = acquireConnection();
            byte[] data = jedis.get(id.getBytes());
            error = false;

            if (data == null) {
                LOGGER.debug("Session " + id + " not found in Redis");
                session = null;
            } else if (Arrays.equals(NULL_SESSION, data)) {
                throw new IllegalStateException("Race condition encountered: attempted to load session[" + id + "] which has been created but not yet serialized.");
            } else {
                LOGGER.debug("Deserializing session " + id + " from Redis");
                session = (RedisSession) createEmptySession();
                serializer.deserializeInto(data, session);
                session.setId(id);
                session.setNew(false);
                session.setMaxInactiveInterval(getMaxInactiveInterval() * 1000);
                session.access();
                session.setValid(true);
                session.resetDirtyTracking();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Session Contents [" + id + "]:");
                    for (Object name : Collections.list(session.getAttributeNames())) {
                        LOGGER.debug("  " + name);
                    }
                }
            }

            return session;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw e;
        } catch (ClassNotFoundException ex) {
            LOGGER.error("Unable to deserialize into session", ex);
            throw new IOException("Unable to deserialize into session", ex);
        } finally {
            if (jedis != null) {
                returnConnection(jedis, error);
            }
        }
    }

    public void save(Session session) throws IOException {
        Jedis jedis = null;
        Boolean error = true;

        try {
            LOGGER.debug("Saving session " + session + " into Redis");

            RedisSession redisSession = (RedisSession) session;

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Session Contents [" + redisSession.getId() + "]:");
                for (Object name : Collections.list(redisSession.getAttributeNames())) {
                    LOGGER.debug("  " + name);
                }
            }

            Boolean sessionIsDirty = redisSession.isDirty();

            redisSession.resetDirtyTracking();
            byte[] binaryId = redisSession.getId().getBytes();

            jedis = acquireConnection();

            if (sessionIsDirty || currentSessionIsPersisted.get() != true) {
                jedis.set(binaryId, serializer.serializeFrom(redisSession));
            }

            currentSessionIsPersisted.set(true);

            LOGGER.debug("Setting expire timeout on session [" + redisSession.getId() + "] to " + getMaxInactiveInterval());
            jedis.expire(binaryId, getMaxInactiveInterval());

            error = false;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());

            throw e;
        } finally {
            if (jedis != null) {
                returnConnection(jedis, error);
            }
        }
    }

    @Override
    public void remove(Session session) {
        Jedis jedis = null;
        Boolean error = true;

        LOGGER.debug("Removing session ID : " + session.getId());

        try {
            jedis = acquireConnection();
            jedis.del(session.getId());
            error = false;
        } finally {
            if (jedis != null) {
                returnConnection(jedis, error);
            }
        }
    }

    public void afterRequest() {
        RedisSession redisSession = currentSession.get();
        if (redisSession != null) {
            currentSession.remove();
            currentSessionId.remove();
            currentSessionIsPersisted.remove();
            LOGGER.debug("Session removed from ThreadLocal :" + redisSession.getIdInternal());
        }
    }

    @Override
    public void processExpires() {
        // We are going to use Redis's ability to expire keys for session expiration.

        // Do nothing.
    }

    protected void initializeRedisPool() throws LifecycleException {
        try {
            // TODO: Allow configuration of pool (such as size...)
            connectionPool = new JedisPool(new JedisPoolConfig(), getHost(), getPort(), getTimeout(), getPassword());
        } catch (Exception e) {
            e.printStackTrace();
            throw new LifecycleException("Error Connecting to Redis", e);
        }
    }

    protected void initializeSerializer() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
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
}
