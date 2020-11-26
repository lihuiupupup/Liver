package com.lihui.android.liver.adapter;

import com.lihui.android.liver.filter.BaseFilter;

public class FilterBean {
    private String name;
    private int icon;
    private BaseFilter filter;
    private boolean isSelect;


    public FilterBean(String name, int icon, BaseFilter filter,boolean isSelect) {
        this.name = name;
        this.icon = icon;
        this.filter = filter;
        this.isSelect = isSelect;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public BaseFilter getFilter() {
        return filter;
    }

    public void setFilter(BaseFilter filter) {
        this.filter = filter;
    }

    public boolean isSelect() {
        return isSelect;
    }

    public void setSelect(boolean select) {
        isSelect = select;
    }
}
