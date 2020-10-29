package cn.ecomb.jackcat.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通道与处理器的映射
 *
 * @author zhouzg
 * @date 2019-10-13.
 */
public abstract class Handler {

    private static final Logger logger = LoggerFactory.getLogger(Handler.class);

    /**
     * 连接与处理器的映射，
     */
    private Map<NioChannel, Processor> connections = new ConcurrentHashMap<>();

    /**
     * 连接处理过程中的 Socket 可能的状态
     * todo 为什么 socket 的状态要定义在这里
     */
    public enum SocketState {
        /** 长连接 */
        OPEN,
        /** 继续读取 */
        LONG,
        /** 发送 */
        WRITE,
        /** 关闭 */
        CLOSED;
    }

    /**
     * 处理请求连接
     *
     * @param channel 请求的通道
     * @return
     */
    public SocketState handle(NioChannel channel) {
        /*
        创建请求
        处理请求
         */
        return SocketState.LONG;
    }

    /**
     * 释放连接
     * @param channel 请求通道
     */
    public void release(NioChannel channel) {
        Processor processor = connections.remove(channel);
        if (processor != null) {
            logger.debug("释放管道 [{}] 关联的 Processor [{}]", channel, processor);
        }
    }

    /**
     * 创建请求处理器
     * @return
     */
    public abstract Processor createProcessor();

}
