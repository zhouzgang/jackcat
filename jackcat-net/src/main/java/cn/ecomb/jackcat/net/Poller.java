package cn.ecomb.jackcat.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.Selector;
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
     * 循环检查是否有事件
     * 获取Channel
     * 处理超时问题
     */
    @Override
    public void run() {

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
        channel.setInterestOps(interestOps);
        events.offer(channel);
        // 唤醒 selector 的作用是什么?
        selector.wakeup();
    }

    /**
     * 判断是否有事件
     * @return
     */
    public boolean events() {
        return false;
    }

}
