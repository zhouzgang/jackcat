package cn.ecomb.jackcat.http;

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
     * @param request 请求封装
     * @param response 响应结果
     */
    void service(JackRequest request, JackResponse response);
}
