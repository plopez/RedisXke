package fr.xebia.xke.redis.valve.todo;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RedisSession extends StandardSession {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisSession.class);

    public RedisSession(Manager manager) {
        super(manager);
    }

    @Override
    public void setId(String id) {
        // Specifically do not call super()
        this.id = id;
    }
}
