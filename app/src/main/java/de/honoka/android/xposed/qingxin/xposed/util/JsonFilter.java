package de.honoka.android.xposed.qingxin.xposed.util;

import java.util.function.Function;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class JsonFilter implements Function<String, String> {

    private boolean lateInit;
}
