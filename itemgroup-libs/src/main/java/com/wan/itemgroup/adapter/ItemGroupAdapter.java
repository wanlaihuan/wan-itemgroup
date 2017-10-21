package com.wan.itemgroup.adapter;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.yingt.common.util.IEventProcessor;
import com.yingt.common.util.RxThread;
import com.yingt.common.util.RxTimerUtil;
import com.wan.itemgroup.R;
import com.wan.itemgroup.cache.DataCache;
import com.wan.itemgroup.listener.OnDragVHListener;
import com.wan.itemgroup.listener.OnItemMoveListener;
import com.wan.itemgroup.model.ItemGroupBean;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.ObservableEmitter;
import io.reactivex.Scheduler;

/**
 * @author laihuan.wan
 * 拖拽排序 + 增删
 * Created by laihuan.wan on 17/10/12.
 */
public class ItemGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {

    private Context mContext;
    public static final int TYPE_HEADER = 0;  // 标题部分
    public static final int TYPE_FIRST_GROUP = 1;  //  第一组
    public static final int TYPE_GROUP = 2;  // 普通组

    private static final int STATE_NORMAL = 0; // 正常状态
    private static final int STATE_ADDIBLE = 1; // 可添加状态
    private static final int STATE_DELETABLE = 2; // 可删除状态
    private static final int STATE_SELECTED = 3; // 已选状态

    private int firstGroupEndIndex = 0; // 第一组中最后选项的索引

    // touch 点击开始时间
    private long startTime;
    // touch 间隔时间  用于分辨是否是 "点击"
    private static final long SPACE_TIME = 100;
    private LayoutInflater mInflater;
    private ItemTouchHelper mItemTouchHelper;
    // 是否为 编辑 模式
    private boolean isEditMode = false;

    /**
     * 用来存储 recycleView 列表的数据
     **/
    private List<Object> positionDatas = new ArrayList<>();

    // 我的频道点击事件
    private OnItemClickListener mOnItemClickListener;

    public ItemGroupAdapter(Context context, ItemTouchHelper helper) {
        this.mInflater = LayoutInflater.from(context);
        this.mItemTouchHelper = helper;
        mContext = context;
    }

    public void setDatas(List<ItemGroupBean.GroupBean> groupBeans) {
        if (groupBeans == null) {
            return;
        }

        positionDatas.clear();
        int count = 0;
        for (int i = 0; i < groupBeans.size(); i++) {
            count++;

            // 存储 Title 到内存，是以 position 的顺序存储
            positionDatas.add(groupBeans.get(i).getGroupTitle());

            List<ItemGroupBean.GroupBean.GroupItemsBean> groupItemsBeans =
                    groupBeans.get(i).getGroupItems();
            for (int i1 = 0; i1 < groupItemsBeans.size(); i1++) {
                count++;

                // 存储 选项数据到内存
                positionDatas.add(groupItemsBeans.get(i1));
            }

            //  遍历到最后需要记录第一组最后的一个索引
            if (i == 0) {
                firstGroupEndIndex = count - 1;
            }
        }
    }

    private ItemGroupBean listToDataModel() {
        ItemGroupBean itemGroupBean = new ItemGroupBean();
        List<ItemGroupBean.GroupBean> groupBeans = new ArrayList<>();
        ItemGroupBean.GroupBean groupBean = null;
        List<ItemGroupBean.GroupBean.GroupItemsBean> groupItemsBeans = null;

        int groupCount = 0;
        int currentGroupCount = 0;

        for (int i = 0; i < positionDatas.size(); i++) {

            Object objectData = positionDatas.get(i);

            if (objectData instanceof String) {
                groupCount++;
                if (currentGroupCount != groupCount) {
                    // 保存
                    if (groupBean != null) {
                        groupBean.setGroupItems(groupItemsBeans);
                        groupBeans.add(groupBean);
                    }
                    groupBean = new ItemGroupBean.GroupBean();
                    groupItemsBeans = new ArrayList<>();

                    groupBean.setGroupTitle((String) objectData);
                }

                currentGroupCount = groupCount;

            } else if (objectData instanceof ItemGroupBean.GroupBean.GroupItemsBean) {
                groupItemsBeans.add((ItemGroupBean.GroupBean.GroupItemsBean) objectData);

            }
        }

        // 最后一组再保存
        if (groupBean != null) {
            groupBean.setGroupItems(groupItemsBeans);
            groupBeans.add(groupBean);
        }
        itemGroupBean.setGroup(groupBeans);

        return itemGroupBean;
    }

    /**
     * 获取组的 title
     *
     * @param position
     * @return
     */
    private String getGroupTitle(int position) {
        Object objectData = positionDatas.get(position);
        String title = "";
        if (objectData instanceof String) {
            title = (String) objectData;
        }

        return title;
    }

    /**
     * 获取组中选项数据对象
     *
     * @param position
     * @return
     */
    private ItemGroupBean.GroupBean.GroupItemsBean getGroupItemBean(int position) {
        Object objectData = positionDatas.get(position);
        ItemGroupBean.GroupBean.GroupItemsBean groupItemBean = null;
        if (objectData instanceof ItemGroupBean.GroupBean.GroupItemsBean) {
            groupItemBean = (ItemGroupBean.GroupBean.GroupItemsBean) objectData;
        }

        return groupItemBean;
    }

    /**
     * 改变编辑模式
     *
     * @return
     */
    public boolean chanageEditMode() {
        if (!isEditMode) {
            startEditMode();
        } else {
            cancelEditMode();
            RxThread.eventProcessor(new IEventProcessor() {
                @Override
                public String onSubscribe(ObservableEmitter<Object> emitter) {
                    ItemGroupBean newGroupModel = listToDataModel();
                    Gson gson = new Gson();
                    String newJsonConfig = gson.toJson(newGroupModel);
                    DataCache.saveGroupConfig(newJsonConfig);

                    // 发送更新通知，更新上层数据
                    EventBus.getDefault().post(newGroupModel);
                    return null;
                }

                @Override
                public Scheduler onSubscribeThreadType() {
                    return null;
                }

                @Override
                public void onAcceptUiThread(String sendId) {

                }
            });
        }
        return isEditMode;
    }

    @Override
    public int getItemViewType(int position) {
        if (positionDatas.get(position) instanceof String) { //  说明是 title 类型
            return TYPE_HEADER;
        } else if (position > 0 && position <= firstGroupEndIndex) {
            return TYPE_FIRST_GROUP;
        } else {
            return TYPE_GROUP;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view;
        switch (viewType) {
            case TYPE_HEADER:
                view = mInflater.inflate(R.layout.yt_item_group_header_layout, parent, false);
                return new HeaderViewHolder(view);

            case TYPE_FIRST_GROUP:
            case TYPE_GROUP:
                view = mInflater.inflate(R.layout.yt_item_group_item_layout, parent, false);
                final GroupViewHolder groupHolder = new GroupViewHolder(view);

                groupHolder.itemParent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {

                        int position = groupHolder.getAdapterPosition();
                        ItemGroupBean.GroupBean.GroupItemsBean groupItemBean
                                = getGroupItemBean(position);

                        if (isEditMode) {
                            if (groupItemBean != null) {
                                if (groupItemBean.getBadgeState() == STATE_DELETABLE) {
                                    moveMyToOther(groupHolder);
                                } else if (groupItemBean.getBadgeState() == STATE_ADDIBLE) {
                                    moveOtherToMy(groupHolder);
                                }
                            }
                        } else {
                            if (mOnItemClickListener != null && groupItemBean != null) {
                                mOnItemClickListener.onItemClick(v, groupItemBean);
                            }
                        }
                    }
                });

                // 只有第一组的可删除状态的按钮才可操作如下事件
                if (viewType == TYPE_FIRST_GROUP) {
                    groupHolder.itemParent.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(final View v) {
                            int position = groupHolder.getAdapterPosition();
                            ItemGroupBean.GroupBean.GroupItemsBean groupItemBean
                                    = getGroupItemBean(position);
                            if (groupItemBean != null &&
                                    groupItemBean.getBadgeState() == STATE_DELETABLE) {
                                mItemTouchHelper.startDrag(groupHolder);
                            }
                            return true;
                        }
                    });

                    groupHolder.itemParent.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            int position = groupHolder.getAdapterPosition();
                            ItemGroupBean.GroupBean.GroupItemsBean groupItemBean
                                    = getGroupItemBean(position);
                            if (groupItemBean != null &&
                                    groupItemBean.getBadgeState() != STATE_DELETABLE) {
                                return false;
                            }

                            if (isEditMode) {
                                switch (MotionEventCompat.getActionMasked(event)) {
                                    case MotionEvent.ACTION_DOWN:
                                        startTime = System.currentTimeMillis();
                                        break;
                                    case MotionEvent.ACTION_MOVE:
                                        if (System.currentTimeMillis() - startTime > SPACE_TIME) {
                                            mItemTouchHelper.startDrag(groupHolder);
                                        }
                                        break;
                                    case MotionEvent.ACTION_CANCEL:
                                    case MotionEvent.ACTION_UP:
                                        startTime = 0;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            return false;
                        }
                    });
                }

                return groupHolder;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) { // 头部

            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvTitle.setText(getGroupTitle(position));

        } else if (holder instanceof GroupViewHolder) { // 组

            final GroupViewHolder groupHolder = (GroupViewHolder) holder;
            ItemGroupBean.GroupBean.GroupItemsBean groupItemBean = getGroupItemBean(position);
            if (groupItemBean != null) {
                groupHolder.textView.setText(groupItemBean.getTitle());
            }

            if (isEditMode) {

                groupHolder.imgEdit.setVisibility(View.VISIBLE);
                // 正常状态则隐藏
                if (groupItemBean.getBadgeState() == STATE_NORMAL) {
                    groupHolder.imgEdit.setVisibility(View.INVISIBLE);

                } else if (groupItemBean.getBadgeState() == STATE_DELETABLE) {
                    groupHolder.imgEdit.setImageResource(R.drawable.yt_item_delete_btn);
                } else if (groupItemBean.getBadgeState() == STATE_ADDIBLE) {
                    groupHolder.imgEdit.setImageResource(R.drawable.yt_item_add_btn);
                } else if (groupItemBean.getBadgeState() == STATE_SELECTED) {
                    groupHolder.imgEdit.setImageResource(R.drawable.yt_item_selected_btn);
                }

            } else {
                groupHolder.imgEdit.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return positionDatas.size();
    }

    /**
     * 我的频道 移动到 其他频道
     *
     * @param holder
     */
    private void moveMyToOther(GroupViewHolder holder) {
        final int position = holder.getAdapterPosition();

        RxThread.eventProcessor(new IEventProcessor() {
            @Override
            public String onSubscribe(ObservableEmitter<Object> emitter) {
                // 处理其他组的状态
                ItemGroupBean.GroupBean.GroupItemsBean groupItemBean = getGroupItemBean(position);
                final String srcUniqueId = groupItemBean.getUniqueId();
                for (int i = firstGroupEndIndex; i < positionDatas.size(); i++) {
                    ItemGroupBean.GroupBean.GroupItemsBean groupItemBean1 = getGroupItemBean(i);
                    if (groupItemBean1 != null && groupItemBean1.getUniqueId().equals(srcUniqueId)) {
                        groupItemBean1.setBadgeState(STATE_ADDIBLE);
                    }
                }
                // 删除第一组中的选项
                firstGroupEndIndex -= 1;
                positionDatas.remove(position);
                return null;
            }

            @Override
            public Scheduler onSubscribeThreadType() {
                return null;
            }

            @Override
            public void onAcceptUiThread(String sendId) {
                notifyItemRemoved(position);
            }
        });

        RxTimerUtil.timer(600, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(long number) {
                notifyDataSetChanged();
            }
        });
    }

    /**
     * 其他频道 移动到 我的频道
     *
     * @param groupHolder
     */
    private void moveOtherToMy(GroupViewHolder groupHolder) {
        int position = groupHolder.getAdapterPosition();
        if (groupHolder.getAdapterPosition() == -1) {
            return;
        }

        final ItemGroupBean.GroupBean.GroupItemsBean groupItemBean
                = getGroupItemBean(position);
        ItemGroupBean.GroupBean.GroupItemsBean groupItemBean1
                = new ItemGroupBean.GroupBean.GroupItemsBean();
        groupItemBean1.setUniqueId(groupItemBean.getUniqueId());
        groupItemBean1.setIconUrl(groupItemBean.getIconUrl());
        groupItemBean1.setTitle(groupItemBean.getTitle());
        groupItemBean1.setBadgeState(STATE_DELETABLE);
        //  新增加后的选项索引
        positionDatas.add(firstGroupEndIndex, groupItemBean1);
        notifyItemInserted(firstGroupEndIndex);
        firstGroupEndIndex += 1;

        // 更新其他组的选项状态
        RxTimerUtil.timer(400, new RxTimerUtil.IRxNext() {
            @Override
            public void doNext(long number) {
                RxThread.eventProcessor(new IEventProcessor() {
                    @Override
                    public String onSubscribe(ObservableEmitter<Object> emitter) {
                        // 更新为已选状态
                        int listSize = positionDatas.size();
                        for (int i = 0; i < listSize; i++) {
                            ItemGroupBean.GroupBean.GroupItemsBean item
                                    = getGroupItemBean(i);
                            if (item != null &&
                                    groupItemBean.getUniqueId().equals(item.getUniqueId())
                                    && item.getBadgeState() != STATE_DELETABLE) {
                                item.setBadgeState(STATE_SELECTED);
                            }
                        }
                        return null;
                    }

                    @Override
                    public Scheduler onSubscribeThreadType() {
                        return null;
                    }

                    @Override
                    public void onAcceptUiThread(String sendId) {
                        notifyDataSetChanged();
                    }
                });
            }
        });
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        ItemGroupBean.GroupBean.GroupItemsBean groupItemBean = getGroupItemBean(toPosition);
        if (groupItemBean != null) {
            if (groupItemBean.getBadgeState() == STATE_DELETABLE) { // 只有是第一组可删除的才允许拖动
                notifyItemMoved(fromPosition, toPosition);

                // 更新内存中的顺序
                ItemGroupBean.GroupBean.GroupItemsBean groupItemBean1
                        = getGroupItemBean(fromPosition);
                if (groupItemBean1 != null) {
                    positionDatas.remove(fromPosition);
                    positionDatas.add(toPosition, groupItemBean1);
                }
            }
        }
    }

    /**
     * 开启编辑模式
     */
    public void startEditMode() {
        isEditMode = true;
        notifyDataSetChanged();
    }

    /**
     * 完成编辑模式
     */
    public void cancelEditMode() {
        isEditMode = false;
//        for (int i = 1; i < 9; i++) {
//            positionDatas.remove(1);
//        }
        notifyDataSetChanged();
//        notifyItemRangeRemoved(1, 8);
    }

    public interface OnItemClickListener {
        /**
         * 点击事件响应回调
         * @param v
         * @param item
         */
        void onItemClick(View v, ItemGroupBean.GroupBean.GroupItemsBean item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    /**
     * 我的频道
     */
    class GroupViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
        private RelativeLayout itemParent;
        private TextView textView;
        private ImageView imgEdit;

        public GroupViewHolder(View itemView) {
            super(itemView);
            itemParent = (RelativeLayout) itemView.findViewById(R.id.item_parent);
            textView = (TextView) itemView.findViewById(R.id.tv);
            imgEdit = (ImageView) itemView.findViewById(R.id.img_edit);
        }

        /**
         * item 被选中时
         */
        @Override
        public void onItemSelected() {
//            itemParent.setBackgroundResource(R.drawable.bg_channel_p);
        }

        /**
         * item 取消选中时
         */
        @Override
        public void onItemFinish() {
//            textView.setBackgroundResource(R.drawable.bg_channel);
        }
    }

    /**
     * 我的频道  标题部分
     */
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tv);
        }
    }
}
