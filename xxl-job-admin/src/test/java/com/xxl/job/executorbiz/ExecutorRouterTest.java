package com.xxl.job.executorbiz;

import com.xxl.job.admin.XxlJobAdminApplication;
import com.xxl.job.admin.core.route.ExecutorRouter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Map;

@SpringBootTest(classes = XxlJobAdminApplication.class)
public class ExecutorRouterTest {

    @Resource
    private Map<String, ExecutorRouter> map;

    @Test
    public void test(){
        map.forEach((k, v) -> {
            System.out.println(k);
        });
    }
}
