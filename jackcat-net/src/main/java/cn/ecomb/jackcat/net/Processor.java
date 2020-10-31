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
     * 这里使用了什么方式，以至于，只返回了状态，而没有返回处理后的结果，这是一种可以总结学习的地方
     *
     * @param channel 请求通道
     * @return 通道处理后的状态
     */
    Handler.SocketState process(NioChannel channel);
}
