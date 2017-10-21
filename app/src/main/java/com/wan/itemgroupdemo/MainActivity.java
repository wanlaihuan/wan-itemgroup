package com.wan.itemgroupdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.wan.itemgroup.ui.ItemGroupFragment;
import com.yingt.uimain.UiMainInit;
import com.yingt.uimain.util.UiMain;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: 初始化 UI MAIN
        UiMainInit.with(this).init();
        UiMain.with(this)
                .loadV4Fragment(new ItemGroupFragment());
    }
}
