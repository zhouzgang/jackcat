package cn.ecomb.jackcat.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    private SocketChannel sChannel;
    private ByteBuffer readBuff;
    private ByteBuffer writeBuff;

    private Poller poller;
    private int interestOps = 0;

    private long timeOut = 100000;
    private long lastAccess = -1;

    private CountDownLatch writLatch;
    private CountDownLatch readLatch;

    public NioChannel(SocketChannel socket) {
        this.sChannel = socket;
        readBuff = ByteBuffer.allocate(2 * 8192);
        writeBuff = ByteBuffer.allocate(2 * 8192);
        lastAccess = System.currentTimeMillis();
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

        } else {
            n = sChannel.read(readBuff);
            logger.debug("从通道 [{}] 非阻塞读取 [{}] 字节", this, n);
        }
        return n;
    }

    /**
     * 阻塞把响应体数据发送到客户端，重置缓冲区，以便再次写入
     */
    public void flush() {
        writeBuff.flip();

    }


    public int read(ByteBuffer dst) throws IOException {
        return sChannel.read(dst);
    }

    public int write(ByteBuffer src) throws IOException {
        return sChannel.write(src);
    }

    @Override
    public String toString() {
        String ret = null;
        try {
            ret = sChannel.getRemoteAddress().toString();
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

    public SocketChannel ioChannel() {
        return sChannel;
    }
}
