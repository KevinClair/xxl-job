package com.xxl.job.admin.dao;

import org.apache.ibatis.annotations.Mapper;

/**
 * XxlJobLockDao.
 *
 * @author KevinClair
 **/
@Mapper
public interface XxlJobLockDao {

    /**
     * mysql行锁
     *
     * @return
     */
    String lock();
}
