## NIO 实践笔记

### IO 模型

### 事件处理的方式
**轮询事件**
线程不断轮询访问相关事件发生源有没有发生事件，有发生事件就调用事件处理逻辑。Java 原生的NIO就是使用的轮询方式。
**事件驱动**
事件驱动方式，发生事件，主线程把事件放入事件队列，在另外线程不断循环消费事件列表中的事件，调用事件对应的处理逻辑处理事件。
事件驱动方式也被称为消息通知方式，其实是设计模式中观察者模式的思路。

### Selector 原理
- [Selector 原理](https://www.jianshu.com/p/2b71ea919d49)
- [NIO编码](https://www.cnblogs.com/yeyang/p/12578701.html)
还是不太明白，这里的关系。

### Endpoint 中的使用


### 疑问
- ServerSocketChannel 与 Selector 之间的关系
- Selector 的机制是什么

### 参考
- [笔记](https://www.notion.so/zhouzg99/NIO-a26ab012e09441e7a60f016d6413aa8e)