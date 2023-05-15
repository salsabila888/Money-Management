package com.sdd.utils;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.WeakHashMap;

import org.zkoss.zul.SimpleListModel;

public final class ListModelFlyweight extends SimpleListModel {
    private static final WeakHashMap< ListModelFlyweight, WeakReference< ListModelFlyweight >> FLYWEIGHT_DATA =
            new WeakHashMap< ListModelFlyweight, WeakReference< ListModelFlyweight >>();

    private final Long dataSize;

    private final String nameStartsWith;

    private final String queryName;

    public ListModelFlyweight (List modelData, String nameStartsWith, String queryName) {
        super(modelData);
        this.dataSize = modelData != null ? modelData.size() : 0L;
        this.nameStartsWith = nameStartsWith;
        this.queryName = queryName;
    }

    public static ListModelFlyweight create(List modelData, String nameStartsWith, String queryName) {
        ListModelFlyweight model = new ListModelFlyweight (modelData, nameStartsWith, queryName);
        if (!FLYWEIGHT_DATA.containsKey(model)) {
            FLYWEIGHT_DATA.put(model, new WeakReference< ListModelFlyweight >(model));
        }
        return FLYWEIGHT_DATA.get(model).get();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListModelFlyweight) {
            if (obj == this) {
                return true;
            }
            ListModelFlyweight other = (ListModelFlyweight) obj;
            return other.dataSize.equals(dataSize) && other.queryName.equals(queryName) &&
                    (nameStartsWith != null ? nameStartsWith.equals(other.nameStartsWith) : (other.nameStartsWith == null));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return ((dataSize != null ? dataSize.hashCode() : 0) * 17 + (nameStartsWith != null ? nameStartsWith.hashCode() : 1) * 33 + queryName.hashCode() + 9);
    }
}
