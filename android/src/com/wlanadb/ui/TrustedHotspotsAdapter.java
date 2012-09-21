package com.wlanadb.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TrustedHotspotsAdapter extends BaseAdapter {

  private final Context mContext;

  private final HotspotsComparator mHotspotsComparator = new HotspotsComparator();
  private final ArrayList<WifiConfiguration> mHotspots = new ArrayList<WifiConfiguration>();
  private final HashSet<String> mTrustedSSIDs = new HashSet<String>();

  public TrustedHotspotsAdapter(Context context) {
    mContext = context;
  }

  public void setHotspots(Collection<WifiConfiguration> items) {
    mHotspots.clear();
    mHotspots.addAll(items);
    notifyDataSetChanged();
  }

  public void setTrustedSSIDs(Collection<String> ssids) {
    mTrustedSSIDs.clear();
    mTrustedSSIDs.addAll(ssids);
    notifyDataSetChanged();
  }

  public Set<String> getTrustedSSIDs() {
    return mTrustedSSIDs;
  }

  @Override
  public void notifyDataSetChanged() {
    Collections.sort(mHotspots, mHotspotsComparator);
    super.notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return mHotspots.size();
  }

  @Override
  public WifiConfiguration getItem(int position) {
    return mHotspots.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final WifiConfiguration item = getItem(position);
    final TextView tv;
    if (convertView == null) {
      tv = new TextView(mContext);
      convertView = tv;
    } else {
      tv = (TextView) convertView;
    }

    tv.setText(item.SSID);

    return convertView;
  }

  private class HotspotsComparator implements Comparator<WifiConfiguration> {

    @Override
    public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
      boolean isLhsTrusted = mTrustedSSIDs.contains(lhs.SSID);
      boolean isRhsTrusted = mTrustedSSIDs.contains(rhs.SSID);

      if (isLhsTrusted && isRhsTrusted) {
        return compareEqualyTrusted(lhs, rhs);
      } else if (!isLhsTrusted && !isRhsTrusted) {
        return compareEqualyTrusted(lhs, rhs);
      } else if (isLhsTrusted) {
        return -1;
      } else if (isRhsTrusted) {
        return 1;
      }

      return 0;
    }

    private int compareEqualyTrusted(WifiConfiguration lhs, WifiConfiguration rhs) {
      if (lhs.status == WifiConfiguration.Status.CURRENT)
        return -1;
      else if (rhs.status == WifiConfiguration.Status.CURRENT)
        return 1;
      else
        return lhs.SSID.compareTo(rhs.SSID);
    }
  }
}
