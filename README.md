## Introduction

在xxl-job上做的一些更改，包括代码逻辑完善等。
原先的README文档请看 [这里](https://github.com/xuxueli/xxl-job)

### 调整内容

## xxl-job-common

* 新增xxl-job-common模块，用于存放admin和core共用的类
* 调整了一些Maven依赖

## xxl-job-admin

* 整体SpringBoot化，原来的通过Instance获取单实例的方式调整为Bean注入；
* 一些单线程任务的调整；
* 调度主逻辑的调整，由原先的编程式事务调整为声明式事务，更安全，更简单；
* 在调度逻辑上添加了一些代码注释，方便理解；
* 取消对core模块的依赖，改由引入common模块；
* 增加了对Controller接口的参数校验，方便验证请求；
* 优化了全局异常处理，取消原有的[WebExceptionResolver](src/main/java/com/xxl/job/admin/controller/resolver/WebExceptionResolver.java)
  ，使用[GlobalExceptionHandler](src/main/java/com/xxl/job/admin/controller/resolver/GlobalExceptionHandler.java)操作全局异常
* 对/api下的所有接口进行了统一的token验证

```java

@Component
public class PermissionInterceptor implements AsyncHandlerInterceptor {
  // 省略

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    // 对JobApiController下的接口统一进行token的校验
    if (request.getRequestURI().startsWith(Constants.NEED_CHECK_TOKEN_URI)) {
      if (StringUtils.hasText(adminConfig.getAccessToken()) && !adminConfig.getAccessToken().equals(request.getHeader(Constants.XXL_JOB_ACCESS_TOKEN))) {
        throw new XxlJobException("The access token is wrong.");
      }
    }
    return true;    // proceed with the next interceptor
  }

}
```

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
public class A implements DisposableBean {
    @Override
    public void destroy() throws Exception {
        // 关闭线程或者线程池
    }
}
```

*
优化了部分代码，包括AdminManager的生成方式等，具体见[AdminManagerClientWrapper](src/main/java/com/xxl/job/core/executor/AdminManagerClientWrapper.java)
;
* 在客户端(应用端)增加了添加job,删除job,更新job方法[AdminManager](src/main/java/com/xxl/job/common/service/AdminManager.java);

```java
public class Test {

    @Resources
    private AdminManagerClientWrapper wrapper;

    public void addJob() {
        AddXxlJobInfoDto infoDto = new AddXxlJobInfoDto();
        wrapper.getAdminManager().addJob(infoDto);
    }

    public void deleteJob() {
        DeleteXxlJobInfoDto infoDto = new DeleteXxlJobInfoDto();
        wrapper.getAdminManager().deleteJob(infoDto);
    }

    public void updateJob() {
        UpdateXxlJobInfoDto infoDto = new UpdateXxlJobInfoDto();
        wrapper.getAdminManager().updateJob(infoDto);
    }
}
```

* 完善了@XxlJob注解功能，提供自动注册job任务
  * 暂时会根据job_desc判断是否重复，不重复则新增

## xxl-job-spring-boot-starter

* 新增的spring-boot-starter模块;
* 增加了属性的配置项，以及属性关联；
* 增加了启动时xxl-job的启动Logo的打印；
* 调整了spring模式下的一些bean注入的方式，原先都是通过Instance实例的方式获取，后统一通过Spring管理bean。

## todo

* 取消admin对core的依赖，提取公共模块(done)
* 在core中新增添加job,删除job,更新job方法(done)
* 新增调度类型，固定延迟
* 增加@XxlJob的属性(done)
* Controller的参数校验(done)
* 调整maven包依赖管理(done)
* XxlJobGroup中的app_name字段唯一性(done)
* XxlJobInfo中的job_desc字段唯一性(done)
* core的Http服务对路由处理，使用策略模式重构
* 取消客户端的重复注册逻辑，改为只在启动时注册，后续通过Admin进行探活
