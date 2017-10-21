package com.wan.itemgroup.model;

import java.util.List;

/**
 * @author laihuan.wan
 * Created by laihuan.wan on 2017/10/12 0012.
 * json 格式：可直接用 Gson 解析转成该数据模型
 * {
 "Group": [
 {
 "groupTitle": "首页功能",
 "groupItems": [
 {
 "uniqueId": "101",
 "badgeState": 2,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "自选股"
 },
 {
 "uniqueId": "102",
 "badgeState": 2,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "大盘指数"
 },
 {
 "uniqueId": "103",
 "badgeState": 2,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "板块排行"
 },
 {
 "uniqueId": "104",
 "badgeState": 2,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "沪深排行"
 },
 {
 "uniqueId": "106",
 "badgeState": 2,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "银证转账"
 },
 {
 "uniqueId": "105",
 "badgeState": 2,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "委托交易"
 },
 {
 "uniqueId": "107",
 "badgeState": 2,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "持仓查询"
 }
 ]
 },
 {
 "groupTitle": "可选功能",
 "groupItems": [
 {
 "uniqueId": "1001",
 "badgeState": 1,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "开户"
 },
 {
 "uniqueId": "1002",
 "badgeState": 1,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "业务办理"
 },
 {
 "uniqueId": "1003",
 "badgeState": 1,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "理财商城"
 },
 {
 "uniqueId": "1004",
 "badgeState": 1,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "新股日历"
 },
 {
 "uniqueId": "1005",
 "badgeState": 1,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "预约打新"
 },
 {
 "uniqueId": "1006",
 "badgeState": 1,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "一键打新"
 },
 {
 "uniqueId": "1007",
 "badgeState": 1,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "资讯"
 },
 {
 "uniqueId": "107",
 "badgeState": 3,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "持仓查询"
 },
 {
 "uniqueId": "101",
 "badgeState": 3,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "自选股"
 },
 {
 "uniqueId": "102",
 "badgeState": 3,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "大盘指数"
 },
 {
 "uniqueId": "103",
 "badgeState": 3,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "板块排行"
 },
 {
 "uniqueId": "104",
 "badgeState": 3,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "沪深排行"
 },
 {
 "uniqueId": "106",
 "badgeState": 3,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "银证转账"
 },
 {
 "uniqueId": "105",
 "badgeState": 3,
 "iconUrl": "file:///android_asset/zixungu_btn_normal.png",
 "title": "委托交易"
 }
 ]
 }
 ]
 }
 */

public class ItemGroupBean {

    private List<GroupBean> group;

    public List<GroupBean> getGroup() {
        return group;
    }

    public void setGroup(List<GroupBean> group) {
        this.group = group;
    }

    public static class GroupBean {
        /**
         * groupTitle : 首页功能
         * groupItems : [{"uniqueId":"101","badgeState":2,"iconUrl":"file:///android_asset/zixungu_btn_normal.png","title":"自选股"},{"uniqueId":"102","badgeState":2,"iconUrl":"file:///android_asset/zixungu_btn_normal.png","title":"大盘指数"},{"uniqueId":"103","badgeState":2,"iconUrl":"file:///android_asset/zixungu_btn_normal.png","title":"板块排行"},{"uniqueId":"104","badgeState":2,"iconUrl":"file:///android_asset/zixungu_btn_normal.png","title":"沪深排行"},{"uniqueId":"106","badgeState":2,"iconUrl":"file:///android_asset/zixungu_btn_normal.png","title":"银证转账"},{"uniqueId":"105","badgeState":2,"iconUrl":"file:///android_asset/zixungu_btn_normal.png","title":"委托交易"},{"uniqueId":"107","badgeState":2,"iconUrl":"file:///android_asset/zixungu_btn_normal.png","title":"持仓查询"}]
         */

        private String groupTitle;
        private List<GroupItemsBean> groupItems;

        public String getGroupTitle() {
            return groupTitle;
        }

        public void setGroupTitle(String groupTitle) {
            this.groupTitle = groupTitle;
        }

        public List<GroupItemsBean> getGroupItems() {
            return groupItems;
        }

        public void setGroupItems(List<GroupItemsBean> groupItems) {
            this.groupItems = groupItems;
        }

        public static class GroupItemsBean {
            /**
             * uniqueId : 101
             * badgeState : 2
             * iconUrl : file:///android_asset/zixungu_btn_normal.png
             * title : 自选股
             */

            private String uniqueId;
            private int badgeState;
            private String iconUrl;
            private String title;

            public String getUniqueId() {
                return uniqueId;
            }

            public void setUniqueId(String uniqueId) {
                this.uniqueId = uniqueId;
            }

            public int getBadgeState() {
                return badgeState;
            }

            public void setBadgeState(int badgeState) {
                this.badgeState = badgeState;
            }

            public String getIconUrl() {
                return iconUrl;
            }

            public void setIconUrl(String iconUrl) {
                this.iconUrl = iconUrl;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }
        }
    }
}
