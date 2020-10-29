package cn.ecomb.jackcat.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.channels.SocketChannel;

/**
 * 接收socket，并将 SocketChannel 注册到 Poller 的队列中。
 * 在 Poller 线程中循环处理队列中的请求
 *
 * @author zhouzg
 * @date 2019-10-13.
 */
public class Acceptor implements Runnable {

    private Logger logger = LoggerFactory.getLogger(Acceptor.class);

    /** 为什么有这个自定义通道注册？ */
    public static final int OP_REGISTER = 0x100;

    private NioEndpoint endpoint;

    public Acceptor(NioEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void run() {
        while (endpoint.isRunning()) {
            try {
                // 申请一个连接名额
                endpoint.acquireConnSemaphore();
                SocketChannel socket = endpoint.accept();
                try {
                    socket.configureBlocking(false);
                    socket.socket().setTcpNoDelay(true);
                    socket.socket().setSoTimeout(endpoint.getSoTimeout());
                    NioChannel channel = new NioChannel(socket);
                    endpoint.getPoller().register(channel, OP_REGISTER);
                    logger.info("接收通道 [{}] 连接", channel);
                } catch (Throwable t) {
                    logger.error("", t);
                    try {
                        endpoint.releaseConnSemaphore();
                        socket.socket().close();
                        socket.close();
                    } catch (IOException e) {
                        logger.error("关闭连接异常：{}", e.getMessage());
                    }
                }
            } catch (SocketTimeoutException ste) {
                logger.error(ste.getMessage());
            } catch (Throwable t) {
                logger.error("accept 异常：{}", t.getMessage());
            }
        }
    }
}
