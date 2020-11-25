package cn.ecomb.jackcat.http;


import cn.ecomb.jackcat.net.Handler;
import cn.ecomb.jackcat.net.NioEndpoint;
import cn.ecomb.jackcat.net.Processor;

import java.io.IOException;

/**
 * @author brian.zhou
 * @date 2020/11/25
 */
public class TestHttpProcessor {

	public static void main(String[] args) throws IOException {
		NioEndpoint endpoint = new NioEndpoint();
		endpoint.setHandler(new Handler() {
			@Override
			public Processor createProcessor() {
				HttpProcessor processor = new HttpProcessor();
				processor.setAdapter(new AdapterImplTest());
				return processor;
			}
		});

		endpoint.init();
		endpoint.start();
	}
}