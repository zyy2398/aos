package aos.system.modules.act;

import aos.framework.core.typewrap.Dto;
import aos.framework.core.typewrap.Dtos;
import aos.framework.core.utils.AOSJson;
import aos.framework.core.utils.AOSUtils;
import aos.framework.web.router.HttpModel;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.util.List;

@Service
public class ActivitiModelService {
    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 初始话模型页面
     * @param httpModel
     */
    public void init(HttpModel httpModel){
        httpModel.setAttribute("juid", httpModel.getUserModel().getJuid());
        httpModel.setViewPath("system/model.jsp");
    }

    /**
     * 模型列表
     * @param httpModel
     */
    public void listModel(HttpModel httpModel){
        Dto qDto = httpModel.getInDto();
        String category = qDto.getString("category");
        ModelQuery modelQuery = repositoryService.createModelQuery().latestVersion().orderByLastUpdateTime().desc();
        if (StringUtils.isNotEmpty(category)){
            modelQuery.modelCategory(category);
        }
        List modelDtos = modelQuery.listPage(qDto.getPageStart(),qDto.getPageLimit());
        httpModel.setOutMsg(AOSJson.toGridJson(modelDtos, modelDtos.size()));
    }

    /**
     * 保存流程模型
     * @param httpModel
     */
    public void saveModel(HttpModel httpModel){
        try {
            Dto outDto = Dtos.newOutDto();
            Dto qDto = httpModel.getInDto();
            String description = qDto.getString("description");
            String key = qDto.getString("key");
            String name = qDto.getString("name");
            String category = qDto.getString("category");

            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("id", "canvas");
            editorNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            Model modelData = repositoryService.newModel();

            description = StringUtils.defaultString(description);
            modelData.setKey(StringUtils.defaultString(key));
            modelData.setName(name);
            modelData.setCategory(category);
            modelData.setVersion(Integer.parseInt(String.valueOf(repositoryService.createModelQuery().modelKey(modelData.getKey()).count()+1)));

            ObjectNode modelObjectNode = objectMapper.createObjectNode();
            modelObjectNode.put(ModelDataJsonConstants.MODEL_NAME, name);
            modelObjectNode.put(ModelDataJsonConstants.MODEL_REVISION, modelData.getVersion());
            modelObjectNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
            modelData.setMetaInfo(modelObjectNode.toString());

            repositoryService.saveModel(modelData);
            repositoryService.addModelEditorSource(modelData.getId(), editorNode.toString().getBytes("utf-8"));
            String id = modelData.getId();
            outDto.put("id",id);
            outDto.setAppMsg("流程模型新增成功");
            httpModel.setOutMsg(AOSJson.toJson(outDto));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 更新流程模型
     * @param httpModel
     */
    public void updateModel(HttpModel httpModel){
        Dto outDto = Dtos.newOutDto();
        Dto qDto = httpModel.getInDto();
        String id = qDto.getString("id");
        String category = qDto.getString("category");
        Model modelData = repositoryService.getModel(id);
        modelData.setCategory(category);
        repositoryService.saveModel(modelData);
        outDto.setAppMsg("流程分类修改成功");
        httpModel.setOutMsg(AOSJson.toJson(outDto));
    }

    /**
     * 删除流程模型
     * @param httpModel
     */
    public void deleteModel(HttpModel httpModel){
        String[] selectionIds = httpModel.getInDto().getRows();
        int rows = 0;
        for (String id : selectionIds) {
            repositoryService.deleteModel(id);
            rows++;
        }
        httpModel.setOutMsg(AOSUtils.merge("操作成功，成功删除[{0}]条流程模型。", rows));
    }

    /**
     * 根据model部署流程
     * @param httpModel
     */
    public void deploy(HttpModel httpModel){
        Dto inDto = httpModel.getInDto();
        String id = inDto.getString("id");
        String message = "";
        try {
            Model modelData = repositoryService.getModel(id);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

            String processName = modelData.getName();
            if (!StringUtils.endsWith(processName, ".bpmn20.xml")){
                processName += ".bpmn20.xml";
            }
//			System.out.println("========="+processName+"============"+modelData.getName());
            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
            Deployment deployment = repositoryService.createDeployment().name(modelData.getName())
                    .addInputStream(processName, in).deploy();
//					.addString(processName, new String(bpmnBytes)).deploy();

            // 设置流程分类
            List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).list();
            for (ProcessDefinition processDefinition : list) {
                repositoryService.setProcessDefinitionCategory(processDefinition.getId(), modelData.getCategory());
                message = "部署成功，流程ID=" + processDefinition.getId();
            }
            if (list.size() == 0){
                message = "部署失败，没有流程。";
            }
        } catch (Exception e) {
            throw new ActivitiException("设计模型图不正确，检查模型正确性，模型ID="+id, e);
        }
        httpModel.setOutMsg(message);
    }

    /**
     * 导出xml文件
     * @param httpModel
     */
    public void export(HttpModel httpModel){
        HttpServletResponse response = httpModel.getResponse();
        Dto inDto = httpModel.getInDto();
        String id = inDto.getString("id");
        try {
            Model modelData = repositoryService.getModel(id);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = new ObjectMapper().readTree(repositoryService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);
            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
            IOUtils.copy(in, response.getOutputStream());
            String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.xml";
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.flushBuffer();
        } catch (Exception e) {
            throw new ActivitiException("导出model的xml文件失败，模型ID="+id, e);
        }
    }
}
