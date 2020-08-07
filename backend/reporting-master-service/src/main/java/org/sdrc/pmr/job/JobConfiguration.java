package org.sdrc.pmr.job;

import javax.annotation.PostConstruct;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

/**
 * 
 * This will configure the job to run within quartz.
 * 
 * @author Sarita
 *
 */
//@Configuration
public class JobConfiguration {
//
//	@Autowired
//	private SchedulerFactoryBean schedulerFactoryBean;
//
//	@PostConstruct
//	private void initialize() throws Exception {
//		schedulerFactoryBean.getScheduler().addJob(coverageJobDetail(), true, true);
//
//		if (!schedulerFactoryBean.getScheduler()
//				.checkExists(new TriggerKey(SchedulerConstants.COVERAGE_JOB_TRIGGER_KEY, SchedulerConstants.COVERAGE_JOB_GROUP))) {
//			schedulerFactoryBean.getScheduler().scheduleJob(coverageJobTrigger());
//
//		}
//		
//	}
//
//	/**
//	 * <p>
//	 * The job is configured here where we provide the job class to be run on each
//	 * invocation. We give the job a name and a value so that we can provide the
//	 * trigger to it on our method {@link #tpJobTrigger()}
//	 *  {@link #defaultJobTrigger()}
//	 *  {@link #defaultNotifyJobTrigger()}
//	 * </p>
//	 * 
//	 * @return an instance of {@link JobDetail}
//	 */
//	private static JobDetail coverageJobDetail() {
//		JobDetailImpl jobDetail = new JobDetailImpl();
//		jobDetail.setKey(new JobKey(SchedulerConstants.COVERAGE_JOB_KEY, SchedulerConstants.COVERAGE_JOB_GROUP));
//		jobDetail.setJobClass(AggregateJob.class);
//		jobDetail.setDurability(true);
//		jobDetail.setRequestsRecovery(true);
//		return jobDetail;
//	}
//
//
//	/**
//	 * <p>
//	 * This method will define the frequency with which we will be running the
//	 * scheduled job which in this instance is 7 hourly.
//	 * </p>
//	 * 
//	 * @return an instance of {@link Trigger}
//	 */
//	/*cron="0 0 0/7 1/1 * ?" -> 7hrs gap
//	1.	Wednesday, September 4, 2019 7:00 AM
//	2.	Wednesday, September 4, 2019 2:00 PM
//	3.	Wednesday, September 4, 2019 9:00 PM
//	4.	Thursday, September 5, 2019 12:00 AM
//	5.	Thursday, September 5, 2019 7:00 AM*/
//	private static Trigger coverageJobTrigger() {
//		return TriggerBuilder.newTrigger().forJob(coverageJobDetail())
//				.withIdentity(SchedulerConstants.COVERAGE_JOB_TRIGGER_KEY, SchedulerConstants.COVERAGE_JOB_GROUP).withPriority(50)
//				.withSchedule(CronScheduleBuilder.cronSchedule("0 0 0/3 1/1 * ?")).build();
//	}

}
