package org.simqle;


import java.util.HashMap;
import java.util.Map;

/**
 */
public class SqlContext {


    /**
     * Sets an object to this context.
     * The clazz parameter is a key; each key can be used only once.
     * @param clazz the key
     * @param impl the value to store
     * @param <T> type of the value
     * @throws IllegalArgumentException the key is in use
     */
    public <T> void set(Class<T> clazz, T impl) {
        if (theContext.containsKey(clazz)) {
            throw new IllegalArgumentException("Key is in use: "+clazz);
        }
        theContext.put(clazz, impl);
    }

    /**
     * Gets an object from the context by key
     * @param clazz the key
     * @param <T> type of the returned value
     * @return the object stored under this key; null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        return (T) theContext.get(clazz);
    }

    private final Map<Class<?>, Object> theContext = new HashMap<Class<?>, Object>();


}
