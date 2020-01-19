package aos.system.modules.jobs.util;

import aos.framework.core.exception.AOSException;
import aos.system.dao.po.AosJobPO;
import org.quartz.*;

/**
 * 定时任务工具类
 * 
 * @author ruoyi
 *
 */
public class ScheduleUtils
{
    /**
     * 得到quartz任务类
     *
     * @param aosJobPo 执行计划
     * @return 具体执行任务类
     */
    private static Class<? extends Job> getQuartzJobClass(AosJobPO aosJobPo) {
        boolean isConcurrent = "0".equals(aosJobPo.getConcurrent());
        return isConcurrent ? QuartzJobExecution.class : QuartzDisallowConcurrentExecution.class;
    }

    /**
     * 构建任务触发对象
     */
    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey(ScheduleConstants.TASK_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * 构建任务键对象
     */
    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey(ScheduleConstants.TASK_CLASS_NAME + jobId, jobGroup);
    }

    /**
     * 创建定时任务
     */
    public static void createScheduleJob(Scheduler scheduler, AosJobPO job) throws SchedulerException,AOSException {
        Class<? extends Job> jobClass = getQuartzJobClass(job);
        // 构建job信息
        Long jobId = job.getJob_id();
        String jobGroup = job.getJob_group();
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(getJobKey(jobId, jobGroup)).build();
        CronScheduleBuilder cronScheduleBuilder = null;
        try{
            cronScheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCron_expression());

            // 表达式调度构建器
            cronScheduleBuilder = handleCronScheduleMisfirePolicy(job, cronScheduleBuilder);
            // 按新的cronExpression表达式构建一个新的trigger
            CronTrigger trigger = TriggerBuilder.newTrigger().withIdentity(getTriggerKey(jobId, jobGroup))
                    .withSchedule(cronScheduleBuilder).build();

            // 放入参数，运行时的方法可以获取
            jobDetail.getJobDataMap().put(ScheduleConstants.TASK_PROPERTIES, job);

            // 判断是否存在
            if (scheduler.checkExists(getJobKey(jobId, jobGroup)))
            {
                // 防止创建时存在数据问题 先移除，然后在执行创建操作
                scheduler.deleteJob(getJobKey(jobId, jobGroup));
            }

            scheduler.scheduleJob(jobDetail, trigger);

            // 暂停任务
            if (job.getStatus().equals(ScheduleConstants.Status.PAUSE.getValue()))
            {
                scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
            }
        }catch (RuntimeException e){
            throw new AOSException("cron表达式错误，请填入正确的表达式");
        }
    }

    /**
     * 设置定时任务策略
     */
    public static CronScheduleBuilder handleCronScheduleMisfirePolicy(AosJobPO job, CronScheduleBuilder cb) throws AOSException {
        switch (job.getMisfire_policy()) {
            case ScheduleConstants.MISFIRE_DEFAULT:
                return cb;
            case ScheduleConstants.MISFIRE_IGNORE_MISFIRES:
                return cb.withMisfireHandlingInstructionIgnoreMisfires();
            case ScheduleConstants.MISFIRE_FIRE_AND_PROCEED:
                return cb.withMisfireHandlingInstructionFireAndProceed();
            case ScheduleConstants.MISFIRE_DO_NOTHING:
                return cb.withMisfireHandlingInstructionDoNothing();
            default:
                throw new AOSException("The task misfire policy '" + job.getMisfire_policy()
                        + "' cannot be used in cron schedule tasks");
        }
    }
}