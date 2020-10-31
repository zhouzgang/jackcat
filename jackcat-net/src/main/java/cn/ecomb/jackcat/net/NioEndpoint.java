package cn.ecomb.jackcat.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Endpoint：终端，作为socket的服务连接端。
 * 有初始化，启动，停止的生命周期
 * <p>
 * 启动后，启动不同的线程池提供以下服务：
 * Socket acceptor thread
 * Socket poller thread
 * Worker threads pool
 *
 * @author zhouzhigang
 * @date 2019-10-12.
 */
public class NioEndpoint {

    private Logger logger = LoggerFactory.getLogger(NioEndpoint.class);

    private volatile boolean running = false;

    private ServerSocketChannel serverSocket;
    private int port = 8089;
    /** 超时时间 */
    private int soTimeout = 60000;

    /** SelectionKey.OP_ACCEPT */
    private int acceptCount = 100;

    /** ExecutorService 与 Executor 之间的区别 */
    private ExecutorService executor;
    private int maxThreads = 5;

    /** 信号量的作用是什么，注册 Channel 的安全保证 */
    private Semaphore connectionLimit;
    private int maxConnections = 2;

    private Poller poller;
    private Acceptor acceptor;
    private Handler handler;

    /**
     * 初始化Endpoint
     */
    public void init() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(port), acceptCount);
        serverSocket.configureBlocking(true);
        serverSocket.socket().setSoTimeout(soTimeout);

        connectionLimit = new Semaphore(maxConnections);
    }

    /**
     * 启动服务
     */
    public void start() throws IOException{
        running = true;
        // 初始化线程池
        executor = Executors.newFixedThreadPool(maxThreads, new ThreadFactory() {
            // 为什么这里要用final的？
            final AtomicInteger threadNumber = new AtomicInteger(1);
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "pool-thread-" + threadNumber);
                return thread;
            }
        });
        // 启动 Poller 线程
        poller = new Poller(this);
        Thread pollerThread = new Thread(poller, "poller");
        pollerThread.start();
        // 启动 Acceptor 线程
        acceptor = new Acceptor(this);
        Thread acceptorThread = new Thread(acceptor, "acceptor");
        acceptorThread.start();

        logger.info("容器启动，监听端口：", port);
    }

    /**
     * 关闭服务
     */
    public void stop() {
        running = false;
        poller.destroy();
        executor.shutdownNow();
    }

    /**
     * 申请一个连接名额
     */
    public void acquireConnSemaphore() throws InterruptedException{
        if (maxConnections == -1) {
            return;
        }
        connectionLimit.acquire();
    }

    /**
     * 释放一个连接名额
     */
    public void releaseConnSemaphore() {
        if (maxConnections == -1) {
            return;
        }
        connectionLimit.release();
    }

    public SocketChannel accept() throws IOException {
        return serverSocket.accept();
    }

    public boolean isRunning() {
        return running;
    }

    public NioEndpoint setRunning(boolean running) {
        this.running = running;
        return this;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public NioEndpoint setSoTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    public int getAcceptCount() {
        return acceptCount;
    }

    public NioEndpoint setAcceptCount(int acceptCount) {
        this.acceptCount = acceptCount;
        return this;
    }

    public Poller getPoller() {
        return poller;
    }

    public NioEndpoint setPoller(Poller poller) {
        this.poller = poller;
        return this;
    }

    public Acceptor getAcceptor() {
        return acceptor;
    }

    public NioEndpoint setAcceptor(Acceptor acceptor) {
        this.acceptor = acceptor;
        return this;
    }

    public Handler getHandler() {
        return handler;
    }

    public NioEndpoint setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
