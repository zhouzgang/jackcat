package cn.ecomb.jackcat.net;

/**
 * 请求处理器
 * 适配不同协议
 *
 * @author zhouzg
 * @date 2019-10-13.
 */
public interface Processor {

    /**
     * 处理请求
     * @param channel 请求通道
     */
    void process(NioChannel channel);
}
