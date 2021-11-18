package cn.ecomb.jackcat.http;


import cn.ecomb.jackcat.net.Handler;
import cn.ecomb.jackcat.net.NioEndpoint;
import cn.ecomb.jackcat.net.Processor;

import java.io.IOException;

/**
 * 使用 curl 工具进行测试：
 * curl  http://127.0.0.1:8089/index?a=2 -X POST -d "user=abc&passwd=创" -v
 * curl  http://127.0.0.1:8089 -X POST -H "Transfer-Encoding:chunked" -d "user=abc&passwd=创" -v
 * curl  http://127.0.0.1:8089 -F "" -v
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