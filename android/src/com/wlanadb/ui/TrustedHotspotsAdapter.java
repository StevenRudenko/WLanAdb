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
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.wlanadb.R;

public class TrustedHotspotsAdapter extends BaseExpandableListAdapter {

  private static final int GROUP_TRUSTED = 0;
  @SuppressWarnings("unused")
  private static final int GROUP_UNTRUSTED = 1;
  private static final int GROUPS_COUNT = 2;

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
    if (items != null)
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

  public int getCount() {
    return mHotspots.size();
  }

  @Override
  public WifiConfiguration getChild(int groupPosition, int childPosition) {
    return mHotspots.get(mTrustedSSIDs.size() * groupPosition + childPosition);
  }

  @Override
  public long getChildId(int groupPosition, int childPosition) {
    return groupPosition * mTrustedSSIDs.size() + childPosition;
  }

  @Override
  public View getChildView(int groupPosition, int childPosition,
      boolean isLastChild, View convertView, ViewGroup parent) {
    final WifiConfiguration item = getChild(groupPosition, childPosition);
    final ChildViewHolder holder;
    if (convertView == null) {
      holder = new ChildViewHolder();
      convertView = mInflater.inflate(R.layout.list_item_trusted_hotspot, parent, false);
      holder.text = (TextView) convertView.findViewById(android.R.id.text1);
      holder.check = (CheckBox) convertView.findViewById(android.R.id.checkbox);
      holder.divider = convertView.findViewById(R.id.divider);

      convertView.setOnClickListener(mOnClickListener);

      convertView.setTag(holder);
    } else {
      holder = (ChildViewHolder) convertView.getTag();
    }

    final String ssid = formatSSID(item.SSID);
    holder.text.setText(ssid);
    holder.text.setTypeface(item.status == WifiConfiguration.Status.CURRENT ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
    final boolean isChecked = mTrustedSSIDs.contains(ssid);
    holder.check.setSelected(isChecked);
    convertView.setBackgroundResource(isChecked ? R.drawable.list_item_trusted_hotspots_checked : R.drawable.list_item_trusted_hotspots_unchecked);
    holder.ssid = ssid;

    if (groupPosition == GROUP_TRUSTED && isLastChild) {
      holder.divider.setVisibility(View.GONE);
    } else {
      holder.divider.setVisibility(View.VISIBLE);
    }

    return convertView;
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    if (groupPosition == GROUP_TRUSTED)
      return mTrustedSSIDs.size();

    return mHotspots.size() - mTrustedSSIDs.size();
  }

  @Override
  public Object getGroup(int groupPosition) {
    return groupPosition;
  }

  @Override
  public int getGroupCount() {
    return GROUPS_COUNT;
  }

  @Override
  public long getGroupId(int groupPosition) {
    return groupPosition;
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded,
      View convertView, ViewGroup parent) {
    final GroupViewHolder holder;
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.list_item_trusted_hotspot_group, parent, false);
      holder = new GroupViewHolder();
      holder.title = (TextView) convertView.findViewById(R.id.groupTitle);
      holder.shadow = convertView.findViewById(R.id.shadow);
      convertView.setTag(holder);
    } else {
      holder = (GroupViewHolder) convertView.getTag();
    }

    final boolean isTrusted = groupPosition == GROUP_TRUSTED;
    holder.title.setText(isTrusted ? R.string.pref_security_trusted_hotspots_title : R.string.pref_security_untrusted_hotspots_title);
    holder.shadow.setVisibility(!isTrusted ? View.VISIBLE : View.GONE);
    convertView.setBackgroundResource(isTrusted ? R.color.list_item_trusted_hotspot_checked : R.color.list_item_trusted_hotspot_unchecked);

    return convertView;
  }

  @Override
  public boolean hasStableIds() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }

  private static String formatSSID(String ssid) {
    return ssid.replaceAll("\"", "");
  }

  private static class ChildViewHolder {
    private TextView text;
    private CheckBox check;
    private View divider;

    private String ssid;
  }

  private static class GroupViewHolder {
    private TextView title;
    private View shadow;
  }

  private class HotspotClickListener implements View.OnClickListener {

    @Override
    public void onClick(View v) {
      final Object tag = v.getTag();
      if (tag == null)
        return;

      if (tag instanceof ChildViewHolder) {
        final ChildViewHolder holder = (ChildViewHolder) tag;
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
