package aos.system.modules.jobs;

import aos.framework.core.exception.AOSException;
import aos.framework.core.typewrap.Dto;
import aos.framework.core.typewrap.Dtos;
import aos.framework.core.utils.AOSCons;
import aos.framework.core.utils.AOSJson;
import aos.framework.core.utils.AOSUtils;
import aos.framework.web.router.HttpModel;
import aos.system.common.id.IdService;
import aos.system.common.model.UserModel;
import aos.system.common.utils.SystemCons;
import aos.system.dao.AosJobDao;
import aos.system.dao.po.AosJobPO;
import aos.system.modules.jobs.util.ScheduleConstants;
import aos.system.modules.jobs.util.ScheduleUtils;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

@Service
public class JobService {
    @Autowired
    private Scheduler scheduler;
    @Autowired
    private AosJobDao aosJobDao;
    @Autowired
    private IdService idService;

    public void init(HttpModel httpModel){
        httpModel.setViewPath("system/job.jsp");
    }

    public void listJob(HttpModel httpModel){
        Dto qDto = httpModel.getInDto();
        List<AosJobPO> list =  aosJobDao.listPage(qDto);
        httpModel.setOutMsg(AOSJson.toGridJson(list,qDto.getPageTotal()));
    }

    @Transactional
    public void addJob(HttpModel httpModel) {
        Dto iDto = httpModel.getInDto();
        String message = "";
        Dto outDto = Dtos.newOutDto();
        UserModel userModel = httpModel.getUserModel();
        AosJobPO aosJobPO = new AosJobPO();
        aosJobPO.copyProperties(iDto);
        aosJobPO.setJob_id(idService.nextValue(SystemCons.SEQ.SEQ_SYSTEM).longValue());
        aosJobPO.setCreate_by(userModel.getId().toString());
        aosJobPO.setCreate_time(AOSUtils.getDateTime());
        try {
            int rows = aosJobDao.insert(aosJobPO);
            if (rows > 0) {
                ScheduleUtils.createScheduleJob(scheduler, aosJobPO);
                message = AOSUtils.merge("定时任务新增成功，任务名【{0}】",aosJobPO.getJob_name());
            }
        } catch (SchedulerException e) {
            message = "定时任务创建失败";
            outDto.setAppCode(AOSCons.ERROR);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }catch (AOSException e) {
            message = e.getMessage();
            outDto.setAppCode(AOSCons.ERROR);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } finally {
            outDto.setAppMsg(message);
            httpModel.setOutMsg(AOSJson.toJson(outDto));
        }
    }

    @Transactional
    public void updateJob(HttpModel httpModel) {
        Dto iDto = httpModel.getInDto();
        Dto outDto = Dtos.newOutDto();
        String message = "";
        UserModel userModel = httpModel.getUserModel();
        AosJobPO aosJobPO = new AosJobPO();
        aosJobPO.copyProperties(iDto);
        aosJobPO.setUpdate_by(userModel.getId().toString());
        aosJobPO.setUpdate_time(AOSUtils.getDateTime());
        int rows = aosJobDao.update(aosJobPO);
        if(rows>0){
            try {
                updateSchedulerJob(aosJobPO,aosJobPO.getJob_group());
                message = AOSUtils.merge("定时任务修改成功，任务名【{0}】",aosJobPO.getJob_name());
            } catch (AOSException e) {
                message = e.getMessage();
                outDto.setAppCode(AOSCons.ERROR);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            } catch (SchedulerException e) {
                message = "定时任务更新失败";
                outDto.setAppCode(AOSCons.ERROR);
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
        outDto.setAppMsg(message);
        httpModel.setOutMsg(AOSJson.toJson(outDto));
    }

    @Transactional
    public void deleteJob(HttpModel httpModel) throws SchedulerException {
        String[] selectionIds = httpModel.getInDto().getRows();
        int i=0;
        for(String job_id : selectionIds){
            AosJobPO job = aosJobDao.selectJobById(Long.valueOf(job_id));
            int result = aosJobDao.deleteByKey(job_id);
            if (result > 0)
            {
                scheduler.deleteJob(ScheduleUtils.getJobKey(job.getJob_id(), job.getJob_group()));
            }
            i++;
        }
        httpModel.setOutMsg(AOSUtils.merge("定时任务删除成功，共删除【{0}】条任务",i));
    }

    @Transactional
    public void run(HttpModel httpModel) throws SchedulerException {
        Dto qDto = httpModel.getInDto();
        Long job_id = qDto.getLong("job_id");
        String job_group = qDto.getString("job_group");
        AosJobPO properties = aosJobDao.selectJobById(job_id);
        // 参数
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(ScheduleConstants.TASK_PROPERTIES, properties);
        scheduler.triggerJob(ScheduleUtils.getJobKey(job_id, job_group), dataMap);
    }

    /**
     * 改变任务状态
     * @param httpModel
     * @throws SchedulerException
     */
    @Transactional
    public void changeStatus(HttpModel httpModel) throws SchedulerException {
        int rows = 0;
        String msg = "任务成功";
        String flag = "";
        Dto inDto = httpModel.getInDto();
        Dto outDto = Dtos.newOutDto();
        String status = inDto.getString("status");
        Long job_id = inDto.getLong("job_id");
        AosJobPO job = aosJobDao.selectJobById(job_id);
        if (ScheduleConstants.Status.NORMAL.getValue().equals(status)) {
            rows = resumeJob(job);
            flag = "恢复";
        }
        else if (ScheduleConstants.Status.PAUSE.getValue().equals(status)) {
            rows = pauseJob(job);
            flag = "暂停";
        }

        if(rows==0){
            outDto.setAppCode(AOSCons.ERROR);
            msg = "任务失败";
        }
        outDto.setAppMsg(flag+msg);
        httpModel.setOutMsg(AOSJson.toJson(outDto));
    }

    /**
     * 暂停任务
     * @param job
     * @return
     * @throws SchedulerException
     */
    @Transactional
    public int pauseJob(AosJobPO job) throws SchedulerException {
        Long jobId = job.getJob_id();
        String jobGroup = job.getJob_group();
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        int rows = aosJobDao.update(job);
        if (rows > 0)
        {
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    /**
     * 恢复任务
     * @param job
     * @return
     * @throws SchedulerException
     */
    @Transactional
    public int resumeJob(AosJobPO job) throws SchedulerException {
        Long jobId = job.getJob_id();
        String jobGroup = job.getJob_group();
        job.setStatus(ScheduleConstants.Status.NORMAL.getValue());
        int rows = aosJobDao.update(job);
        if (rows > 0)
        {
            scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    /**
     * 更新任务
     *
     * @param job 任务对象
     * @param jobGroup 任务组名
     */
    public void updateSchedulerJob(AosJobPO job, String jobGroup) throws SchedulerException,AOSException {
        Long jobId = job.getJob_id();
        // 判断是否存在
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        if (scheduler.checkExists(jobKey))
        {
            // 防止创建时存在数据问题 先移除，然后在执行创建操作
            scheduler.deleteJob(jobKey);
        }
        ScheduleUtils.createScheduleJob(scheduler, job);
    }
}
