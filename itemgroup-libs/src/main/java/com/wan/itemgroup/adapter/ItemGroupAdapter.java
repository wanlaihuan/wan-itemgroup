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

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.wan.itemgroup.R;
import com.wan.itemgroup.cache.DataCache;
import com.wan.itemgroup.listener.OnDragVHListener;
import com.wan.itemgroup.listener.OnItemMoveListener;
import com.wan.itemgroup.model.GroupState;
import com.wan.itemgroup.model.ItemGroupBean;
import com.yingt.common.util.IEventProcessor;
import com.yingt.common.util.RxThread;
import com.yingt.common.util.RxTimerUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.ObservableEmitter;
import io.reactivex.Scheduler;

/**
 * @author laihuan.wan
 *         拖拽排序 + 增删
 *         Created by laihuan.wan on 17/10/12.
 */
public class ItemGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnItemMoveListener {

    private Context mContext;
    public static final int TYPE_HEADER = 0;  // 标题部分
    public static final int TYPE_FIRST_GROUP = 1;  //  第一组
    public static final int TYPE_GROUP = 2;  // 普通组
    public static final int TYPE_HEADER_NOR = 3;  // 正常状态下的标题部分

    private static final int ITEM_STATE_NORMAL = 0; // 正常状态
    private static final int ITEM_STATE_ADDIBLE = 1; // 可添加状态
    private static final int ITEM_STATE_DELETABLE = 2; // 可删除状态
    private static final int ITEM_STATE_SELECTED = 3; // 已选状态

    private GroupState mGroupState = GroupState.GROUP_STATE_NORMAL; // 正常状态

    private int firstGroupEndIndex = 0; // 第一组中最后选项的索引

    /**
     * touch 点击开始时间
     */
    private long startTime;
    /**
     * touch 间隔时间  用于分辨是否是 "点击"
     */
    private static final long SPACE_TIME = 100;
    private LayoutInflater mInflater;
    private ItemTouchHelper mItemTouchHelper;
    // 是否为 编辑 模式
    private boolean isEditMode = false;

    /**
     * 用来存储 recycleView 列表的所有数据（编辑状态界面下的数据列表）
     **/
    private ArrayList<Object> mPositionDatas = new ArrayList<>();
    /**
     * 正常状态下的数据，也即隐藏第一组时的数据列表
     */
    private ArrayList<Object> mNormalPositionDatas;
    private OnItemClickListener mOnItemClickListener;
    private OnModeChangedListener mOnModeChangedListener;

    public ItemGroupAdapter(Context context, ItemTouchHelper helper) {
        this.mInflater = LayoutInflater.from(context);
        this.mItemTouchHelper = helper;
        mContext = context;
    }

    public void setDatas(List<ItemGroupBean.GroupBean> groupBeans) {
        if (groupBeans == null) {
            return;
        }

        mPositionDatas.clear();
        int count = 0;
        for (int i = 0; i < groupBeans.size(); i++) {
            count++;

            // 存储 Title 到内存，是以 position 的顺序存储
            mPositionDatas.add(groupBeans.get(i).getGroupTitle());

            List<ItemGroupBean.GroupBean.GroupItemsBean> groupItemsBeans =
                    groupBeans.get(i).getGroupItems();
            for (int i1 = 0; i1 < groupItemsBeans.size(); i1++) {
                count++;

                // 存储 选项数据到内存
                mPositionDatas.add(groupItemsBeans.get(i1));
            }

            //  遍历到最后需要记录第一组最后的一个索引
            if (i == 0) {
                firstGroupEndIndex = count - 1;
            }
        }

        // 默认是正常的状态，此时是需要隐藏第一组的选项
        mGroupState = GroupState.GROUP_STATE_NORMAL;
        removeFirstGroupItem();
    }

    /**
     * 将 list 转化成分组的数据模型
     *
     * @return
     */
    private ItemGroupBean listToDataModel() {
        ItemGroupBean itemGroupBean = new ItemGroupBean();
        List<ItemGroupBean.GroupBean> groupBeans = new ArrayList<>();
        ItemGroupBean.GroupBean groupBean = null;
        List<ItemGroupBean.GroupBean.GroupItemsBean> groupItemsBeans = null;

        int groupCount = 0;
        int currentGroupCount = 0;

        for (int i = 0; i < mPositionDatas.size(); i++) {

            Object objectData = mPositionDatas.get(i);

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
     * 获取组的最后一个选项的 list 索引
     *
     * @param groupIndex
     * @return
     */
    private int getGroupLastItemIndex(int groupIndex) {
        int lastIndex = 0;
        int groupCount = 0;

        for (int i = 0; i < mPositionDatas.size(); i++) {

            Object objectData = mPositionDatas.get(i);

            if (objectData instanceof String) {
                groupCount++;
                if (groupCount == groupIndex + 2) {
                    lastIndex = i - 1;
                    return lastIndex;
                }
            }
        }
        lastIndex = mPositionDatas.size() - 1;
        return lastIndex;
    }

    /**
     * 获取组的 title
     *
     * @param position list 索引
     * @return
     */
    private String getGroupTitle(int position) {
        String title = "";

        Object objectData = getItemData(position);
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
        Object objectData = getItemData(position);
        ItemGroupBean.GroupBean.GroupItemsBean groupItemBean = null;
        if (objectData instanceof ItemGroupBean.GroupBean.GroupItemsBean) {
            groupItemBean = (ItemGroupBean.GroupBean.GroupItemsBean) objectData;
        }

        return groupItemBean;
    }

    /**
     * 获取选项数据
     *
     * @param position
     * @return
     */
    private Object getItemData(int position) {
        Object objectData = mGroupState == GroupState.GROUP_STATE_NORMAL ?
                mNormalPositionDatas.get(position) : mPositionDatas.get(position);
        return objectData;
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
                    // 进行配置数据持久化
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

        if (mOnModeChangedListener != null) {
            mOnModeChangedListener.onChanged(isEditMode);
        }
        return isEditMode;
    }

    @Override
    public int getItemViewType(int position) {
        if (mGroupState == GroupState.GROUP_STATE_NORMAL) {
            if (position == 0) {
                return TYPE_HEADER_NOR; // 正常状态（隐藏第一组选项）时的 Title
            } else if (mNormalPositionDatas.get(position) instanceof String) { //  说明是 title 类型
                return TYPE_HEADER;
            } else {
                return TYPE_GROUP;
            }
        } else {
            if (mPositionDatas.get(position) instanceof String) { //  说明是 title 类型
                return TYPE_HEADER;
            } else if (position > 0 && position <= firstGroupEndIndex) {
                return TYPE_FIRST_GROUP;
            } else {
                return TYPE_GROUP;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final View view;
        switch (viewType) {
            case TYPE_HEADER_NOR:
                view = mInflater.inflate(R.layout.yt_item_group_nor_header_layout, parent, false);
                return new NorHeaderViewHolder(view);
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
                                if (groupItemBean.getBadgeState() == ITEM_STATE_DELETABLE) {
                                    moveMyToOther(groupHolder);
                                } else if (groupItemBean.getBadgeState() == ITEM_STATE_ADDIBLE) {
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
                            // 非产出 item 是不可以拖动的，比如 定制 按钮
                            if (groupItemBean != null &&
                                    groupItemBean.getBadgeState() == ITEM_STATE_DELETABLE) {
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
                                    groupItemBean.getBadgeState() != ITEM_STATE_DELETABLE) {
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
        if (holder instanceof NorHeaderViewHolder) {
            // 非编辑正常状态的头部
            NorHeaderViewHolder headerHolder = (NorHeaderViewHolder) holder;
            headerHolder.tvTitle.setText(getGroupTitle(position));

        } else if (holder instanceof HeaderViewHolder) {
            // 头部
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvTitle.setText(getGroupTitle(position));

        } else if (holder instanceof GroupViewHolder) {
            // 组
            final GroupViewHolder groupHolder = (GroupViewHolder) holder;
            ItemGroupBean.GroupBean.GroupItemsBean groupItemBean = getGroupItemBean(position);
            if (groupItemBean != null) {

                // 比如定制按钮的处理
                if (groupItemBean.getBadgeState() == ITEM_STATE_NORMAL) {
                    groupHolder.itemParent.setBackgroundResource(R.drawable.yt_item_dingzhi_bg);
                    groupHolder.ivIcon.setVisibility(View.INVISIBLE);
                    groupHolder.textView.setVisibility(View.INVISIBLE);
                } else {
                    groupHolder.itemParent.setBackgroundColor(0xfff9f9f9);
                    groupHolder.ivIcon.setVisibility(View.VISIBLE);
                    groupHolder.textView.setVisibility(View.VISIBLE);
                }
                groupHolder.textView.setText(groupItemBean.getTitle());
                if (groupHolder.ivIcon.getVisibility() == View.VISIBLE) {
                    Glide.with(mContext)
                            .load(groupItemBean.getIconUrl())
                            .skipMemoryCache(true)
                            .centerCrop()
                            .into(groupHolder.ivIcon);
                }
            }

            //  处理选项右上角的的状态指示按钮
            if (isEditMode) {
                groupHolder.imgEdit.setVisibility(View.VISIBLE);

                // 正常状态则隐藏，比如 定制 按钮
                if (groupItemBean.getBadgeState() == ITEM_STATE_NORMAL) {
                    groupHolder.imgEdit.setVisibility(View.INVISIBLE);

                } else if (groupItemBean.getBadgeState() == ITEM_STATE_DELETABLE) {
                    groupHolder.imgEdit.setImageResource(R.drawable.yt_item_delete_btn);
                } else if (groupItemBean.getBadgeState() == ITEM_STATE_ADDIBLE) {
                    groupHolder.imgEdit.setImageResource(R.drawable.yt_item_add_btn);
                } else if (groupItemBean.getBadgeState() == ITEM_STATE_SELECTED) {
                    groupHolder.imgEdit.setImageResource(R.drawable.yt_item_selected_btn);
                }
            } else {
                groupHolder.imgEdit.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mGroupState == GroupState.GROUP_STATE_NORMAL) {
            return mNormalPositionDatas.size();
        }
        return mPositionDatas.size();
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
                for (int i = firstGroupEndIndex; i < mPositionDatas.size(); i++) {
                    ItemGroupBean.GroupBean.GroupItemsBean groupItemBean1 = getGroupItemBean(i);
                    if (groupItemBean1 != null && groupItemBean1.getUniqueId().equals(srcUniqueId)) {
                        groupItemBean1.setBadgeState(ITEM_STATE_ADDIBLE);
                    }
                }
                // 删除第一组中的选项
                firstGroupEndIndex -= 1;
                mPositionDatas.remove(position);
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
        groupItemBean1.setBadgeState(ITEM_STATE_DELETABLE);
        //  新增加后的选项索引
        mPositionDatas.add(firstGroupEndIndex, groupItemBean1);
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
                        int listSize = mPositionDatas.size();
                        for (int i = 0; i < listSize; i++) {
                            ItemGroupBean.GroupBean.GroupItemsBean item
                                    = getGroupItemBean(i);
                            if (item != null &&
                                    groupItemBean.getUniqueId().equals(item.getUniqueId())
                                    && item.getBadgeState() != ITEM_STATE_DELETABLE) {
                                item.setBadgeState(ITEM_STATE_SELECTED);
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
            if (groupItemBean.getBadgeState() == ITEM_STATE_DELETABLE) { // 只有是第一组可删除的才允许拖动
                notifyItemMoved(fromPosition, toPosition);

                // 更新内存中的顺序
                ItemGroupBean.GroupBean.GroupItemsBean groupItemBean1
                        = getGroupItemBean(fromPosition);
                if (groupItemBean1 != null) {
                    mPositionDatas.remove(fromPosition);
                    mPositionDatas.add(toPosition, groupItemBean1);
                }
            }
        }
    }

    /**
     * 开启编辑模式
     */
    public void startEditMode() {
        isEditMode = true;
        // 编辑状态界面
        mGroupState = GroupState.GROUP_STATE_EDIT;
        notifyDataSetChanged();
    }

    /**
     * 完成编辑模式
     */
    public void cancelEditMode() {
        isEditMode = false;
        mGroupState = GroupState.GROUP_STATE_NORMAL;
        removeFirstGroupItem();
        notifyDataSetChanged();
    }

    /**
     * 移除第一组选项的数据
     */
    private void removeFirstGroupItem() {
        // 隐藏第一组的应用
        mNormalPositionDatas = (ArrayList<Object>) mPositionDatas.clone();
        for (int i = 0; i < firstGroupEndIndex; i++) {
            mNormalPositionDatas.remove(1);
        }
    }

    public interface OnItemClickListener {
        /**
         * 点击事件响应回调
         *
         * @param v
         * @param item
         */
        void onItemClick(View v, ItemGroupBean.GroupBean.GroupItemsBean item);
    }

    public interface OnModeChangedListener {
        /**
         * 编辑模式改变监听
         *
         * @param isEditMode
         */
        void onChanged(boolean isEditMode);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnModeChangedListener(OnModeChangedListener onModeChangedListener) {
        mOnModeChangedListener = onModeChangedListener;
    }

    /**
     * 我的频道
     */
    class GroupViewHolder extends RecyclerView.ViewHolder implements OnDragVHListener {
        private RelativeLayout itemParent;
        private ImageView ivIcon;
        private TextView textView;
        private ImageView imgEdit;

        public GroupViewHolder(View itemView) {
            super(itemView);
            itemParent = (RelativeLayout) itemView.findViewById(R.id.item_parent);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
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
     * 标题部分
     */
    class HeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tv);
        }
    }

    /**
     * 正常状态下的 标题部分
     */
    class NorHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvEditBtn;

        public NorHeaderViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tv);
            tvEditBtn = (TextView) itemView.findViewById(R.id.tv_edit_btn);
            tvEditBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chanageEditMode();
                }
            });
        }
    }
}
