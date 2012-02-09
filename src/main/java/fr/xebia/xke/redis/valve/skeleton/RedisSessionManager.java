package fr.xebia.xke.redis.valve.skeleton;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Session;
import org.apache.catalina.session.ManagerBase;
import org.apache.catalina.util.LifecycleSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.io.IOException;

public class RedisSessionManager extends ManagerBase implements Lifecycle {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisSessionManager.class);

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
        // Attach RedisManager to RedisValve

        try {
            initializeSerializer();
        } catch (Exception e) {
            LOGGER.info("Unable to load serializer", e);
            throw new LifecycleException(e);
        }

        initializeRedisPool();

        setDistributable(true);

        lifecycle.fireLifecycleEvent(START_EVENT, null);
    }

    private void initializeRedisPool() {
        // TODO
    }

    private void initializeSerializer() {
        // TODO
    }

    @Override
    public void stop() throws LifecycleException {
        // TODO
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

    public void afterRequest() {
        // TODO
    }

    public void save(Session session) {
        // TODO
    }
}
