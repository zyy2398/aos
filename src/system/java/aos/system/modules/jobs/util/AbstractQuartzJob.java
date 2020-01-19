package aos.system.modules.jobs.util;

import aos.framework.core.utils.AOSCons;
import aos.framework.core.utils.AOSCxt;
import aos.framework.core.utils.AOSUtils;
import aos.system.dao.po.AosJobLogPO;
import aos.system.dao.po.AosJobPO;
import aos.system.modules.jobs.JobLogService;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * 抽象quartz调用
 *
 * @author ruoyi
 */
public abstract class AbstractQuartzJob implements Job
{
    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    /**
     * 线程本地变量
     */
    private static ThreadLocal<Date> threadLocal = new ThreadLocal<>();

    @Override
    public void execute(JobExecutionContext context)
    {
        AosJobPO sysJob = new AosJobPO();
        AOSUtils.copyProperties(context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES),sysJob);
        try
        {
            before(context, sysJob);
            if (sysJob != null)
            {
                doExecute(context, sysJob);
            }
            after(context, sysJob, null);
        }
        catch (Exception e)
        {
            log.error("任务执行异常  - ：", e);
            after(context, sysJob, e);
        }
    }

    /**
     * 执行前
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     */
    protected void before(JobExecutionContext context, AosJobPO sysJob)
    {
        threadLocal.set(AOSUtils.getDateTime());
    }

    /**
     * 执行后
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     */
    protected void after(JobExecutionContext context, AosJobPO sysJob, Exception e)
    {
        Date startTime = threadLocal.get();
        threadLocal.remove();

        final AosJobLogPO sysJobLog = new AosJobLogPO();
        sysJobLog.setJob_name(sysJob.getJob_name());
        sysJobLog.setJob_group(sysJob.getJob_group());
        sysJobLog.setInvoke_target(sysJob.getInvoke_target());
        sysJobLog.setStart_time(startTime);
        sysJobLog.setEnd_time(AOSUtils.getDateTime());
        sysJobLog.setCreate_time(AOSUtils.getDateTime());
        long runMs = sysJobLog.getEnd_time().getTime() - sysJobLog.getStart_time().getTime();
        sysJobLog.setJob_message(sysJobLog.getJob_name() + " 总共耗时：" + runMs + "毫秒");
        if (e != null)
        {
            sysJobLog.setStatus(AOSCons.STR_FALSE);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
            String str = sw.toString();
            String errorMsg = StringUtils.substring(str, 0, 2000);
            sysJobLog.setException_info(errorMsg);
        }
        else
        {
            sysJobLog.setStatus(AOSCons.STR_TRUE);
        }

        // 写入数据库当中
        JobLogService JobLogService = (JobLogService)AOSCxt.getBean("jobLogService");
        JobLogService.addJobLog(sysJobLog);
    }

    /**
     * 执行方法，由子类重载
     *
     * @param context 工作执行上下文对象
     * @param sysJob 系统计划任务
     * @throws Exception 执行过程中的异常
     */
    protected abstract void doExecute(JobExecutionContext context, AosJobPO sysJob) throws Exception;
}
