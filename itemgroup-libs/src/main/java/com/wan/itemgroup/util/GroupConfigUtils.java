package com.wan.itemgroup.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.yingt.common.util.FileSystem;
import com.wan.itemgroup.cache.DataCache;
import com.wan.itemgroup.model.ItemGroupBean;
import com.yingt.uimain.util.Res;

/**
 * @author laihuan.wan
 *         Created by laihuan.wan on 2017/10/18 0018.
 */

public class GroupConfigUtils {

    /**
     * 获取分组的数据模型数据
     *
     * @return
     */
    public<T> T getItemGroupBean(Class<T> classOfT) {
        T itemGroupBean = null;
        String shortcutConfig;
        String jsonConfig = DataCache.getGroupConfig();
        if (!TextUtils.isEmpty(jsonConfig)) {
            shortcutConfig = jsonConfig;
        } else {
            shortcutConfig = FileSystem.getFromAssets(Res.getContext(), "itemGroup/groupConfig");
        }

        Gson gson = new Gson();
        // 解析出快捷按钮
        if (shortcutConfig != null) {
            itemGroupBean = gson.fromJson(shortcutConfig, classOfT);
        }
        if(itemGroupBean == null){
            shortcutConfig = FileSystem.getFromAssets(Res.getContext(), "itemGroup/groupConfig");
            itemGroupBean = gson.fromJson(shortcutConfig, classOfT);
        }
        return itemGroupBean;
    }

}
