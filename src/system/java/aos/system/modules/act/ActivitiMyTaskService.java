/**
 * 任务管理服务类
 * papio 2019/12/17
 */
package aos.system.modules.act;

import aos.framework.core.typewrap.Dto;
import aos.framework.core.typewrap.Dtos;
import aos.framework.core.utils.AOSJson;
import aos.framework.web.router.HttpModel;
import com.google.common.collect.Lists;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ActivitiMyTaskService {

    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private RepositoryService repositoryService;

    /**
     * 初始化页面
     * @param httpModel
     */
    public void init(HttpModel httpModel){
        httpModel.setAttribute("juid", httpModel.getUserModel().getJuid());
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
        String userId = httpModel.getUserModel().getId().toString();
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
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
            Dto e = Dtos.newDto();
            e.put("task",task);
            e.put("vars",task.getProcessVariables());
//			e.setTaskVars(task.getTaskLocalVariables());
//			System.out.println(task.getId()+"  =  "+task.getProcessVariables() + "  ========== " + task.getTaskLocalVariables());
            e.put("procDef",pd);
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
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(task.getProcessDefinitionId()).singleResult();
            Dto e = Dtos.newDto();
            e.put("task",task);
            e.put("vars",task.getProcessVariables());
//			e.setTaskVars(task.getTaskLocalVariables());
//			System.out.println(task.getId()+"  =  "+task.getProcessVariables() + "  ========== " + task.getTaskLocalVariables());
            e.put("procDef",pd);
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
        String userId = httpModel.getUserModel().getId().toString();
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
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery().processDefinitionId(histTask.getProcessDefinitionId()).singleResult();
            Dto e = Dtos.newDto();
            e.put("histTask",histTask);
            e.put("vars",histTask.getProcessVariables());
//			e.setTaskVars(histTask.getTaskLocalVariables());
//			System.out.println(histTask.getId()+"  =  "+histTask.getProcessVariables() + "  ========== " + histTask.getTaskLocalVariables());
            e.put("procDef",pd);
//			e.setProcIns(runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult());
//			e.setProcExecUrl(ActUtils.getProcExeUrl(task.getProcessDefinitionId()));
            e.put("status","finish");
            actList.add(e);
            //page.getList().add(e);
        }
        httpModel.setOutMsg(AOSJson.toGridJson(actList, actList.size()));
    }

}
