package com.example.QuanLyKTX.common;

/** Một mục trong sidebar. icon là id của biểu tượng trong fragments/icons.html. */
public class MenuItem {

    private final String label;
    private final String url;
    private final String icon;
    private final boolean active;
    private final String description;

    public MenuItem(String label, String url, String icon, boolean active, String description) {
        this.label = label;
        this.url = url;
        this.icon = icon;
        this.active = active;
        this.description = description;
    }

    public String getLabel() { return label; }
    public String getUrl() { return url; }
    public String getIcon() { return icon; }
    public boolean isActive() { return active; }
    /** Mô tả ngắn chức năng (theo vai trò), hiện ở lối tắt trang chủ. */
    public String getDescription() { return description; }
}
