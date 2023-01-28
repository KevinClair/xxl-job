package com.xxl.job.admin.service;

import com.xxl.job.common.dto.AddXxlJobInfoDto;
import com.xxl.job.common.dto.DeleteXxlJobInfoDto;
import com.xxl.job.common.dto.UpdateXxlJobInfoDto;

/**
 * 远程api接口
 */
public interface JobRemoteApiService {

    /**
     * 添加Job
     *
     * @param request {@link AddXxlJobInfoDto}
     * @return
     */
    String addJob(AddXxlJobInfoDto request);

    /**
     * 修改Job
     *
     * @param request {@link UpdateXxlJobInfoDto}
     * @return
     */
    String updateJob(UpdateXxlJobInfoDto request);

    /**
     * 删除job
     *
     * @param request {@link DeleteXxlJobInfoDto}
     * @return
     */
    String deleteJob(DeleteXxlJobInfoDto request);
}
