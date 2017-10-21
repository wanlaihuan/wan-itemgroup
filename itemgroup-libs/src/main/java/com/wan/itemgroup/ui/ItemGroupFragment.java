package com.wan.itemgroup.ui;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.Toast;

import com.wan.itemgroup.R;
import com.wan.itemgroup.adapter.ItemGroupAdapter;
import com.wan.itemgroup.helper.ItemDragHelperCallback;
import com.wan.itemgroup.model.ItemGroupBean;
import com.wan.itemgroup.util.GroupConfigUtils;
import com.yingt.common.util.IEventProcessor;
import com.yingt.common.util.RxThread;
import com.yingt.uimain.base.BaseToolbar;
import com.yingt.uimain.base.YtBaseFragment;

import io.reactivex.ObservableEmitter;
import io.reactivex.Scheduler;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;

/**
 * @author laihuan.wan
 *         Created by laihuan.wan on 2017/10/11 0011.
 */

public class ItemGroupFragment extends YtBaseFragment {
    private RecyclerView mRecy;
    private ItemGroupAdapter mAdapter;
    private ItemGroupToolbar shortcutToolbar;

    @Override
    public boolean isRemovedToolbar() {
        return false;
    }

    @Override
    public BaseToolbar getLayoutToolbarView() {
        shortcutToolbar = new ItemGroupToolbar(getActivity());
        return shortcutToolbar;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.yt_item_group_fragment_layout;
    }

    @Override
    public void onFindViewById() {
        super.onFindViewById();

        mRecy = (RecyclerView) findViewById(R.id.recy);
    }

    @Override
    public void onEventProcessor() {
        super.onEventProcessor();

        GridLayoutManager manager = new GridLayoutManager(getActivity(), 4);
        mRecy.setLayoutManager(manager);
        mRecy.setItemAnimator(new ScaleInAnimator());

        ItemDragHelperCallback callback = new ItemDragHelperCallback();
        final ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecy);
        mAdapter = new ItemGroupAdapter(getActivity(), helper);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = mAdapter.getItemViewType(position);

                return viewType == ItemGroupAdapter.TYPE_GROUP ||
                        viewType == ItemGroupAdapter.TYPE_FIRST_GROUP ? 1 : 4;
            }
        });

        RxThread.eventProcessor(new IEventProcessor() {
            @Override
            public String onSubscribe(ObservableEmitter<Object> emitter) {

                // 解析出快捷按钮
                GroupConfigUtils groupConfigUtils = new GroupConfigUtils();
                ItemGroupBean itemGroupBean = groupConfigUtils.getItemGroupBean(ItemGroupBean.class);
                if (itemGroupBean != null) {
                    mAdapter.setDatas(itemGroupBean.getGroup());
                }

                return null;
            }

            @Override
            public Scheduler onSubscribeThreadType() {
                return null;
            }

            @Override
            public void onAcceptUiThread(String sendId) {
                mRecy.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new ItemGroupAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, ItemGroupBean.GroupBean.GroupItemsBean item) {
                        Toast.makeText(getActivity(), item.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                });
                if (shortcutToolbar != null) {
                    shortcutToolbar.setChannelAdapter(mAdapter);
                }
            }
        });
    }
}
