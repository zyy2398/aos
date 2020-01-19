package aos.system.dao.po;

import aos.framework.core.typewrap.PO;
import aos.system.modules.jobs.util.CronUtils;
import aos.system.modules.jobs.util.ScheduleConstants;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

public class AosJobPO extends PO {
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long job_id;

    /** 任务名称 */
    private String job_name;

    /** 任务组名 */
    private String job_group;

    /** 调用目标字符串 */
    private String invoke_target;

    /** cron执行表达式 */
    private String cron_expression;

    /** cron计划策略 */
    private String misfire_policy = ScheduleConstants.MISFIRE_DEFAULT;

    /** 是否并发执行（0允许 1禁止） */
    private String concurrent;

    /** 任务状态（0正常 1暂停） */
    private String status;

    private String create_by;

    private Date create_time;

    private String update_by;

    private Date update_time;

    private String remark;

    public Long getJob_id() {
        return job_id;
    }

    public void setJob_id(Long job_id) {
        this.job_id = job_id;
    }

    public String getJob_name() {
        return job_name;
    }

    public void setJob_name(String job_name) {
        this.job_name = job_name;
    }

    public String getJob_group() {
        return job_group;
    }

    public void setJob_group(String job_group) {
        this.job_group = job_group;
    }

    public String getInvoke_target() {
        return invoke_target;
    }

    public void setInvoke_target(String invoke_target) {
        this.invoke_target = invoke_target;
    }

    public String getCron_expression() {
        return cron_expression;
    }

    public void setCron_expression(String cron_expression) {
        this.cron_expression = cron_expression;
    }

    public Date getNextValidTime()
    {
        if (StringUtils.isNotEmpty(cron_expression))
        {
            return CronUtils.getNextExecution(cron_expression);
        }
        return null;
    }

    public String getMisfire_policy() {
        return misfire_policy;
    }

    public void setMisfire_policy(String misfire_policy) {
        this.misfire_policy = misfire_policy;
    }

    public String getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(String concurrent) {
        this.concurrent = concurrent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreate_by() {
        return create_by;
    }

    public void setCreate_by(String create_by) {
        this.create_by = create_by;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_by() {
        return update_by;
    }

    public void setUpdate_by(String update_by) {
        this.update_by = update_by;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
