package com.zackjp.hiccup.client;

import android.content.ContentValues;

public interface RequestAdapter {
    ContentValues toValues(Object model);
}
