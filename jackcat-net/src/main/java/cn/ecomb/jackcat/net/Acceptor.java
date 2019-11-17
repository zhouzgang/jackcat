package cn.ecomb.jackcat.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * 接收socket，并将 SocketChannel 注册到 Poller 的队列中。
 * 在 Poller 线程中循环处理队列中的请求
 *
 * @author zhouzg
 * @date 2019-10-13.
 */
public class Acceptor implements Runnable{

    private Logger logger = LoggerFactory.getLogger(Acceptor.class);

    public static final int OP_REGISTER = 0x100;
    private NioEndpoint endpoint;

    public Acceptor(NioEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    public void run() {
        while (endpoint.isRunning()) {
            try {
                endpoint.acquire();
                SocketChannel socket = endpoint.accept();

                socket.configureBlocking(false);
                socket.socket().setTcpNoDelay(true);
                socket.socket().setSoTimeout(endpoint.getSoTimeout());
                NioChannel channel = new NioChannel();
                endpoint.getPoller().
            } catch (Exception e) {

            }
        }
    }
}
