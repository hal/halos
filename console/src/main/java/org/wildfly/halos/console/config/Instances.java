package org.wildfly.halos.console.config;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class Instances implements Iterable<Instance> {

    private final Map<String, Instance> instances;

    public Instances() {
        instances = new HashMap<>();
    }

    public int size() {return instances.size();}

    public boolean isEmpty() {return instances.isEmpty();}

    public boolean contains(String name) {return instances.containsKey(name);}

    public Instance add(Instance instance) {return instances.put(instance.name, instance);}

    public Instance get(String name) {return instances.get(name);}

    public Instance remove(String name) {return instances.remove(name);}

    @Override
    public Iterator<Instance> iterator() {
        return instances.values().iterator();
    }
}
