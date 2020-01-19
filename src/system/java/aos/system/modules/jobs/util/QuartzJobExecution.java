package aos.system.modules.jobs.util;

import aos.system.dao.po.AosJobPO;
import org.quartz.JobExecutionContext;

/**
 * 定时任务处理（允许并发执行）
 * 
 * @author ruoyi
 *
 */
public class QuartzJobExecution extends AbstractQuartzJob
{
    @Override
    protected void doExecute(JobExecutionContext context, AosJobPO sysJob) throws Exception
    {
        JobInvokeUtil.invokeMethod(sysJob);
    }
}
