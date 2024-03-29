package fr.xebia.xke.redis.valve.done;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.HashMap;

public class RedisSession extends StandardSession {

    private static Logger log = LoggerFactory.getLogger(RedisSession.class);

    protected HashMap<String, Object> changedAttributes;
    protected Boolean dirty;

    public RedisSession(Manager manager) {
        super(manager);
        resetDirtyTracking();
    }

    public Boolean isDirty() {
        return dirty || !changedAttributes.isEmpty();
    }

    public HashMap<String, Object> getChangedAttributes() {
        return changedAttributes;
    }

    public void resetDirtyTracking() {
        changedAttributes = new HashMap<String, Object>();
        dirty = false;
    }

    public void setAttribute(String key, Object value) {
        Object oldValue = getAttribute(key);
        if (value == null && oldValue != null
                || oldValue == null && value != null
                || !value.getClass().isInstance(oldValue)
                || !value.equals(oldValue)) {
            changedAttributes.put(key, value);
        }

        super.setAttribute(key, value);
    }

    public void removeAttribute(String name) {
        dirty = true;
        super.removeAttribute(name);
    }

    @Override
    public void setId(String id) {
        // Specifically do not call super(): it's implementation does unexpected things
        // like calling manager.remove(session.id) and manager.add(session).

        this.id = id;
    }

    public void setPrincipal(Principal principal) {
        dirty = true;
        super.setPrincipal(principal);
    }


}
