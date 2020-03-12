/**
 * 任务管理服务类
 * papio 2019/12/17
 */
package aos.system.modules.act;

import aos.framework.core.dao.SqlDao;
import aos.framework.core.typewrap.Dto;
import aos.framework.core.typewrap.Dtos;
import aos.framework.core.utils.AOSCxt;
import aos.framework.core.utils.AOSJson;
import aos.framework.core.utils.TimeUtils;
import aos.framework.dao.AosUserDao;
import aos.framework.dao.po.AosUserPO;
import aos.framework.web.router.HttpModel;
import aos.system.common.model.UserModel;
import aos.system.dao.po.AosRolePO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.*;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.spring.ProcessEngineFactoryBean;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ActivitiMyTaskService {

    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private IdentityService identityService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private FormService formService;
    @Autowired
    private ProcessEngineFactoryBean processEngineFactoryBean;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private SqlDao sqlDao;
    @Autowired
    private AosUserDao aosUserDao;

    private static final Logger logger = LoggerFactory.getLogger(ActivitiMyTaskService.class);
    /**
     * 初始化页面
     * @param httpModel
     */
    public void init(HttpModel httpModel){
        httpModel.setViewPath("system/task.jsp");
    }

    /**
     * 待办事项列表
     * @param httpModel
     */
    public void listTodoTask(HttpModel httpModel){
        Dto inDto = httpModel.getInDto();
        String procDefKey = inDto.getString("procDefKey");
        Date beginDate = inDto.getDate("beginDate");
        Date endDate = inDto.getDate("endDate");
        String userId = httpModel.getUserModel().getAccount();
        //ObjectUtils.toString(UserUtils.getUser().getId());

        List<Dto> result = Lists.newArrayList();

        // =============== 已经签收的任务  ===============
        TaskQuery todoTaskQuery = taskService.createTaskQuery().taskAssignee(userId).active()
                .includeProcessVariables().orderByTaskCreateTime().desc();

        // 设置查询条件
        if (StringUtils.isNotBlank(procDefKey)){
            todoTaskQuery.processDefinitionKey(procDefKey);
        }
        if (beginDate != null){
            todoTaskQuery.taskCreatedAfter(beginDate);
        }
        if (endDate != null){
            todoTaskQuery.taskCreatedBefore(endDate);
        }

        // 查询列表
        List<Task> todoList = todoTaskQuery.list();
        for (Task task : todoList) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
            Dto e = Dtos.newDto();
            e.put("title",task.getProcessVariables().get("title"));
            e.put("taskNameLabel",task.getName());
            e.put("description",task.getProcessVariables().get("description"));
            e.put("processName",pd.getName());
            e.put("create_time",task.getCreateTime());
            e.put("processInstanceId",task.getProcessInstanceId());
            e.put("assignee",task.getAssignee());
            e.put("task_id",task.getId());
            e.put("businessKey",processInstance.getBusinessKey());
            e.put("form_path",getFormKey(pd.getId(), task.getTaskDefinitionKey()));
            //e.put("task",task);
            //e.put("vars",task.getProcessVariables());
//			e.setTaskVars(task.getTaskLocalVariables());
//			System.out.println(task.getId()+"  =  "+task.getProcessVariables() + "  ========== " + task.getTaskLocalVariables());
            //e.put("procDef",pd);
//			e.setProcIns(runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult());
//			e.setProcExecUrl(ActUtils.getProcExeUrl(task.getProcessDefinitionId()));
            e.put("status","todo");
            result.add(e);
        }

        // =============== 等待签收的任务  ===============
        TaskQuery toClaimQuery = taskService.createTaskQuery().taskCandidateUser(userId)
                .includeProcessVariables().active().orderByTaskCreateTime().desc();

        // 设置查询条件
        if (StringUtils.isNotBlank(procDefKey)){
            toClaimQuery.processDefinitionKey(procDefKey);
        }
        if (beginDate != null){
            toClaimQuery.taskCreatedAfter(beginDate);
        }
        if (endDate != null){
            toClaimQuery.taskCreatedBefore(endDate);
        }

        // 查询列表
        List<Task> toClaimList = toClaimQuery.list();
        for (Task task : toClaimList) {
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
            Dto e = Dtos.newDto();
            e.put("title",task.getProcessVariables().get("title"));
            e.put("taskNameLabel",task.getName());
            e.put("description",task.getProcessVariables().get("description"));
            e.put("processName",pd.getName());
            e.put("create_time",task.getCreateTime());
            e.put("processInstanceId",task.getProcessInstanceId());
            e.put("assignee",task.getAssignee());
            e.put("task_id",task.getId());
            e.put("businessKey",processInstance.getBusinessKey());
            e.put("form_path",getFormKey(pd.getId(), task.getTaskDefinitionKey()));
            //e.put("task",task);
            //e.put("vars",task.getProcessVariables());
//			e.setTaskVars(task.getTaskLocalVariables());
//			System.out.println(task.getId()+"  =  "+task.getProcessVariables() + "  ========== " + task.getTaskLocalVariables());
            //e.put("procDef",pd);
//			e.setProcIns(runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult());
//			e.setProcExecUrl(ActUtils.getProcExeUrl(task.getProcessDefinitionId()));
            e.put("status","claim");
            result.add(e);
        }
        httpModel.setOutMsg(AOSJson.toGridJson(result, result.size()));
    }

    /**
     * 已办事项列表
     * @param httpModel
     */
    public void listHistoricTask(HttpModel httpModel){
        Dto inDto = httpModel.getInDto();
        String procDefKey = inDto.getString("procDefKey");
        Date beginDate = inDto.getDate("beginDate");
        Date endDate = inDto.getDate("endDate");
        String userId = httpModel.getUserModel().getAccount();
        //ObjectUtils.toString(UserUtils.getUser().getId());

        HistoricTaskInstanceQuery histTaskQuery = historyService.createHistoricTaskInstanceQuery().taskAssignee(userId).finished()
                .includeProcessVariables().orderByHistoricTaskInstanceEndTime().desc();

        // 设置查询条件
        if (StringUtils.isNotBlank(procDefKey)){
            histTaskQuery.processDefinitionKey(procDefKey);
        }
        if (beginDate != null){
            histTaskQuery.taskCompletedAfter(beginDate);
        }
        if (endDate != null){
            histTaskQuery.taskCompletedBefore(endDate);
        }

        // 查询列表
        List<HistoricTaskInstance> histList = histTaskQuery.listPage(inDto.getPageStart(),inDto.getPageLimit());
        //处理分页问题
        List<Dto> actList= Lists.newArrayList();
        for (HistoricTaskInstance histTask : histList) {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(histTask.getProcessInstanceId()).singleResult();
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(histTask.getProcessDefinitionId()).singleResult();
            Dto e = Dtos.newDto();
            e.put("title",histTask.getProcessVariables().get("title"));
            e.put("taskNameLabel",histTask.getName());
            e.put("description",histTask.getProcessVariables().get("description"));
            e.put("processName",pd.getName());
            e.put("end_time",histTask.getEndTime());
            e.put("processInstanceId",histTask.getProcessInstanceId());
            e.put("businessKey",historicProcessInstance.getBusinessKey());
            e.put("form_path",getFormKey(pd.getId(), histTask.getTaskDefinitionKey()));
            //e.put("histTask",histTask);
            //e.put("vars",histTask.getProcessVariables());
//			e.setTaskVars(histTask.getTaskLocalVariables());
//			System.out.println(histTask.getId()+"  =  "+histTask.getProcessVariables() + "  ========== " + histTask.getTaskLocalVariables());
            //e.put("procDef",pd);
//			e.setProcIns(runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult());
//			e.setProcExecUrl(ActUtils.getProcExeUrl(task.getProcessDefinitionId()));
            e.put("status","finish");
            actList.add(e);
            //page.getList().add(e);
        }
        httpModel.setOutMsg(AOSJson.toGridJson(actList, actList.size()));
    }

    /**
     * 获取流转历史列表
     */
    public void histoicFlowList(HttpModel httpModel){
        Dto inDto = httpModel.getInDto();
        String procInsId =  inDto.getString("procInsId");
        String startAct = inDto.getString("startAct");
        String endAct = inDto.getString("endAct");
        List<Dto> actList = Lists.newArrayList();
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery().processInstanceId(procInsId)
                .orderByHistoricActivityInstanceStartTime().asc().orderByHistoricActivityInstanceEndTime().asc().list();

        boolean start = false;
        Map<String, Integer> actMap = Maps.newHashMap();

        for (int i=0; i<list.size(); i++){

            HistoricActivityInstance histIns = list.get(i);

            // 过滤开始节点前的节点
            if (StringUtils.isNotBlank(startAct) && startAct.equals(histIns.getActivityId())){
                start = true;
            }
            if (StringUtils.isNotBlank(startAct) && !start){
                continue;
            }

            // 只显示开始节点和结束节点，并且执行人不为空的任务
            if (StringUtils.isNotBlank(histIns.getAssignee())
                    || "startEvent".equals(histIns.getActivityType())
                    || "endEvent".equals(histIns.getActivityType())){

                // 给节点增加一个序号
                Integer actNum = actMap.get(histIns.getActivityId());
                if (actNum == null){
                    actMap.put(histIns.getActivityId(), actMap.size());
                }

                Dto e = Dtos.newDto();
                String durationTime = "";
                if (histIns!=null && histIns.getDurationInMillis() != null){
                    durationTime = TimeUtils.toTimeString(histIns.getDurationInMillis());
                }
                e.put("activityName",histIns.getActivityName());
                e.put("startTime",histIns.getStartTime());
                e.put("endTime",histIns.getEndTime());
                e.put("durationTime",durationTime);
                // 获取流程发起人名称
                if ("startEvent".equals(histIns.getActivityType())){
                    List<HistoricProcessInstance> il = historyService.createHistoricProcessInstanceQuery().processInstanceId(procInsId).orderByProcessInstanceStartTime().asc().list();
//					List<HistoricIdentityLink> il = historyService.getHistoricIdentityLinksForProcessInstance(procInsId);
                    if (il.size() > 0){
                        if (StringUtils.isNotBlank(il.get(0).getStartUserId())){
                            AosUserPO user = aosUserDao.selectOne(Dtos.newDto("account",il.get(0).getStartUserId()));
                            if (user != null){
                                e.put("assignee",histIns.getAssignee());
                                e.put("assigneeName",user.getName());
                            }
                        }
                    }
                }
                // 获取任务执行人名称
                if (StringUtils.isNotEmpty(histIns.getAssignee())){
                    AosUserPO user = aosUserDao.selectOne(Dtos.newDto("account",histIns.getAssignee()));
                    if (user != null){
                        e.put("assignee",histIns.getAssignee());
                        e.put("assigneeName",user.getName());
                    }
                }
                // 获取意见评论内容
                if (StringUtils.isNotBlank(histIns.getTaskId())){
                    List<Comment> commentList = taskService.getTaskComments(histIns.getTaskId());
                    if (commentList.size()>0){
                        e.put("comment",commentList.get(0).getFullMessage());
                    }
                }
                actList.add(e);
            }

            // 过滤结束节点后的节点
            if (StringUtils.isNotBlank(endAct) && endAct.equals(histIns.getActivityId())){
                boolean bl = false;
                Integer actNum = actMap.get(histIns.getActivityId());
                // 该活动节点，后续节点是否在结束节点之前，在后续节点中是否存在
                for (int j=i+1; j<list.size(); j++){
                    HistoricActivityInstance hi = list.get(j);
                    Integer actNumA = actMap.get(hi.getActivityId());
                    if ((actNumA != null && actNumA < actNum) || StringUtils.equals(hi.getActivityId(), histIns.getActivityId())){
                        bl = true;
                    }
                }
                if (!bl){
                    break;
                }
            }
        }
        httpModel.setOutMsg(AOSJson.toGridJson(actList));
    }

    /**
     * 获取流程表单（首先获取任务节点表单KEY，如果没有则取流程开始节点表单KEY）
     * @return
     */
    public String getFormKey(String procDefId, String taskDefKey){
        String formKey = "";
        if (StringUtils.isNotBlank(procDefId)){
            if (StringUtils.isNotBlank(taskDefKey)){
                try{
                    formKey = formService.getTaskFormKey(procDefId, taskDefKey);
                }catch (Exception e) {
                    formKey = "";
                }
            }
            if (StringUtils.isBlank(formKey)){
                formKey = formService.getStartFormKey(procDefId);
            }
            if (StringUtils.isBlank(formKey)){
                formKey = "/WEB-INF/jsp/common/404";
            }
        }
        logger.debug("getFormKey: {}", formKey);
        return formKey;
    }

    /**
     * 获取流程实例对象
     * @param procInsId
     * @return
     */
    @Transactional
    public HistoricProcessInstance getProcIns(String procInsId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(procInsId).singleResult();
        //return runtimeService.createProcessInstanceQuery().processInstanceId(procInsId).singleResult();
    }

    /**
     * 启动流程
     * @param procDefKey 流程定义KEY
     * @param businessTable 业务表表名
     * @param businessId	业务表编号
     * @return 流程实例ID
     */
    @Transactional
    public String startProcess(String procDefKey, String businessTable, String businessId) {
        return startProcess(procDefKey, businessTable, businessId, "");
    }

    /**
     * 启动流程
     * @param procDefKey 流程定义KEY
     * @param businessTable 业务表表名
     * @param businessId	业务表编号
     * @param title			流程标题，显示在待办任务标题
     * @return 流程实例ID
     */
    @Transactional
    public String startProcess(String procDefKey, String businessTable, String businessId, String title) {
        Dto vars = Dtos.newDto();
        return startProcess(procDefKey, businessTable, businessId, title, vars);
    }

    /**
     * 启动流程
     * @param procDefKey 流程定义KEY
     * @param businessTable 业务表表名
     * @param businessId	业务表编号
     * @param title			流程标题，显示在待办任务标题
     * @param vars			流程变量
     * @return 流程实例ID
     */
    @Transactional
    public String startProcess(String procDefKey, String businessTable, String businessId, String title, Dto vars) {
        String userId = AOSCxt.getUserModel().getAccount();
        // 用来设置启动流程的人员ID，引擎会自动把用户ID保存到activiti:initiator中
        identityService.setAuthenticatedUserId(userId);
        // 设置流程变量
        if (vars == null){
            vars = Dtos.newDto();
        }
        // 设置流程标题
        if (StringUtils.isNotBlank(title)){
            vars.put("title", title);
        }
        // 启动流程
        ProcessInstance procIns = runtimeService.startProcessInstanceByKey(procDefKey, businessId, vars);
        // 更新业务表流程实例ID
        Dto dto = Dtos.newDto();
        dto.put("businessTable",businessTable);
        dto.put("businessId",businessId);
        dto.put("procInsId",procIns.getId());
        sqlDao.update("ActivitiMyTask.updateProcInsIdByBusinessId",dto);
        return procIns.getId();
    }

    /**
     * 获取任务
     * @param taskId 任务ID
     */
    public Task getTask(String taskId){
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    /**
     * 当前任务节点执行人是否为当前用户
     * @param taskId
     * @return
     */
    public boolean isExecutor(String taskId){
        UserModel user = AOSCxt.getUserModel();
        Task task = getTask(taskId);
        if(StringUtils.equals(task.getAssignee(),user.getAccount())){
            return true;
        }
        ProcessDefinitionEntity processDefinitionEntity=(ProcessDefinitionEntity) processEngine.getRepositoryService()
                .getProcessDefinition(task.getProcessDefinitionId());
        ActivityImpl activityImpl=processDefinitionEntity.findActivity(task.getTaskDefinitionKey()); // 根据活动id获取活动实例
        TaskDefinition taskDef = (TaskDefinition)activityImpl.getProperties().get("taskDefinition");
        Set<Expression> userCodes = taskDef.getCandidateUserIdExpressions();//候选人
        Set<Expression> roleCodes = taskDef.getCandidateGroupIdExpressions();//候选组
        for (Expression str : userCodes) {
            if(StringUtils.equals(str.getExpressionText(),user.getAccount())){
                return true;
            }
        }
        for (Expression str : roleCodes) {
            for(AosRolePO role:user.getAosRolePOs()){
                if(StringUtils.equals(str.getExpressionText(),role.getBiz_code())){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 删除任务
     * @param taskId 任务ID
     * @param deleteReason 删除原因
     */
    @Transactional
    public void deleteTask(String taskId, String deleteReason){
        taskService.deleteTask(taskId, deleteReason);
    }

    /**
     * 提交任务, 并保存意见
     * @param taskId 任务ID
     * @param procInsId 流程实例ID，如果为空，则不保存任务提交意见
     * @param comment 任务提交意见的内容
     * @param vars 任务变量
     */
    @Transactional
    public void complete(String taskId, String procInsId, String comment, Map<String, Object> vars){
        complete(taskId, procInsId, comment, "", vars);
    }

    /**
     * 提交任务, 并保存意见
     * @param taskId 任务ID
     * @param procInsId 流程实例ID，如果为空，则不保存任务提交意见
     * @param comment 任务提交意见的内容
     * @param title			流程标题，显示在待办任务标题
     * @param vars 任务变量
     */
    @Transactional
    public void complete(String taskId, String procInsId, String comment, String title, Map<String, Object> vars){
        // 添加意见
        if (StringUtils.isNotBlank(procInsId) && StringUtils.isNotBlank(comment)){
            taskService.addComment(taskId, procInsId, comment);
        }

        // 设置流程变量
        if (vars == null){
            vars = Maps.newHashMap();
        }

        // 设置流程标题
        if (StringUtils.isNotBlank(title)){
            vars.put("title", title);
        }
        // 签收任务
        taskService.claim(taskId, AOSCxt.getUserModel().getAccount());
        // 提交任务
        taskService.complete(taskId, vars);
    }

    /**
     * 完成第一个任务
     * @param procInsId
     */
    @Transactional
    public void completeFirstTask(String procInsId){
        completeFirstTask(procInsId, null, null, null);
    }

    /**
     * 完成第一个任务
     * @param procInsId
     * @param comment
     * @param title
     * @param vars
     */
    @Transactional
    public void completeFirstTask(String procInsId, String comment, String title, Map<String, Object> vars){
        String userId = AOSCxt.getUserModel().getAccount();
        Task task = taskService.createTaskQuery().taskAssignee(userId).processInstanceId(procInsId).active().singleResult();
        if (task != null){
            complete(task.getId(), procInsId, comment, title, vars);
        }
    }

//	/**
//	 * 委派任务
//	 * @param taskId 任务ID
//	 * @param userId 被委派人
//	 */
//	public void delegateTask(String taskId, String userId){
//		taskService.delegateTask(taskId, userId);
//	}
//
//	/**
//	 * 被委派人完成任务
//	 * @param taskId 任务ID
//	 */
//	public void resolveTask(String taskId){
//		taskService.resolveTask(taskId);
//	}
//
//	/**
//	 * 回退任务
//	 * @param taskId
//	 */
//	public void backTask(String taskId){
//		taskService.
//	}

    ////////////////////////////////////////////////////////////////////
    /**
     * 获取流程图像，已执行节点和流程线高亮显示
     */
    public void getActivitiProccessImage(HttpModel httpModel) {
        Dto inDto = httpModel.getInDto();
        String pProcessInstanceId = inDto.getString("pProcessInstanceId");
        HttpServletResponse response = httpModel.getResponse();
        //logger.info("[开始]-获取流程图图像");
        try {
            //  获取历史流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(pProcessInstanceId).singleResult();

            if (historicProcessInstance == null) {
                //throw new BusinessException("获取流程实例ID[" + pProcessInstanceId + "]对应的历史流程实例失败！");
            }
            else {
                // 获取流程定义
                ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)
                        .getDeployedProcessDefinition(historicProcessInstance.getProcessDefinitionId());

                // 获取流程历史中已执行节点，并按照节点在流程中执行先后顺序排序
                List<HistoricActivityInstance> historicActivityInstanceList = historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(pProcessInstanceId).orderByHistoricActivityInstanceId().asc().list();

                // 已执行的节点ID集合
                List<String> executedActivityIdList = new ArrayList<String>();
                int index = 1;
                //logger.info("获取已经执行的节点ID");
                for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                    executedActivityIdList.add(activityInstance.getActivityId());

                    //logger.info("第[" + index + "]个已执行节点=" + activityInstance.getActivityId() + " : " +activityInstance.getActivityName());
                    index++;
                }

                BpmnModel bpmnModel = repositoryService.getBpmnModel(historicProcessInstance.getProcessDefinitionId());

                // 已执行的线集合
                List<String> flowIds = new ArrayList<String>();
                // 获取流程走过的线 (getHighLightedFlows是下面的方法)
                flowIds = getHighLightedFlows(bpmnModel,processDefinition, historicActivityInstanceList);

                // 获取流程图图像字符流
                ProcessDiagramGenerator pec = processEngineFactoryBean.getProcessEngineConfiguration().getProcessDiagramGenerator();
                //配置字体
                InputStream imageStream = pec.generateDiagram(bpmnModel, "png", executedActivityIdList, flowIds,"宋体","微软雅黑",null,2.0);

                response.setContentType("image/png");
                OutputStream os = response.getOutputStream();
                int bytesRead = 0;
                byte[] buffer = new byte[8192];
                while ((bytesRead = imageStream.read(buffer, 0, 8192)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.close();
                imageStream.close();
            }
            //logger.info("[完成]-获取流程图图像");
        } catch (Exception e) {
            System.out.println(e.getMessage());
            //logger.error("【异常】-获取流程图失败！" + e.getMessage());
            //throw new BusinessException("获取流程图失败！" + e.getMessage());
        }
    }

    public List<String> getHighLightedFlows(BpmnModel bpmnModel, ProcessDefinitionEntity processDefinitionEntity, List<HistoricActivityInstance> historicActivityInstances) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //24小时制
        List<String> highFlows = new ArrayList<String>();// 用以保存高亮的线flowId

        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {
            // 对历史流程节点进行遍历
            // 得到节点定义的详细信息
            FlowNode activityImpl = (FlowNode)bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(i).getActivityId());


            List<FlowNode> sameStartTimeNodes = new ArrayList<FlowNode>();// 用以保存后续开始时间相同的节点
            FlowNode sameActivityImpl1 = null;

            HistoricActivityInstance activityImpl_ = historicActivityInstances.get(i);// 第一个节点
            HistoricActivityInstance activityImp2_ ;

            for(int k = i + 1 ; k <= historicActivityInstances.size() - 1; k++) {
                activityImp2_ = historicActivityInstances.get(k);// 后续第1个节点

                if ( activityImpl_.getActivityType().equals("userTask") && activityImp2_.getActivityType().equals("userTask") &&
                        df.format(activityImpl_.getStartTime()).equals(df.format(activityImp2_.getStartTime()))   ) //都是usertask，且主节点与后续节点的开始时间相同，说明不是真实的后继节点
                {

                }
                else {
                    sameActivityImpl1 = (FlowNode)bpmnModel.getMainProcess().getFlowElement(historicActivityInstances.get(k).getActivityId());//找到紧跟在后面的一个节点
                    break;
                }

            }
            sameStartTimeNodes.add(sameActivityImpl1); // 将后面第一个节点放在时间相同节点的集合里
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点

                if (df.format(activityImpl1.getStartTime()).equals(df.format(activityImpl2.getStartTime()))  )
                {// 如果第一个节点和第二个节点开始时间相同保存
                    FlowNode sameActivityImpl2 = (FlowNode)bpmnModel.getMainProcess().getFlowElement(activityImpl2.getActivityId());
                    sameStartTimeNodes.add(sameActivityImpl2);
                }
                else
                {// 有不相同跳出循环
                    break;
                }
            }
            List<SequenceFlow> pvmTransitions = activityImpl.getOutgoingFlows() ; // 取出节点的所有出去的线

            for (SequenceFlow pvmTransition : pvmTransitions)
            {// 对所有的线进行遍历
                FlowNode pvmActivityImpl = (FlowNode)bpmnModel.getMainProcess().getFlowElement( pvmTransition.getTargetRef());// 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }

        }
        return highFlows;

    }
}
