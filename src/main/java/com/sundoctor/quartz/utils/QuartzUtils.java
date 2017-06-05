package com.sundoctor.quartz.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import com.sundoctor.quartz.entity.ScheduleJob;

/**
 * quartz_job的工具类
 */
@Service
public class QuartzUtils {
	private final Logger logger = LoggerFactory.getLogger(QuartzUtils.class);

	@Resource
	private Scheduler scheduler;

	/**
	 * 
	 * 获取计划任务列表
	 * 
	 * @return List<ScheduleJob>
	 */
	public List<ScheduleJob> getPlanJobList() throws SchedulerException {
		List<ScheduleJob> jobList = new ArrayList<ScheduleJob>();
		GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
		Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
		
		jobKeys = scheduler.getJobKeys(matcher);
		for (JobKey jobKey : jobKeys) {
			List<? extends Trigger> triggers = scheduler
					.getTriggersOfJob(jobKey);
			for (Trigger trigger : triggers) {
				ScheduleJob job = new ScheduleJob();
				job.setJobName(jobKey.getName());
				job.setJobGroup(jobKey.getGroup());
				// 此处是我自己业务需要，给每个定时任务配置类对应的编号和描述
				/*String value = PropertiesUtils.getStringCN(jobKey.getName());
				if (null != value && !"".equals(value)) {
					job.setJobNo(value.split("/")[0]);
					job.setDesc(value.split("/")[1]);
				} else {
					job.setJobNo("0000");
					job.setDesc("未监控任务");
				}*/
				job.setTriggerName("触发器:" + trigger.getKey());
				Trigger.TriggerState triggerState = scheduler
						.getTriggerState(trigger.getKey());
				job.setJobStatus(triggerState.name());
				if (trigger instanceof CronTrigger) {
					CronTrigger cronTrigger = (CronTrigger) trigger;
					String cronExpression = cronTrigger.getCronExpression();
					job.setCronExpression(cronExpression);
				}
				jobList.add(job);
			}
		}
		// 对返回的定时任务安装编号做排序
		/*Collections.sort(jobList, new Comparator<ScheduleJob>() {
			public int compare(ScheduleJob arg0, ScheduleJob arg1) {
				return arg0.getJobNo().compareTo(arg1.getJobNo());
			}
		});*/

		return jobList;
	}

	/**
	 * 获取正在运行的任务列表
	 * 
	 * @return List<ScheduleJob>
	 */
	public List<ScheduleJob> getCurrentJobList() throws SchedulerException {
		List<JobExecutionContext> executingJobs = scheduler
				.getCurrentlyExecutingJobs();
		;
		List<ScheduleJob> jobList = new ArrayList<ScheduleJob>(
				executingJobs.size());
		;
		for (JobExecutionContext executingJob : executingJobs) {
			ScheduleJob job = new ScheduleJob();
			JobDetail jobDetail = executingJob.getJobDetail();
			JobKey jobKey = jobDetail.getKey();
			Trigger trigger = executingJob.getTrigger();
			job.setJobName(jobKey.getName());
			job.setJobGroup(jobKey.getGroup());
	/*		String value = PropertiesUtils.getStringCN(jobKey.getName());
			if (null != value && !"".equals(value)) {
				job.setJobNo(value.split("/")[0]);
				job.setDesc(value.split("/")[1]);
			} else {
				job.setJobNo("0000");
				job.setDesc("未监控任务");
			}*/
			job.setTriggerName("触发器:" + trigger.getKey());
			Trigger.TriggerState triggerState = scheduler
					.getTriggerState(trigger.getKey());
			job.setJobStatus(triggerState.name());
			if (trigger instanceof CronTrigger) {
				CronTrigger cronTrigger = (CronTrigger) trigger;
				String cronExpression = cronTrigger.getCronExpression();
				job.setCronExpression(cronExpression);
			}
			jobList.add(job);
		}
		/*Collections.sort(jobList, new Comparator<ScheduleJob>() {
			public int compare(ScheduleJob arg0, ScheduleJob arg1) {
				return arg0.getJobNo().compareTo(arg1.getJobNo());
			}
		});*/
		return jobList;
	}

	/**
	 * 暂停当前任务
	 * 
	 * @param scheduleJob
	 */
	public void pauseJob(ScheduleJob scheduleJob) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(),
				scheduleJob.getJobGroup());
		if (scheduler.checkExists(jobKey)) {
			scheduler.pauseJob(jobKey);
		}
	}

	/**
	 * 恢复当前任务
	 * 
	 * @param scheduleJob
	 */
	public void resumeJob(ScheduleJob scheduleJob) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(),
				scheduleJob.getJobGroup());

		if (scheduler.checkExists(jobKey)) {
			// 并恢复
			scheduler.resumeJob(jobKey);
			// 重置当前时间
			this.rescheduleJob(scheduleJob);
		}
	}

	/**
	 * 删除任务
	 * 
	 * @param scheduleJob
	 * @return boolean
	 */
	public boolean deleteJob(ScheduleJob scheduleJob) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(),
				scheduleJob.getJobGroup());
		if (scheduler.checkExists(jobKey)) {
			return scheduler.deleteJob(jobKey);
		}
		return false;

	}

	/**
	 * 立即触发当前任务
	 * 
	 * @param scheduleJob
	 */
	public void triggerJob(ScheduleJob scheduleJob) throws SchedulerException {
		JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(),
				scheduleJob.getJobGroup());
		if (scheduler.checkExists(jobKey)) {
			scheduler.triggerJob(jobKey);
		}

	}

	/**
	 * 更新任务的时间表达式
	 * 
	 * @param scheduleJob
	 * @return Date
	 */
	public Date rescheduleJob(ScheduleJob scheduleJob)
			throws SchedulerException {
		TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(),
				scheduleJob.getJobGroup());
		if (scheduler.checkExists(triggerKey)) {
			CronTrigger trigger = (CronTrigger) scheduler
					.getTrigger(triggerKey);
			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder
					.cronSchedule(scheduleJob.getCronExpression());
			// 按新的cronExpression表达式重新构建trigger
			trigger = trigger.getTriggerBuilder().withIdentity(triggerKey)
					.withSchedule(scheduleBuilder).build();
			// 按新的trigger重新设置job执行
			return scheduler.rescheduleJob(triggerKey, trigger);
		}
		return null;
	}

	/**
	 * 查询其中一个任务的状态
	 * 
	 * @param scheduleJob
	 * @return
	 * @throws SchedulerException
	 */
	public String scheduleJob(ScheduleJob scheduleJob)
			throws SchedulerException {
		String status = null;
		TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(),
				scheduleJob.getJobGroup());
		CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
		if (null != trigger) {
			Trigger.TriggerState triggerState = scheduler
					.getTriggerState(trigger.getKey());
			status = triggerState.name();
		}
		return status;
	}

	/**
	 * 校验job是否已经加载
	 * 
	 * @param scheduleJob
	 *            JOB基本信息参数
	 * @return 是否已经加载
	 */
	public boolean checkJobExisted(ScheduleJob scheduleJob)
			throws SchedulerException {
		return scheduler.checkExists(new JobKey(scheduleJob.getJobName(),
				scheduleJob.getJobGroup()));
	}

	private String getStatuDesc(String status) {
		if (status.equalsIgnoreCase("NORMAL")) {
			return "正常";
		} else if (status.equalsIgnoreCase("PAUSED")) {
			return "暂停";
		} else {
			return "异常";
		}
	}
}
