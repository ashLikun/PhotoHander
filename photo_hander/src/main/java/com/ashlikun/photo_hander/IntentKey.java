package com.ashlikun.photo_hander;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/16　13:12
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：意图的Key
 */
interface IntentKey {
    /**
     * 选择的结果
     */
    String EXTRA_RESULT = "select_result";
    /**
     * 已选的数据
     */
    String EXTRA_DEFAULT_SELECTED_LIST = "default_list";
    /**
     * 已选的数据
     */
    String EXTRA_DEFAULT_ADD_IMAGES = "add_list_images";
    /**
     * 列表正在显示的数据
     */
    String EXTRA_ADAPTER_SHOW_DATA = "adapter_show_data";
    /**
     * 列表点击查看大图的对应position
     */
    String EXTRA_ADAPTER_CLICK_POSITION = "adapter_click_position";
    /**
     * 列表点击查看大图的对应position的data
     */
    String EXTRA_ADAPTER_CLICK_DATA = "adapter_click_data";
}
