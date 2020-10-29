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
    private ConcurrentLinkedQueue<NioChannel> eventQueue = new ConcurrentLinkedQueue<>();

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
            boolean hasEvents = false;
            try {
                if (!close) {
                    // 判断是否有准备好的事件
                    hasEvents = hasEvents();
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

            // todo 为什么这里要在检查一次
            if (keyCount == 0) {
                hasEvents = (hasEvents | hasEvents());
            }

            if (keyCount > 0) {
                for (SelectionKey key : selector.selectedKeys()) {
                    final NioChannel channel = (NioChannel) key.attachment();
                    if (channel != null && (key.isReadable() || key.isWritable())) {
                        logger.debug("通道 {} 发生 {} IO 事件，从关注事件中移除已就绪的事件",
                                channel, key.isReadable() ? "读" : "写");

                        // todo 搞明白这一段在做什么？有点想计算可读字节长度
                        int interestOps = key.interestOps() & (~key.readyOps());
                        key.interestOps(interestOps);
                        channel.interestOps(interestOps);

                        // todo 没太明白这里的两个操作是在干嘛
                        if (channel.getWritLatch() != null) {
                            logger.debug("模拟阻塞「写」，通道可写: {}", channel);
                            channel.getWritLatch().countDown();
                            continue;
                        }

                        if (channel.getReadLatch() != null) {
                            logger.debug("模拟阻塞「读」，通道可读: {}", channel);
                            channel.getReadLatch().countDown();
                            continue;
                        }

                        logger.debug("提交通道 {} 到线程池，处理 {} 事件", channel, key.isReadable() ? "读" : "写");

                        // todo 这里需要处理线程池拒绝策略，应该使用自定义线程池
                        endpoint.getExecutor().execute(() -> {
                            Handler.SocketState socketState = endpoint.getHandler().handle(channel);
                            if (socketState == Handler.SocketState.CLOSED) {
                                logger.debug("关闭通道 {}", channel);
                                cancelledKey(key);
                            }
                        });

                    }
                }
            }

            // todo 为什么这里还要做超时处理
            timeout();
        }
    }

    /**
     * 处理超时
     * todo 这里应该不叫 timeout
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
        eventQueue.offer(channel);
        // 使尚未返回的第一个选择操作立即返回。
        selector.wakeup();
    }

    /**
     * 判断是否有事件
     * @return
     */
    public boolean hasEvents() {
        boolean hasEvents = false;
        NioChannel channel;
        while ((channel = eventQueue.poll()) != null) {
            hasEvents = true;
            SocketChannel sc = channel.getSocketChannel();
            int eventOps = channel.interestOps();
            if (eventOps == Acceptor.OP_REGISTER) {
                try {
                    logger.debug("注册新通道 [{}]", channel);
                    sc.register(selector, SelectionKey.OP_READ, channel);
                } catch (ClosedChannelException e) {
                    logger.debug("注册新通道 [{}] 失败，错误信息：{}", channel, e.getMessage());
                }
            } else if (eventOps == SelectionKey.OP_READ || eventOps == SelectionKey.OP_WRITE) {
                // todo keyFor 函数的作用？
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

    /**
     * 关闭管道
     * @param key
     */
    public void cancelledKey(SelectionKey key) {
        // todo attach() 函数的作用？
        NioChannel channel = (NioChannel) key.attach(null);
        if (channel != null) {
            endpoint.getHandler().release(channel);
            if (key.isValid()) {
                key.cancel();
            }
            logger.debug("关闭通道 {} 连接", channel);
            try {
                channel.getSocketChannel().close();
            } catch (IOException e) {
                logger.error("释放管道失败，{}", e.getMessage());
            }
            endpoint.releaseConnSemaphore();
        }
    }

    public Selector getSelector() {
        return selector;
    }
}
