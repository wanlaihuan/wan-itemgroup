package com.wan.itemgroup.cache;

import com.yingt.common.util.FileSystem;
import com.yingt.uimain.util.Res;

/**
 * @author laihuan.wan
 * Created by laihuan.wan on 2017/10/16 0016.
 */

public class DataCache {
    private static final String CACHE_ROOT = "/kds";
    private static final String CACHE_EDIT_CONFIG = "/shortcut/groupShortcutConfig";

    /**
     * 保存定制界面的 json 配置数据
     */
    public static void saveGroupConfig(String newJsonConfig) {
        FileSystem.saveTextToFile(newJsonConfig,
                FileSystem.getDataCacheFile(Res.mContext, CACHE_ROOT + CACHE_EDIT_CONFIG).getAbsolutePath());
    }

    /**
     * 获取组的配置
     * @return
     */
    public static String getGroupConfig() {
        String jsonConfig = FileSystem.readFromFile(Res.mContext,
                FileSystem.getDataCacheFile(Res.mContext, CACHE_ROOT + CACHE_EDIT_CONFIG).getAbsolutePath());
        return jsonConfig;
    }

}
