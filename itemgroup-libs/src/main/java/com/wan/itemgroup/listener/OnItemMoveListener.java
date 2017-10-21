package com.wan.itemgroup.listener;

/**
 * @author laihuan.wan
 * Item移动后 触发
 * Created by laihuan.wan on 17/10/15.
 */
public interface OnItemMoveListener {
    /**
     * 选项移动回调
     * @param fromPosition
     * @param toPosition
     */
    void onItemMove(int fromPosition, int toPosition);
}
