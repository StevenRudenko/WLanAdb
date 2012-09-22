package com.wlanadb.fragment;

import java.util.List;

import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.wlanadb.R;
import com.wlanadb.ui.BlockingFrameLayout;
import com.wlanadb.ui.TrustedHotspotsAdapter;
import com.wlanadb.utils.WiFiUtils;

public class TrustedHotspotsFragment extends Fragment {

  private TrustedHotspotsAdapter mAdapter;

  private BlockingFrameLayout viewContainer;
  private ListView viewList;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final View v = inflater.inflate(R.layout.fragment_trusted_hotspots, container, false);
    viewContainer = (BlockingFrameLayout) v.findViewById(R.id.container);
    viewList = (ListView) v.findViewById(android.R.id.list);

    final List<WifiConfiguration> hotspots = WiFiUtils.getHotspotsList(getActivity());
    mAdapter = new TrustedHotspotsAdapter(getActivity());
    mAdapter.setHotspots(hotspots);

    viewList.setAdapter(mAdapter);

    return v;
  }

  public void setEnabled(boolean enabled) {
    viewContainer.setEnabled(enabled);
  }

}
