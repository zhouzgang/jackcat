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
     */
    public enum SocketState {
        /** 长连接 */
        OPEN,
        /** 继续读取 */
        LONG,
        WRITE,
        CLOSED;
    }

}
