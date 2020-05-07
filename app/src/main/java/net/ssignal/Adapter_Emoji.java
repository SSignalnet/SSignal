package net.ssignal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class Adapter_Emoji extends BaseAdapter {

    private List<? extends Map<String, ?>> 列表项;
    private int 项布局;
    private String[] From;
    private int[] To;
    private LayoutInflater 布局扩充器;

    public Adapter_Emoji(Context 场景, List<? extends Map<String, ?>> 列表项, int 项布局, String[] From, int[] To) {
        this.列表项 = 列表项;
        this.项布局 = 项布局;
        this.From = From;
        this.To = To;
        布局扩充器 = (LayoutInflater) 场景.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return 列表项.size();
    }

    @Override
    public Object getItem(int position) {
        return 列表项.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = 布局扩充器.inflate(项布局, viewGroup, false);
        }
        final Map 某一项 = 列表项.get(position);
        if (某一项 != null) {
            for (int i = 0; i < To.length; i++) {
                View 控件 = view.findViewById(To[i]);
                if (控件 != null) {
                    final Object 数据 = 某一项.get(From[i]);
                    String 文字 = 数据 == null ? "" : 数据.toString();
                    if (文字 == null) { 文字 = ""; }
                    if (控件 instanceof TextView) {
                        ((TextView)控件).setText(文字);
                    }
                }
            }
        }
        return view;
    }
}
