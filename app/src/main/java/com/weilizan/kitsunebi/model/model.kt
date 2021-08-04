package com.weilizan.kitsunebi.model

import com.google.gson.annotations.SerializedName


data class VmessURL(
    //address 地址IP或域名
    @SerializedName("add")
    val address: String,
    //aid: alterId
    @SerializedName("aid")
    val alterId: String,
    //host: 伪装的域名
    val host: String,
    //id: UUID
    @SerializedName("id")
    val uuid: String,
    //net: 传输协议(tcp\kcp\ws\h2\quic)
    val net: String,
//    path: path
    val path: String,
    //port: 端口号
    val port: String,
    //ps: 备注或别名
    var ps: String,
    //scy: 加密方式(security),没有时值默认auto
    @SerializedName("scy")
    val security: String,
//    sni: 服务器域名
    val sni: String,
//    tls: 底层传输安全(tls)
    val tls: String,
    //type: 伪装类型(none\http\srtp\utp\wechat-video) *tcp or kcp or QUIC
    val type: String,
    //v: 配置文件版本号,主要用来识别当前配置
    @SerializedName("v")
    val version: String
)

