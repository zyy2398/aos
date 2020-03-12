package aos.system.modules.act;

import aos.framework.core.typewrap.Dto;
import aos.framework.core.typewrap.Dtos;
import aos.framework.core.utils.AOSCons;
import aos.framework.core.utils.AOSJson;
import aos.framework.core.utils.AOSUtils;
import aos.framework.web.router.HttpModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
public class ActivitiProcessService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;

    /**
     * 初始化流程界面
     * @param httpModel
     */
    public void init(HttpModel httpModel){
        httpModel.setAttribute("juid", httpModel.getUserModel().getJuid());
        httpModel.setViewPath("system/process.jsp");
    }

    /**
     * 流程实例列表
     * @param httpModel
     */
    public void listProcess(HttpModel httpModel){
        Dto inDto = httpModel.getInDto();
        String category = inDto.getString("category");
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery()
                .latestVersion().orderByProcessDefinitionKey().asc();

        if (StringUtils.isNotEmpty(category)){
            processDefinitionQuery.processDefinitionCategory(category);
        }

        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.listPage(inDto.getPageStart(),inDto.getPageLimit());
        List<Dto> processList = Lists.newArrayList();
        for (ProcessDefinition processDefinition : processDefinitionList) {
            Dto ProcessDto = Dtos.newDto();
            String deploymentId = processDefinition.getDeploymentId();
            Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentId).singleResult();
            ProcessDto.put("id",processDefinition.getId());
            ProcessDto.put("category",processDefinition.getCategory());
            ProcessDto.put("key",processDefinition.getKey());
            ProcessDto.put("name",processDefinition.getName());
            ProcessDto.put("version",processDefinition.getVersion());
            ProcessDto.put("resourceName",processDefinition.getResourceName());
            ProcessDto.put("diagramResourceName",processDefinition.getDiagramResourceName());
            ProcessDto.put("deploymentTime",deployment.getDeploymentTime());
            ProcessDto.put("suspended",processDefinition.isSuspended());
            ProcessDto.put("deploymentId",processDefinition.getDeploymentId());
            processList.add(ProcessDto);
        }
        httpModel.setOutMsg(AOSJson.toGridJson(processList, processList.size()));
    }

    /**
     * 运行中流程列表
     * @param httpModel
     */
    public void listRunProcess(HttpModel httpModel){
        Dto inDto = httpModel.getInDto();
        String procInsId = inDto.getString("procInsId");
        String procDefKey = inDto.getString("procDefKey");
        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery();

        if (StringUtils.isNotBlank(procInsId)){
            processInstanceQuery.processInstanceId(procInsId);
        }

        if (StringUtils.isNotBlank(procDefKey)){
            processInstanceQuery.processDefinitionKey(procDefKey);
        }
        List<ProcessInstance> list = processInstanceQuery.listPage(inDto.getPageStart(),inDto.getPageLimit());
        List<Dto> dtos = Lists.newArrayList();
        for(ProcessInstance processInstance :list){
            Dto dto = Dtos.newDto();
            dto.put("id",processInstance.getId());
            dto.put("processInstanceId",processInstance.getProcessInstanceId());
            dto.put("processDefinitionId",processInstance.getProcessDefinitionId());
            dto.put("processDefinitionName",processInstance.getProcessDefinitionName());
            dto.put("activityId",processInstance.getActivityId());
            dto.put("suspended",processInstance.isSuspended());
            dtos.add(dto);
        }
        httpModel.setOutMsg(AOSJson.toGridJson(dtos, (int)processInstanceQuery.count()));
    }

    /**
     * 更新流程分类
     * @param httpModel
     */
    public void updateCategory(HttpModel httpModel){
        Dto inDto = httpModel.getInDto();
        String procDefId = inDto.getString("procDefId");
        String category = inDto.getString("category");
        repositoryService.setProcessDefinitionCategory(procDefId, category);
        httpModel.setOutMsg("流程分类修改成功");
    }

    /**
     * 部署流程
     * @param httpModel
     */
    public void deploy(HttpModel httpModel){
        Dto inDto = httpModel.getInDto();
        Dto outDto = Dtos.newDto();
        outDto.setAppCode(AOSCons.SUCCESS);
        String category = inDto.getString("category");
        HttpServletRequest request = httpModel.getRequest();
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        String message = "";

        String fileName = file.getOriginalFilename();

        try {
            InputStream fileInputStream = file.getInputStream();
            Deployment deployment = null;
            String extension = FilenameUtils.getExtension(fileName);
            if (extension.equals("zip") || extension.equals("bar")) {
                ZipInputStream zip = new ZipInputStream(fileInputStream);
                deployment = repositoryService.createDeployment().addZipInputStream(zip).deploy();
            } else if (extension.equals("png")) {
                deployment = repositoryService.createDeployment().addInputStream(fileName, fileInputStream).deploy();
            } else if (fileName.indexOf("bpmn20.xml") != -1) {
                deployment = repositoryService.createDeployment().addInputStream(fileName, fileInputStream).deploy();
            } else if (extension.equals("bpmn")) { // bpmn扩展名特殊处理，转换为bpmn20.xml
                String baseName = FilenameUtils.getBaseName(fileName);
                deployment = repositoryService.createDeployment().addInputStream(baseName + ".bpmn20.xml", fileInputStream).deploy();
            } else {
                message = "不支持的文件类型：" + extension;
                outDto.setAppCode(AOSCons.ERROR);
            }

            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();

            // 设置流程分类
            for (ProcessDefinition processDefinition : list) {
//					ActUtils.exportDiagramToFile(repositoryService, processDefinition, exportDir);
                repositoryService.setProcessDefinitionCategory(processDefinition.getId(), category);
                message += "部署成功，流程ID=" + processDefinition.getId() + "<br/>";
            }

            if (list.size() == 0){
                message = "部署失败，没有流程。";
                outDto.setAppCode(AOSCons.ERROR);
            }
            outDto.setAppMsg(message);

        } catch (Exception e) {
            message = e.toString();
            outDto.setAppCode(AOSCons.ERROR);
            outDto.setAppMsg(message);
        }
        httpModel.setOutMsg(AOSJson.toJson(outDto));
    }

    /**
     * 将部署的流程转换为模型
     * @param httpModel
     */
    public void convertToModel(HttpModel httpModel){
        try {
            String[] selectionIds = httpModel.getInDto().getRows();
            int rows = 0;
            for(String procDefId:selectionIds){
                ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).singleResult();
                InputStream bpmnStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(),
                        processDefinition.getResourceName());
                XMLInputFactory xif = XMLInputFactory.newInstance();
                InputStreamReader in = new InputStreamReader(bpmnStream, "UTF-8");
                XMLStreamReader xtr = xif.createXMLStreamReader(in);
                BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(xtr);

                BpmnJsonConverter converter = new BpmnJsonConverter();
                ObjectNode modelNode = converter.convertToJson(bpmnModel);
                org.activiti.engine.repository.Model modelData = repositoryService.newModel();
                modelData.setKey(processDefinition.getKey());
                modelData.setName(processDefinition.getResourceName());
                modelData.setCategory(processDefinition.getCategory());//.getDeploymentId());
                modelData.setDeploymentId(processDefinition.getDeploymentId());
                modelData.setVersion(Integer.parseInt(String.valueOf(repositoryService.createModelQuery().modelKey(modelData.getKey()).count() + 1)));

                ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
                modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, processDefinition.getName());
                modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
                modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, processDefinition.getDescription());
                modelData.setMetaInfo(modelObjectNode.toString());

                repositoryService.saveModel(modelData);

                repositoryService.addModelEditorSource(modelData.getId(), modelNode.toString().getBytes("utf-8"));
                rows++;
            }
            httpModel.setOutMsg(AOSUtils.merge("操作成功，成功转换[{0}]条流程实例。", rows));
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除流程实例
     * @param httpModel
     */
    public void deleteProcess(HttpModel httpModel){
        String[] selectionIds = httpModel.getInDto().getRows();
        int rows = 0;
        for(String deploymentId:selectionIds){
            repositoryService.deleteDeployment(deploymentId, true);
            rows++;
        }
        httpModel.setOutMsg(AOSUtils.merge("操作成功，成功删除[{0}]条流程实例。", rows));
    }

    /**
     * 删除运行中流程
     * @param httpModel
     */
    public void deleteRunProcess(HttpModel httpModel){
        Dto inDto = httpModel.getInDto();
        String[] ids = inDto.getRows();
        for(String procInsId : ids){
            runtimeService.deleteProcessInstance(procInsId, "客户端删除");
        }
        httpModel.setOutMsg("流程删除成功");
    }

    /**
     * 挂起、激活流程实例
     * @param httpModel
     */
    public void updateState(HttpModel httpModel){
        Dto inDto = httpModel.getInDto();
        String state = inDto.getString("state");
        String procDefId = inDto.getString("procDefId");
        String msg = "";
        if (state.equals("active")) {
            repositoryService.activateProcessDefinitionById(procDefId, true, null);
            msg = "已激活ID为[" + procDefId + "]的流程定义。";
        } else if (state.equals("suspend")) {
            repositoryService.suspendProcessDefinitionById(procDefId, true, null);
            msg = "已挂起ID为[" + procDefId + "]的流程定义。";
        }
        httpModel.setOutMsg(msg);
    }
}
