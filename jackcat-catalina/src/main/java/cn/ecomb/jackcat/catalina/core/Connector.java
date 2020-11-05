package cn.ecomb.jackcat.catalina.core;

import cn.ecomb.jackcat.http.Adapter;
import cn.ecomb.jackcat.http.HttpProcessor;
import cn.ecomb.jackcat.net.Handler;
import cn.ecomb.jackcat.net.NioEndpoint;
import cn.ecomb.jackcat.net.Processor;

import java.io.IOException;

/**
 * 连接器，提供 xml 配置功能，端口，超时时间等
 *
 * @author zhouzg
 * @date 2020-10-29.
 */
public class Connector {

    private Context context;

    private int prot = 8088;

    /** 端点 */
    private NioEndpoint endpoint;

    /** 适配器 */
    private Adapter adapter;

    public Connector() {
        adapter = new AdapterImpl(this);
        endpoint = new NioEndpoint();
        endpoint.setHandler(new Handler() {
            @Override
            public Processor createProcessor() {
                HttpProcessor processor = new HttpProcessor();
                processor.setAdapter(adapter);
                return processor;
            }
        });
    }

    public void start() throws IOException {
        endpoint.init();
        endpoint.start();
    }

    public void stop() {
        endpoint.stop();
    }

    public int getProt() {
        return prot;
    }

    public Connector setProt(int port) {
        this.prot = port;
        endpoint.setPort(port);
        return this;
    }

    public Connector setContext(Context context) {
        this.context = context;
        return this;
    }

    public Context getContext() {
        return context;
    }
}
