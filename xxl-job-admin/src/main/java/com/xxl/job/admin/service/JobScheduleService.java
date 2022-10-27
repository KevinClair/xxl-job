package com.xxl.job.admin.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * job执行服务
 */
public interface JobScheduleService {

    /**
     * 调度执行器
     *
     * @return
     */
    boolean execute(Integer preReadCount, Map<Integer, List<Integer>> ringData) throws ParseException;
}
