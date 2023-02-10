package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.vo.AddXxlJobGroupRequest;
import com.xxl.job.admin.controller.vo.UpdateXxlJobGroupRequest;
import com.xxl.job.admin.core.exception.XxlJobException;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobRegistry;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobRegistryDao;
import com.xxl.job.common.enums.RegistryConstants;
import com.xxl.job.common.model.ReturnT;
import com.xxl.job.common.utils.I18nUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * job group controller
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

	public final XxlJobInfoDao xxlJobInfoDao;
	public final XxlJobGroupDao xxlJobGroupDao;
	private final XxlJobRegistryDao xxlJobRegistryDao;

	public JobGroupController(XxlJobInfoDao xxlJobInfoDao, XxlJobGroupDao xxlJobGroupDao, XxlJobRegistryDao xxlJobRegistryDao) {
		this.xxlJobInfoDao = xxlJobInfoDao;
		this.xxlJobGroupDao = xxlJobGroupDao;
		this.xxlJobRegistryDao = xxlJobRegistryDao;
	}

	@RequestMapping
	public String index(Model model) {
		return "jobgroup/jobgroup.index";
	}

	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(HttpServletRequest request,
										@RequestParam(required = false, defaultValue = "0") int start,
										@RequestParam(required = false, defaultValue = "10") int length,
										String appname, String title) {

		// page query
		List<XxlJobGroup> list = xxlJobGroupDao.pageList(start, length, appname, title);
		int list_count = xxlJobGroupDao.pageListCount(start, length, appname, title);

		// package result
		Map<String, Object> maps = new HashMap<String, Object>();
		maps.put("recordsTotal", list_count);		// 总记录数
		maps.put("recordsFiltered", list_count);	// 过滤后的总记录数
		maps.put("data", list);  					// 分页列表
		return maps;
	}

	@RequestMapping("/save")
	@ResponseBody
	public ReturnT<String> save(@RequestBody @Valid AddXxlJobGroupRequest request) {

		// valid
		if (request.getAppname().contains(">") || request.getAppname().contains("<")) {
			return new ReturnT<String>(500, "AppName" + I18nUtil.getString("system_unvalid"));
		}
		if (request.getTitle().contains(">") || request.getTitle().contains("<")) {
			return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_title") + I18nUtil.getString("system_unvalid"));
		}
		XxlJobGroup xxlJobGroup = new XxlJobGroup();
		Date current = new Date();
		xxlJobGroup.setAppname(request.getAppname());
		xxlJobGroup.setAddressType(request.getAddressType());
		xxlJobGroup.setTitle(request.getTitle());
		xxlJobGroup.setUpdateTime(current);
		if (request.getAddressType() == 1) {
			if (request.getAddressList().contains(">") || request.getAddressList().contains("<")) {
				return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList") + I18nUtil.getString("system_unvalid"));
			}
			// 根据appName删除所有的registry信息
			xxlJobRegistryDao.deleteByAppName(request.getAppname());
			List<XxlJobRegistry> xxlJobRegistryList = new ArrayList<>();
			String addressString = Arrays.asList(request.getAddressList().split(",")).stream().filter(StringUtils::hasText).map(each -> {
				// 更新registry信息
				XxlJobRegistry xxlJobRegistry = new XxlJobRegistry();
				xxlJobRegistry.setRegistryGroup(RegistryConstants.RegistryType.ADMIN.name());
				xxlJobRegistry.setRegistryKey(request.getAppname());
				xxlJobRegistry.setRegistryValue(each);
				xxlJobRegistry.setUpdateTime(current);
				xxlJobRegistryList.add(xxlJobRegistry);
				return each;
			}).collect(Collectors.joining(","));
			if (!StringUtils.hasText(addressString)) {
				return new ReturnT<String>(500, I18nUtil.getString("jobgroup_field_registryList_unvalid"));
			}
			xxlJobRegistryDao.saveBatch(xxlJobRegistryList);
		}

		// process
		xxlJobGroup.setUpdateTime(current);

		int ret = xxlJobGroupDao.save(xxlJobGroup);
		return (ret>0)?ReturnT.SUCCESS:ReturnT.FAIL;
	}

	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(@RequestBody @Valid UpdateXxlJobGroupRequest request) {
		// valid
		XxlJobGroup xxlJobGroup = xxlJobGroupDao.load(request.getId());
		if (Objects.isNull(xxlJobGroup)) {
			return new ReturnT<String>(500, "Invalid id");
		}
		// 无论是自动注册还是手动注册，只要修改了地址，统一以修改后的地址为准，删除xxl_job_registry中的数据，重新添加
		// 删除原有的xxl_job_registry中的数据
		xxlJobRegistryDao.deleteByAppName(xxlJobGroup.getAppname());
		// 重新添加
		Date current = new Date();
		List<XxlJobRegistry> xxlJobRegistryList = Arrays.stream(request.getAddressList().split(",")).map(each -> {
			if (!StringUtils.hasText(each)) {
				throw new XxlJobException("Invalid address");
			}
			XxlJobRegistry registry = new XxlJobRegistry();
			registry.setRegistryGroup(xxlJobGroup.getAddressType() == 0 ? RegistryConstants.RegistryType.EXECUTOR.name() : RegistryConstants.RegistryType.ADMIN.name());
			registry.setRegistryKey(xxlJobGroup.getAppname());
			registry.setRegistryValue(each);
			registry.setUpdateTime(current);
			return registry;
		}).collect(Collectors.toList());
		xxlJobRegistryDao.saveBatch(xxlJobRegistryList);
		// 更新xxlJobGroup
		xxlJobGroup.setTitle(request.getTitle());
		xxlJobGroup.setAddressList(request.getAddressList());
		xxlJobGroup.setUpdateTime(current);
		return (xxlJobGroupDao.update(xxlJobGroup) > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
	}

	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id) {
		// valid
		XxlJobGroup xxlJobGroup = xxlJobGroupDao.load(id);
		if (Objects.isNull(xxlJobGroup)) {
			return new ReturnT<String>(500, "Invalid id");
		}
		// 删除原有的xxl_job_registry中的数据
		xxlJobRegistryDao.deleteByAppName(xxlJobGroup.getAppname());
		return (xxlJobGroupDao.remove(id) > 0) ? ReturnT.SUCCESS : ReturnT.FAIL;
	}

	@RequestMapping("/loadById")
	@ResponseBody
	public ReturnT<XxlJobGroup> loadById(int id) {
		XxlJobGroup jobGroup = xxlJobGroupDao.load(id);
		return jobGroup != null ? new ReturnT<XxlJobGroup>(jobGroup) : new ReturnT<XxlJobGroup>(ReturnT.FAIL_CODE, null);
	}

	private List<String> findRegistryByAppName(String appnameParam) {
		HashMap<String, List<String>> appAddressMap = new HashMap<String, List<String>>();
		List<XxlJobRegistry> list = xxlJobRegistryDao.findAll(RegistryConstants.DEAD_TIMEOUT, new Date());
		if (list != null) {
			for (XxlJobRegistry item : list) {
				if (RegistryConstants.RegistryType.EXECUTOR.name().equals(item.getRegistryGroup())) {
					String appname = item.getRegistryKey();
					List<String> registryList = appAddressMap.get(appname);
					if (registryList == null) {
						registryList = new ArrayList<String>();
					}

					if (!registryList.contains(item.getRegistryValue())) {
						registryList.add(item.getRegistryValue());
					}
					appAddressMap.put(appname, registryList);
				}
			}
		}
		return appAddressMap.get(appnameParam);
	}

}
