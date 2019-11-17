package cn.ecomb.jackcat.net;

/**
 * 请求处理器
 * 适配不同协议
 * @author zhouzg
 * @date 2019-10-13.
 */
public interface Processor {

    void process(NioChannel channel);
}
