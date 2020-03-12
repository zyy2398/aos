package aos.system.modules.act.utils;

import aos.framework.core.dao.SqlDao;
import aos.framework.core.typewrap.Dto;
import aos.framework.core.typewrap.Dtos;
import aos.framework.dao.AosUserDao;
import aos.framework.dao.po.AosUserPO;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.springframework.dao.EmptyResultDataAccessException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 覆盖activiti用户管理，实现系统自带用户管理
 */

public class CustomUserEntityManager extends UserEntityManager {

    @Resource
    private AosUserDao aosUserDao;
    @Resource
    private SqlDao sqlDao;

    @Override
    public User findUserById(String userId) {
        System.out.println("CustomUserEntityManager  findUserById userId:" + userId);
        if (userId == null)
            return null;
        try {
            UserEntity userEntity = new UserEntity();
            boolean hasUser = hasUser(userId);
            if (!hasUser)return null;

            userEntity.setId(userId);
            userEntity.setFirstName(getUserName(userId));
            userEntity.setPassword(getUserPassword(userId));
            userEntity.setEmail(getUserEmail(userId));
            userEntity.setRevision(1);
            return userEntity;
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean hasUser(String userId) {
        AosUserPO aosUserPO = aosUserDao.selectOne(Dtos.newDto("account",userId));
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

    public static GroupEntity toActivitiGroup(String roleCode, String roleName) {
        GroupEntity groupEntity = new GroupEntity();
        groupEntity.setRevision(1);
        groupEntity.setType("assignment");
        groupEntity.setId(roleCode);
        groupEntity.setName(roleName);
        return groupEntity;
    }

    public String getUserName(String userId) {
        AosUserPO aosUserPO = aosUserDao.selectOne(Dtos.newDto("account",userId));
        String name = aosUserPO.getName();
        return name;
    }

    public String getUserPassword(String userId) {
        AosUserPO aosUserPO = aosUserDao.selectOne(Dtos.newDto("account",userId));
        String password = aosUserPO.getPassword();
        return password;
    }

    public String getUserEmail(String userId) {
        AosUserPO aosUserPO = aosUserDao.selectOne(Dtos.newDto("account",userId));
        String email = aosUserPO.getEmail();
        return email;
    }

    public List<Map<String, Object>> getRoleList(String userId) {
        List<Map<String,Object>> roleList = new ArrayList<>();
        Dto qDto = Dtos.newDto();
        qDto.put("account", userId);
        List<Dto> roleDtos = sqlDao.list("User.listSelectedRoles", qDto);
        for (Dto dto : roleDtos) {
          Map<String, Object> roleMap = new HashMap<>();
          roleMap.put("roleCode", dto.getString("biz_code"));
          roleMap.put("roleName", dto.getString("name"));
          roleList.add(roleMap);
        }
        return roleList;
    }

}
