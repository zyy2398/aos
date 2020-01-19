package aos.system.modules.jobs.util;

import aos.system.dao.po.AosJobPO;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 定时任务处理（禁止并发执行）
 * 
 * @author ruoyi
 *
 */
@DisallowConcurrentExecution
public class QuartzDisallowConcurrentExecution extends AbstractQuartzJob
{
    @Override
    protected void doExecute(JobExecutionContext context, AosJobPO aosJobPo) throws Exception
    {
        JobInvokeUtil.invokeMethod(aosJobPo);
    }
}
