package com.sundoctor.quartz.web;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sundoctor.quartz.entity.ScheduleJob;
import com.sundoctor.quartz.utils.QuartzUtils;

import javax.annotation.Resource;

import java.util.List;

/**
 * Created by lyndon on 16/9/13.
 */
@RestController
public class QuartzController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Resource
	private QuartzUtils quartzUtils;

	// 获取定时任务的列表
	@RequestMapping(value = { "/getJobList" })
	public List<ScheduleJob> getPlanJobList(String openId) {
		// QuartzUtils quartzUtils = new QuartzUtils();
		List<ScheduleJob> list = null;
		try {
			list = quartzUtils.getPlanJobList();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return list;
	}

	// 暂停任务
	@RequestMapping(value = { "/pauseJob" })
	public String pauseJob(String openId) {
		// QuartzUtils quartzUtils = new QuartzUtils();
		ScheduleJob job = new ScheduleJob();
		job.setJobGroup("innmall_job");
		job.setJobName("refreshWxToKenJobDetail");
		try {
			quartzUtils.pauseJob(job);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return "暂停成功";
	}

	// 恢复任务
	@RequestMapping(value = { "/resumeJob" })
	public String resumeJob(String openId) {
		// QuartzUtils quartzUtils = new QuartzUtils();
		ScheduleJob job = new ScheduleJob();
		job.setJobGroup("innmall_job");
		job.setJobName("refreshWxToKenJobDetail");
		try {
			quartzUtils.resumeJob(job);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return "恢复成功";
	}

	// 立即触发任务
	@RequestMapping(value = { "/triggerJob" })
	public String triggerJob(String openId) {
		// QuartzUtils quartzUtils = new QuartzUtils();
		ScheduleJob job = new ScheduleJob();
		job.setJobGroup("innmall_job");
		job.setJobName("refreshWxToKenJobDetail");
		try {
			quartzUtils.triggerJob(job);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return "触发成功";
	}

	// 删除任务
	@RequestMapping(value = { "/deleteJob" })
	public String deleteJob(String openId) {
		// QuartzUtils quartzUtils = new QuartzUtils();
		ScheduleJob job = new ScheduleJob();
		job.setJobGroup("innmall_job");
		job.setJobName("refreshWxToKenJobDetail");
		try {
			quartzUtils.deleteJob(job);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return "触发成功";
	}

}