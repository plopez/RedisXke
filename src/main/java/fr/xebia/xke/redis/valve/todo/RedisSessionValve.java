package fr.xebia.xke.redis.valve.todo;


import org.apache.catalina.Session;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;

public class RedisSessionValve extends ValveBase {

    private static Logger LOGGER = LoggerFactory.getLogger(RedisSessionValve.class);
    private RedisSessionManager manager;

    public void setRedisSessionManager(RedisSessionManager manager) {
        this.manager = manager;
    }

    // This method is the only one needed to override to extends ValveBase
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            getNext().invoke(request, response);
        } finally {
            final Session session = request.getSessionInternal(false);
            storeOrRemoveSession(session);
            manager.afterRequest();
        }
    }

    private void storeOrRemoveSession(Session session) {
        try {
            if (session != null) {
                if (session.isValid()) {
                    LOGGER.debug("Request with session completed, saving session " + session.getId());
                    if (session.getSession() != null) {
                        LOGGER.debug("HTTP Session present, saving " + session.getId());
                        manager.save(session);
                    } else {
                        LOGGER.debug("No HTTP Session present, Not saving " + session.getId());
                    }
                } else {
                    LOGGER.debug("HTTP Session has been invalidated, removing :" + session.getId());
                    manager.remove(session);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception during storeOrRemoveSession", e);
        }
    }
}
