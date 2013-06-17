package com.wlanadb.ui;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider;

import com.wlanadb.data.ClientProto.Client;

public class ClientProvider extends ObservableMapLabelProvider {

  private static IObservableMap[] getAttributes(ObservableListContentProvider contentProvider) {
    return BeansObservables.observeMaps(contentProvider.getKnownElements(), Client.class, new String[] { "id", "ip" });
  }

  ClientProvider(ObservableListContentProvider contentProvider) {
    super(getAttributes(contentProvider));
  }

  @Override
  public String getColumnText(Object element, int index) {
    return "Hello World";
  }

}