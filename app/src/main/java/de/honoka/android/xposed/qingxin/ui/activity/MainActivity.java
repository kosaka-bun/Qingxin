package de.honoka.android.xposed.qingxin.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import de.honoka.android.xposed.qingxin.R;
import de.honoka.android.xposed.qingxin.util.AndroidUtils;

public class MainActivity extends AppCompatActivity {

    public final Map<Integer, Runnable> onPermissionsResultCallBacks =
            new HashMap<>();

    public final Map<Integer, Consumer<Intent>> onActivityResultCallBacks =
            new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AndroidUtils.setActionBarIcon(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Runnable callback = onPermissionsResultCallBacks.get(requestCode);
        if(callback != null) {
            callback.run();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Consumer<Intent> callback = onActivityResultCallBacks.get(requestCode);
        if(callback != null) {
            callback.accept(data);
        }
    }
}
