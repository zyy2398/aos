package aos.framework.core.security;

import aos.framework.core.asset.WebCxt;
import aos.framework.core.dao.SqlDao;
import aos.framework.core.exception.AOSException;
import aos.framework.core.typewrap.Dto;
import aos.framework.core.typewrap.Dtos;
import aos.framework.core.utils.AOSCodec;
import aos.framework.core.utils.AOSUtils;
import aos.framework.dao.AosUserDao;
import aos.framework.dao.po.AosUserPO;
import aos.system.common.model.UserModel;
import aos.system.common.utils.ErrorCode;
import aos.system.common.utils.SystemCons;
import aos.system.dao.AosOrgDao;
import aos.system.dao.po.AosOrgPO;
import aos.system.dao.po.AosRolePO;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

public class MyRealm extends AuthorizingRealm {
    @Autowired
    private AosOrgDao aosOrgDao;
    @Autowired
    private SqlDao sqlDao;
    @Autowired
    private AosUserDao aosUserDao;
    /**
     * 授权的方法，每次访问需要权限的接口都会执行
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        UserModel userModel = (UserModel)getAvailablePrincipal(principalCollection);
        if(userModel!=null){
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            Dto qDto = Dtos.newDto();
            qDto.put("user_id", userModel.getId());
            qDto.put("grant_type", SystemCons.GRANT_TYPE.BIZ);
            qDto.put("menu_type", "F");
            List<Dto> moduleList = sqlDao.list("Home.selectModulesOfUser", qDto);
            for(Dto dto : moduleList){
                if (StringUtils.isNotBlank(dto.getString("perms"))){
                    // 添加基于Permission的权限信息
                    for (String permission : StringUtils.split(dto.getString("perms"),",")){
                        info.addStringPermission(permission);
                    }
                }
            }
            // 添加用户角色信息
            for (AosRolePO role : userModel.getAosRolePOs()){
                info.addRole(role.getName());
            }
            return info;
        }else{
            return null;
        }
    }

    /**
     * 认证的方法，登录时执行
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        HttpServletRequest request = AOSUtils.getRequest();
        // token是用户输入的用户名和密码
        // 第一步从token中取出用户名
        UsernamePasswordToken upToken = (UsernamePasswordToken) authenticationToken;
        String account = upToken.getUsername();
        String password = "";
        if (upToken.getPassword() != null)
        {
            password = new String(upToken.getPassword());
        }

        // 帐号存在校验
        Dto qDto = Dtos.newDto("account", account);
        qDto.put("is_del", SystemCons.IS.NO);
        AosUserPO aosUserPO = aosUserDao.selectOne(qDto);
        if (AOSUtils.isEmpty(aosUserPO)) {
            throw new AOSException(ErrorCode.ACCOUNT_ERROR);
        } else {
            // 密码校验
            String jmpassword = AOSCodec.password(password);
            if (!StringUtils.equals(jmpassword, aosUserPO.getPassword())) {
                throw new AOSException(ErrorCode.PASSWORD_ERROR);
            } else {
                // 状态校验
                if (!aosUserPO.getStatus().equals(SystemCons.USER_STATUS.NORMAL)) {
                    throw new AOSException(ErrorCode.LOCKED_ERROR);
                }
            }
        }
        //缓存用户信息
        String juid = UUID.randomUUID().toString();
        UserModel userModel = new UserModel();
        AOSUtils.copyProperties(aosUserPO, userModel);
        userModel.setJuid(juid);
        AosOrgPO aosOrgPO = aosOrgDao.selectByKey(userModel.getOrg_id());
        //查询用户角色列表
        Dto qroleDto = Dtos.newDto("user_id",userModel.getId());
        List<Dto> roleDtos = sqlDao.list("User.listSelectedRoles", qroleDto);
        List<AosRolePO> riles = Lists.newArrayList();
        for(Dto dto : roleDtos){
            AosRolePO aosRolePO = new AosRolePO();
            AOSUtils.copyProperties(dto,aosRolePO);
            riles.add(aosRolePO);
        }
        userModel.setAosOrgPO(aosOrgPO);
        userModel.setAosRolePOs(riles);
        userModel.setLogin_time(AOSUtils.getDateTimeStr());
        userModel.setClient_ip(WebCxt.getClientIpAddr(request));
        userModel.setClient_key(request.getHeader("USER-AGENT"));
        return new SimpleAuthenticationInfo(userModel, password, this.getName());
    }

    //系统登出后 会自动调用以下方法清理授权和认证缓存
    @Override
    public void clearCachedAuthorizationInfo(PrincipalCollection principals) {
        super.clearCachedAuthorizationInfo(principals);
    }

    @Override
    public void clearCachedAuthenticationInfo(PrincipalCollection principals) {
        super.clearCachedAuthenticationInfo(principals);
    }
}
