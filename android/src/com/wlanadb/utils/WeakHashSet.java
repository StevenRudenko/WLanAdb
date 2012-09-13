package com.wlanadb.utils;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.WeakHashMap;

public class WeakHashSet<E> extends AbstractSet<E> {

  private WeakHashMap<E, Boolean> items = new WeakHashMap<E, Boolean>();

  @Override
  public boolean add(E object) {
    return items.put(object, Boolean.TRUE) == null;
  }

  @Override
  public void clear() {
    items.clear();
  }

  @Override
  public boolean contains(Object object) {
    return items.containsKey(object);
  }

  @Override
  public boolean isEmpty() {
    return items.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    return items.keySet().iterator();
  }

  @Override
  public boolean remove(Object object) {
    return items.remove(object);
  }

  @Override
  public int size() {
    return items.size();
  }
}
