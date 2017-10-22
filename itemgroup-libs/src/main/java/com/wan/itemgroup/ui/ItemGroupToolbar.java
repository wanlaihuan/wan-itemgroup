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
    private TextView tvFinishBtn;
    private ItemGroupAdapter adapter;

    public ItemGroupToolbar(Context context) {
        this(context, null);
    }

    public ItemGroupToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemGroupToolbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

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

    public void setChannelAdapter(ItemGroupAdapter adapter) {
        this.adapter = adapter;
        adapter.setOnModeChangedListener(new ItemGroupAdapter.OnModeChangedListener() {
            @Override
            public void onChanged(boolean isEditMode) {
                if (isEditMode) {
                    tvFinishBtn.setVisibility(View.VISIBLE);
                    tvFinishBtn.setText(R.string.finish);
                }else{
                    tvFinishBtn.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onFindViewById() {
        super.onFindViewById();

        tvFinishBtn = (TextView) findViewById(R.id.btn_edit);
        tvFinishBtn.setVisibility(View.GONE);
        tvFinishBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (adapter != null) {
                    adapter.chanageEditMode();
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
        setToolbarBackIcon(R.drawable.yt_toolbar_back_btn);
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
