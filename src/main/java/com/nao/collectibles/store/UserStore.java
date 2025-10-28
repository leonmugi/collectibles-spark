package com.nao.collectibles.store;

import com.nao.collectibles.model.User;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserStore {
    private final Map<String, User> db = new ConcurrentHashMap<>();

    public Collection<User> findAll() { return db.values(); }
    public User findById(String id) { return db.get(id); }
    public boolean exists(String id) { return db.containsKey(id); }

    public boolean create(String id, User u) {
        if (db.containsKey(id)) return false;
        u.setId(id);
        db.put(id, u);
        return true;
    }

    public boolean update(String id, User u) {
        if (!db.containsKey(id)) return false;
        u.setId(id);
        db.put(id, u);
        return true;
    }

    public boolean delete(String id) {
        return db.remove(id) != null;
    }
}
