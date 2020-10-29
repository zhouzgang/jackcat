package cn.ecomb.jackcat.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 封装 Selector ， 处理读写事件的通知，同时处理超时通道
 *
 * @author zhouzg
 * @date 2019-10-13.
 */
public class Poller implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Poller.class);

    private NioEndpoint endpoint;

    /** 多路复用器 */
    private Selector selector;

    /** 停止线程的标记 */
    private boolean close = false;

    private long nextExpiration = 0;

    /** 为什么要在这里加一个队列，而且要是并发安全的 */
    private ConcurrentLinkedQueue<NioChannel> events = new ConcurrentLinkedQueue<>();

    public Poller(NioEndpoint endpoint) throws IOException {
        this.endpoint = endpoint;
        selector = Selector.open();
    }

    /**
     * 循环检查是否有事件是否可读
     * 获取Channel
     * 处理超时问题
     */
    @Override
    public void run() {
        int keyCount = 0;
        while (true) {
            boolean hasEvens = false;
            try {
                if (!close) {
                    hasEvens = events();
                    keyCount = selector.select(5000);
                } else {
                    timeout();
                    selector.close();
                    break;
                }
            } catch (IOException e) {
                logger.error("selector select 异常：{}", e.getMessage());
                continue;
            }


        }
    }

    /**
     * 处理超时
     */
    public void timeout() {

    }

    /**
     * 停止Poller线程
     */
    public void destroy() {
        close = true;
        selector.wakeup();
    }

    /**
     * 将队列注册到协作队列中
     */
    public void register(NioChannel channel, int interestOps) {
        channel.setPoller(this);
        channel.interestOps(interestOps);
        events.offer(channel);
        // 使尚未返回的第一个选择操作立即返回。
        selector.wakeup();
    }

    /**
     * 判断是否有事件
     * @return
     */
    public boolean events() {
        boolean hasEvents = false;
        NioChannel channel;
        while ((channel = events.poll()) != null) {
            hasEvents = true;
            SocketChannel sc = channel.ioChannel();
            int eventOps = channel.interestOps();
            if (eventOps == Acceptor.OP_REGISTER) {
                try {
                    logger.debug("注册新通道 [{}]", channel);
                    sc.register(selector, SelectionKey.OP_READ, channel);
                } catch (ClosedChannelException e) {
                    logger.debug("注册新通道 [{}] 失败，错误信息：{}", channel, e.getMessage());
                }
            } else if (eventOps == SelectionKey.OP_READ || eventOps == SelectionKey.OP_WRITE) {
                // keyFor 函数的作用？
                SelectionKey key = sc.keyFor(channel.getPoller().getSelector());
                try {
                    // 这里在做什么？
                    if (key != null) {
                        int ops = key.interestOps() | channel.interestOps();
                        key.interestOps(ops);
                        channel.interestOps(ops);
                    }
                } catch (CancelledKeyException ckx) {
                    cancelledKey(key);
                }
            }
        }
        return hasEvents;
    }

    public void cancelledKey(SelectionKey key) {
        // attach() 函数的作用？
        NioChannel socket = (NioChannel) key.attach(null);
        if (socket != null) {
        //endpoint.getHandler()
        }
    }

    public Selector getSelector() {
        return selector;
    }
}
