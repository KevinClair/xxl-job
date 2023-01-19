package com.xxl.job.admin.util;

import com.xxl.job.common.utils.I18nUtil;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * email util test
 *
 * @author xuxueli 2017-12-22 17:16:23
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class I18nUtilTest {
    private static Logger logger = LoggerFactory.getLogger(I18nUtilTest.class);

    @Test
    public void test(){
        logger.info(I18nUtil.getString("admin_name"));
    }

}
