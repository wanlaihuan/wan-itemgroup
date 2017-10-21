package com.wan.itemgroup.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.wan.itemgroup.R;
import com.wan.itemgroup.adapter.ItemGroupAdapter;
import com.yingt.uimain.base.YtBaseToolbar;
import com.yingt.uimain.util.Res;

/**
 * @author laihuan.wan
 * Created by laihuan.wan on 2017/10/11 0011.
 */

public class ItemGroupToolbar extends YtBaseToolbar {

    public ItemGroupToolbar(Context context) {
        this(context, null);
    }

    public ItemGroupToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemGroupToolbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    private ItemGroupAdapter adapter;

    public void setChannelAdapter(ItemGroupAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int getLayoutLeftResId() {
        return super.getLayoutLeftResId();
    }


    @Override
    public int getLayoutTitleResId() {
        return super.getLayoutTitleResId();
    }

    @Override
    public int getLayoutRightMenuResId() {
        return R.layout.yt_item_group_title_menu;
    }

    @Override
    public void onFindViewById() {
        super.onFindViewById();

        final TextView textView = (TextView) findViewById(R.id.btn_edit);
        textView.setText(R.string.edit);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null) {
                    boolean isEditMode = adapter.chanageEditMode();
                    if (isEditMode) {
                        textView.setText(R.string.finish);
                    } else {
                        textView.setText(R.string.edit);
                    }
                }
            }
        });
    }

    @Override
    public void onInitUiData() {
        super.onInitUiData();

        setToolbarBackgroundColor(Res.getColor(R.color.yt_edit_title_bg_color));
        setToolbarTitle(Res.getString(R.string.yt_edit_title));
        setToolbarTitleColor(Res.getColor(R.color.yt_edit_title_text_color));
        setToolbarDividerColor(Res.getColor(R.color.yt_edit_title_divider_color));
    }

    @Override
    public void onBackBtnPressed() {
        super.onBackBtnPressed();
    }

    @Override
    public void onEventProcessor() {
        super.onEventProcessor();
    }

}
