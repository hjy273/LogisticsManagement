package com.clb.logisticsmanagement.ble.conn;

//接口定义常量，创建枚举类型需要用到 enum关键词
//枚举类型的每一个值都将映射到 protected Enum(String name, int ordinal) 构造函数中，在这里，每个值的名称都被转换成一个字符串，并且序数设置表示了此设置被创建的顺序。
//使用枚举通常会比使用静态常量要消耗两倍以上的内存，在Android开发当中我们应当尽可能地不使用枚举。

/**
 * 这段代码实际上调用了3次 Enum(String name, int ordinal)：
 * new Enum<ConnResult>("CONN_SUCCESS",0);
 *  new Enum<ConnResult>("CONN_DIS",1);
 *  new Enum<ConnResult>("CONN_FAILURE",2);
* */

public enum ConnResult {


    /**
     * 连接上
     */
    CONN_SUCCESS,
    /**
     * 断开连接
     */
    CONN_DIS,
    /**
     * 连接失败
     */
    CONN_FAILURE;


}
