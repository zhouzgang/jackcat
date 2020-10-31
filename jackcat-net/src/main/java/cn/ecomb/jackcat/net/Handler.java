package cn.ecomb.jackcat.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
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

        Processor processor = connections.get(channel);
        if (processor == null) {
            processor = createProcessor();
            connections.put(channel, processor);
            logger.debug("为通道 {} 创建处理器 {}", channel, processor);
        }

        SocketState state = processor.process(channel);
        switch (state) {
            case OPEN:
                logger.debug("保持连接，通道 {} 重新声明读取事件", channel);
                connections.remove(channel);
                channel.getPoller().register(channel, SelectionKey.OP_READ);
                break;
            case LONG:
                logger.debug("请求数据没准备好，通道 {} 重新注册读取事件", channel);
                channel.getPoller().register(channel, SelectionKey.OP_READ);
                break;
            case WRITE:
                logger.debug("写数据，通道 {} 注册写入事件", channel);
                channel.getPoller().register(channel, SelectionKey.OP_WRITE);
                break;
            case CLOSED:
                connections.remove(channel);
                break;
            default:
                connections.remove(channel);
        }

        return state;
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
