package com.wlanadb.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.graphics.Typeface;
import android.net.wifi.WifiConfiguration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.wlanadb.R;

public class TrustedHotspotsAdapter extends BaseAdapter {

  private final LayoutInflater mInflater;

  private final HotspotsComparator mHotspotsComparator = new HotspotsComparator();
  private final ArrayList<WifiConfiguration> mHotspots = new ArrayList<WifiConfiguration>();
  private final HashSet<String> mTrustedSSIDs = new HashSet<String>();

  private final HotspotClickListener mOnClickListener = new HotspotClickListener();

  public TrustedHotspotsAdapter(Context context) {
    mInflater = LayoutInflater.from(context);
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
    final ViewHolder holder;
    if (convertView == null) {
      holder = new ViewHolder();
      convertView = mInflater.inflate(R.layout.list_item_trusted_hotspot, parent, false);
      holder.text = (TextView) convertView.findViewById(android.R.id.text1);
      holder.check = (CheckBox) convertView.findViewById(android.R.id.checkbox);

      convertView.setOnClickListener(mOnClickListener);

      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    final String ssid = formatSSID(item.SSID);
    holder.text.setText(ssid);
    holder.text.setTypeface(item.status == WifiConfiguration.Status.CURRENT ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    holder.check.setSelected(mTrustedSSIDs.contains(ssid));
    holder.ssid = ssid;

    return convertView;
  }

  private static String formatSSID(String ssid) {
    return ssid.replaceAll("\"", "");
  }

  private static class ViewHolder {
    private TextView text;
    private CheckBox check;

    private String ssid;
  }

  private class HotspotClickListener implements View.OnClickListener {

    @Override
    public void onClick(View v) {
      final Object tag = v.getTag();
      if (tag == null)
        return;

      if (tag instanceof ViewHolder) {
        final ViewHolder holder = (ViewHolder) tag;
        if (mTrustedSSIDs.contains(holder.ssid))
          mTrustedSSIDs.remove(holder.ssid);
        else
          mTrustedSSIDs.add(holder.ssid);
        notifyDataSetChanged();
      }
    }
  }

  private class HotspotsComparator implements Comparator<WifiConfiguration> {

    @Override
    public int compare(WifiConfiguration lhs, WifiConfiguration rhs) {
      boolean isLhsTrusted = mTrustedSSIDs.contains(formatSSID(lhs.SSID));
      boolean isRhsTrusted = mTrustedSSIDs.contains(formatSSID(rhs.SSID));

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
        return formatSSID(lhs.SSID).compareTo(formatSSID(rhs.SSID));
    }
  }
}
