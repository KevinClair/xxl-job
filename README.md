## Introduction
在xxl-job上做的一些更改，包括代码逻辑完善等。
原先的README文档请看 [这里](https://github.com/xuxueli/xxl-job)
### 调整内容
## xxl-job-admin
* 整体SpringBoot化，原来的通过Instance获取单实例的方式调整为Bean注入；
* 一些单线程任务的调整；
* 调度主逻辑的调整，由原先的编程式事务调整为声明式事务，更安全，更简单；
* 在调度逻辑上添加了一些代码注释，方便理解；
## xxl-job-core
* 调整了部分代码，包括例如对象非null，集合非空，以及字符串非空的判断；
* 重构了Executor启动模块的代码，基本都使用了构造函数的方式启动线程；
* 类的一些属性上的调整，例如一些缓存数据，通过单独的类来管理一些缓存数据；
* 一些轮询的线程，如果可以调整为定时任务的，均调整为使用定时任务线程池处理任务，取代原先的定义线程，之后`Thread.sleep()`的方式实现调度；
```java
public class A {
    ScheduledThreadPoolExecutor threadPool = new ScheduledThreadPoolExecutor();
}
```
* 删除了非Spring模式下的启动模式；
* 调整了Http服务器的启动方式，改为主线程生成；
* 在部分类中存在线程关闭的场景，也通过Spring的bean生命周期的一些方式，关闭线程或者线程池；
```java
// 例如
public class A implements DisposableBean{
    @Override
    public void destroy() throws Exception{
        // 关闭线程或者线程池
    }
}
```
## xxl-job-spring-boot-starter
* 新增的spring-boot-starter模块;
* 增加了属性的配置项，以及属性关联；
* 增加了启动时xxl-job的启动Logo的打印；
* 调整了spring模式下的一些bean注入的方式，原先都是通过Instance实例的方式获取，后统一通过Spring管理bean。

## todo
* 取消admin对core的依赖，提取公共模块
* 在core中新增添加job和删除job的方法
* 新增调度类型，固定延迟
* 增加@XxlJob的属性
* Controller的参数校验
* 调整maven包依赖管理
* XxlJobGroup中的app_name字段唯一性
