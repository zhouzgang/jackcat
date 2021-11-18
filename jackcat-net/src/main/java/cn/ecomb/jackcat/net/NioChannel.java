package cn.ecomb.jackcat.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;

/**
 * 封装 SocketChannel，实现对 Socket 流的读写和刷新。
 *
 * @author zhouzg
 * @date 2019-10-13.
 */
public class NioChannel {

    private Logger logger = LoggerFactory.getLogger(NioChannel.class);

    private SocketChannel socketChannel;
    private ByteBuffer readBuff;
    private ByteBuffer writeBuff;

    private Poller poller;
    private int interestOps = 0;

    private long timeOut = 10000;
    private long lastAccessTime = -1;

    private CountDownLatch writLatch;
    private CountDownLatch readLatch;

    public NioChannel(SocketChannel socket) {
        this.socketChannel = socket;
        readBuff = ByteBuffer.allocate(2 * 8192);
        writeBuff = ByteBuffer.allocate(2 * 8192);
        lastAccessTime = System.currentTimeMillis();
    }

    /**
     * 从底层通道读取数据
     *
     * @param dst 目标缓冲区
     * @param block
     * @return
     * @throws IOException
     */
    public int read(ByteBuffer dst, boolean block) throws IOException {
        int n = 0;
        block = false;
        if (block) {
            logger.debug("模拟阻塞从 {} 读取", this);

        } else {
            n = socketChannel.read(readBuff);
            logger.debug("从通道 [{}] 非阻塞读取 [{}] 字节", this, n);
        }
        return n;
    }

    /**
     * 阻塞把响应体数据发送到客户端，重置缓冲区，以便再次写入
     */
    public void flush() throws IOException {
        writeBuff.flip();
        // todo 这里应该还没写完
        if (writeBuff.remaining() > 0) {
            logger.debug("模拟阻塞写入 - 将响应体 [{}B] 数据写入通道 [{}]", writeBuff.remaining(), this);
        }
        while (writeBuff.hasRemaining()) {
            int n = socketChannel.write(writeBuff);
            if (n == -1) {
                throw new EOFException();
            }
            if (n > 0) {
                logger.debug("阻塞写入 {}B 字节", n);
                continue;
            }

            // todo what is here?
            writLatch = new CountDownLatch(1);
            poller.register(this, SelectionKey.OP_WRITE);
            logger.debug("等待阻塞通道 {} 写数据", this);
            try {
                writLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            writLatch = null;
        }
        writeBuff.clear();


    }

    public CountDownLatch getWritLatch() {
        return writLatch;
    }

    public CountDownLatch getReadLatch() {
        return readLatch;
    }

    public int read(ByteBuffer dst) throws IOException {
        return socketChannel.read(dst);
    }

    public int write(ByteBuffer src) throws IOException {
        return socketChannel.write(src);
    }

    @Override
    public String toString() {
        String ret = null;
        try {
            ret = socketChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            ret = super.toString();
        }
        return ret;
    }

    public Poller getPoller() {
        return poller;
    }

    public void setPoller(Poller poller) {
        this.poller = poller;
    }

    public int interestOps() {
        return interestOps;
    }

    public void interestOps(int interestOps) {
        this.interestOps = interestOps;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    public ByteBuffer getReadBuff() {
        return readBuff;
    }

    public ByteBuffer getWriteBuff() {
        return writeBuff;
    }
}
