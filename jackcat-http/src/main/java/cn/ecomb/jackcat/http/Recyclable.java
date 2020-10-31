package cn.ecomb.jackcat.http;

/**
 * 可回收重复利用的对象
 * 这里可以对比一下对象池
 *
 * @author zhouzg
 * @date 2020-11-01.
 */
public interface Recyclable {

	/**
	 * 回收释放资源，供下次请求使用
	 */
	void recycle();
}
