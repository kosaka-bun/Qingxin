package de.honoka.android.xposed.qingxin.xposed.util;

import com.google.gson.JsonElement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class JsonFilter {

    private boolean lateInit;

    public abstract boolean isJsonWillBeFiltered(JsonElement je);

    public abstract String doFilter(JsonElement je);
}
