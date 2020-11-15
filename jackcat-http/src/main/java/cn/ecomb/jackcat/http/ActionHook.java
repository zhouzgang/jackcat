package cn.ecomb.jackcat.http;

/**
 * Processor 处理后的回调机制
 *
 * @author zhouzg
 * @date 2020-10-31.
 */
public interface ActionHook {


	/**
	 * processor 回调
	 *
	 * @param code  回调类型
	 * @param param 参数，这里有可能是 doService 的结果
	 */
	void action(ActionCode code, Object... param);

	/**
	 * 处理回调类型
	 */
	enum ActionCode {

		ACK,
		/** 提交响应头数据到存缓存区 */
		COMMIT,
		/** 读取并解析参数 */
		PARSE_PARAMS,
		/** 读取并解析请求体数据 */
		READ_BODY,
		/** 写入返回数据 */
		WRITE_BODY,
		/** 将返回数据发送给客户端 */
		FLUSH,
		/** 响应处理完毕 */
		CLOSE
	}

}
