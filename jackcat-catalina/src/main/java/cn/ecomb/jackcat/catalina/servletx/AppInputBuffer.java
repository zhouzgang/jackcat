package cn.ecomb.jackcat.catalina.servletx;

import cn.ecomb.jackcat.http.ActionHook;
import cn.ecomb.jackcat.http.BufferHolder;
import cn.ecomb.jackcat.http.JackRequest;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author zhouzg
 * @date 2020-11-06.
 */
public class AppInputBuffer extends ServletInputStream implements BufferHolder {

	private JackRequest jackRequest;
	private ByteBuffer bodyView;

	@Override
	public void setByteBuffer(ByteBuffer byteBuffer) {
		bodyView = byteBuffer;
	}

	@Override
	public ByteBuffer getByteBuffer() {
		return bodyView;
	}

	@Override
	public int read() throws IOException {
		return 0;
	}

	@Override
	public int read(byte[] b, int off, int len) {
		jackRequest.action(ActionHook.ActionCode.READ_BODY, this);
		if (bodyView == null) {
			return -1;
		}
		int n = Math.min(len, bodyView.remaining());
		bodyView.get(b, off, n);
		return n;
	}
}
