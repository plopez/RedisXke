package fr.xebia.xke.redis.valve.skeleton;


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
        // TODO
    }

    private void storeOrRemoveSession(Session session) {
        // TODO
    }
}
