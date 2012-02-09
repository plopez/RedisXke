package fr.xebia.xke.redis.valve.skeleton;


import javax.servlet.http.HttpSession;
import java.io.IOException;

public class RedisSerializer {
    private ClassLoader loader;

    public void setClassLoader(ClassLoader loader) {
        this.loader = loader;
    }

    public byte[] serializeFrom(HttpSession session) throws IOException {
        return null;
    }

    public HttpSession deserializeInto(byte[] data, HttpSession session) throws IOException, ClassNotFoundException {

        return null;
    }
}
