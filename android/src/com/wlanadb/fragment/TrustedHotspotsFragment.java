package com.wlanadb.fragment;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.wlanadb.R;
import com.wlanadb.ui.BlockingFrameLayout;
import com.wlanadb.ui.TrustedHotspotsAdapter;
import com.wlanadb.utils.WiFiUtils;

public class TrustedHotspotsFragment extends Fragment {

  private TrustedHotspotsAdapter mAdapter;

  private BlockingFrameLayout viewContainer;
  private ExpandableListView viewList;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    final View v = inflater.inflate(R.layout.fragment_trusted_hotspots, container, false);
    viewContainer = (BlockingFrameLayout) v.findViewById(R.id.container);
    viewList = (ExpandableListView) v.findViewById(android.R.id.list);

    final List<WifiConfiguration> hotspots = WiFiUtils.getHotspotsList(getActivity());
    mAdapter = new TrustedHotspotsAdapter(getActivity());
    mAdapter.setHotspots(hotspots);

    viewList.setAdapter(mAdapter);
    final int groupsCount = mAdapter.getGroupCount();
    for(int i=0; i<groupsCount; ++i)
      viewList.expandGroup(i);

    viewList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
      @Override
      public boolean onGroupClick(ExpandableListView parent, View v,
          int groupPosition, long id) {
        return true;  // This way the expander cannot be collapsed
      }
    });

    return v;
  }

  public void setEnabled(boolean enabled) {
    viewContainer.setEnabled(enabled);
  }

  public Set<String> getTrustedSSIDs() {
    return mAdapter.getTrustedSSIDs();
  }

  public void setTrustedSSIDs(Collection<String> ssids) {
    mAdapter.setTrustedSSIDs(ssids);
  }
}
