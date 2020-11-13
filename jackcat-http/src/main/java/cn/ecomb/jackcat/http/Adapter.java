package cn.ecomb.jackcat.http;

import java.io.IOException;

/**
 * 容器适配器，连接 Endpoint 和 Container
 *
 * @author zhouzg
 * @date 2020-10-29.
 */
public interface Adapter {

    /**
     * 处理请求，响应结果
     *
     * @param jackRequest 请求封装
     * @param jackResponse 响应结果
     */
    void service(JackRequest jackRequest, JackResponse jackResponse) throws IOException;
}
