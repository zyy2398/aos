package aos.system.modules.act.utils;

import aos.framework.core.dao.SqlDao;
import aos.framework.core.typewrap.Dto;
import aos.framework.core.typewrap.Dtos;
import aos.framework.dao.AosUserDao;
import aos.framework.dao.po.AosUserPO;
import com.google.common.collect.Lists;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomGroupEntityManager extends GroupEntityManager {
    @Resource
    private SqlDao sqlDao;
    @Resource
    private AosUserDao aosUserDao;

    public boolean hasUser(String userId) {
        AosUserPO aosUserPO = aosUserDao.selectByKey(Integer.valueOf(userId));
        if(aosUserPO==null){
            return false;
        }
        return true;
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        System.out.println("CustomUserEntityManager  findGroupsByUser userId:" + userId);
        if (userId == null)
            return null;
        boolean hasUser = hasUser(userId);
        if (!hasUser)return null;
        List<Map<String,Object>> roleList = getRoleList(userId);
        List<Group> groupEntitys = new ArrayList<Group>();
        if(null != roleList) {
            for (Map<String, Object> role : roleList) {
                String roleCode = null == role.get("roleCode")?"":role.get("roleCode").toString();
                String roleName = null == role.get("roleName")?"":role.get("roleName").toString();
                GroupEntity groupEntity = toActivitiGroup(roleCode,roleName);
                groupEntitys.add(groupEntity);
            }
        }
        return groupEntitys;
    }

    public static GroupEntity toActivitiGroup(String roleCode,String roleName) {
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setRevision(1);
        groupEntity.setType("assignment");
        groupEntity.setId(roleCode);
        groupEntity.setName(roleName);
        return groupEntity;
    }

    public List<Map<String, Object>> getRoleList(String userId) {
        List<Map<String,Object>> roleList = Lists.newArrayList();
        Dto qDto = Dtos.newDto();
        qDto.put("user_id", userId);
        List<Dto> roleDtos = sqlDao.list("User.listSelectedRoles", qDto);
        for (Dto dto : roleDtos) {
            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("roleCode", dto.getString("id"));
            roleMap.put("roleName", dto.getString("name"));
            roleList.add(roleMap);
        }
        return roleList;
    }
}
