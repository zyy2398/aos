package aos.system.dao.po;

import aos.framework.core.typewrap.PO;

import java.util.Date;

public class AosJobLogPO extends PO {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long job_log_id;

    /** 任务名称 */
    private String job_name;

    /** 任务组名 */
    private String job_group;

    /** 调用目标字符串 */
    private String invoke_target;

    /** 日志信息 */
    private String job_message;

    /** 执行状态（1正常 0失败） */
    private String status;

    /** 异常信息 */
    private String exception_info;

    /** 开始时间 */
    private Date start_time;

    /** 结束时间 */
    private Date end_time;

    private Date create_time;

    public Long getJob_log_id() {
        return job_log_id;
    }

    public void setJob_log_id(Long job_log_id) {
        this.job_log_id = job_log_id;
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

    public String getJob_message() {
        return job_message;
    }

    public void setJob_message(String job_message) {
        this.job_message = job_message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getException_info() {
        return exception_info;
    }

    public void setException_info(String exception_info) {
        this.exception_info = exception_info;
    }

    public Date getStart_time() {
        return start_time;
    }

    public void setStart_time(Date start_time) {
        this.start_time = start_time;
    }

    public Date getEnd_time() {
        return end_time;
    }

    public void setEnd_time(Date end_time) {
        this.end_time = end_time;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }
}
