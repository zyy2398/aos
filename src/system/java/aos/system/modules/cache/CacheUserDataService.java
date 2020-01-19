package aos.system.modules.cache;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import aos.framework.core.dao.SqlDao;
import aos.system.dao.po.AosRolePO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.crazycake.shiro.RedisSessionDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

import aos.framework.core.asset.WebCxt;
import aos.framework.core.exception.AOSException;
import aos.framework.core.redis.JedisUtil;
import aos.framework.core.typewrap.Dto;
import aos.framework.core.typewrap.Dtos;
import aos.framework.core.utils.AOSCons;
import aos.framework.core.utils.AOSCxt;
import aos.framework.core.utils.AOSJson;
import aos.framework.core.utils.AOSUtils;
import aos.framework.dao.po.AosUserPO;
import aos.system.common.model.UserModel;
import aos.system.common.utils.SystemCons;
import aos.system.dao.AosOrgDao;
import aos.system.dao.po.AosOrgPO;
import redis.clients.jedis.Jedis;

/**
 * <b>用户数据缓存服务</b>
 * 
 * @author xiongchun
 */
@Service
public class CacheUserDataService {

	private static Logger logger = LoggerFactory.getLogger(CacheUserDataService.class);

	@Autowired
	private AosOrgDao aosOrgDao;
	@Autowired
	private SqlDao sqlDao;
	@Autowired
	private RedisSessionDAO redisSessionDAO;
	/**
	 * 将用户信息刷到缓存
	 * 
	 * @param userModel
	 */
	public void cacheUserModel(UserModel userModel) {
		if (AOSUtils.isEmpty(userModel.getJuid())) {
			throw new AOSException("JUID不能为空");
		}
		AosOrgPO aosOrgPO = aosOrgDao.selectByKey(userModel.getOrg_id());
		userModel.setAosOrgPO(aosOrgPO);
		//查询用户角色列表
		Dto qDto = Dtos.newDto("user_id",userModel.getId());
		List<Dto> roleDtos = sqlDao.list("User.listSelectedRoles", qDto);
		List<AosRolePO> riles = Lists.newArrayList();
		for(Dto dto : roleDtos){
			AosRolePO aosRolePO = new AosRolePO();
			AOSUtils.copyProperties(dto,aosRolePO);
			riles.add(aosRolePO);
		}
		userModel.setAosRolePOs(riles);
		Subject subject = SecurityUtils.getSubject();
		PrincipalCollection principalCollection = subject.getPrincipals();
		String realmName = principalCollection.getRealmNames().iterator().next();
		PrincipalCollection newPrincipalCollection = new SimplePrincipalCollection(userModel, realmName);
		subject.runAs(newPrincipalCollection);
	}

	/**
	 * 将用户信息从缓存中重置
	 * 
	 * @param juid
	 */
	public void resetUserModel(String juid) {
		if (AOSUtils.isNotEmpty(juid)) {
			Collection<Session> sessions = redisSessionDAO.getActiveSessions();
			for(Session session:sessions){
				SimplePrincipalCollection principalCollection = (SimplePrincipalCollection) session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
				UserModel userModel = (UserModel) principalCollection.getPrimaryPrincipal();
				if(juid.equals(userModel.getJuid())) {
					redisSessionDAO.delete(session);
					break;
				}
			}
		}
	}
	
	/**
	 * 将用户授权信息从缓存中重置(卡片和卡片内导航菜单信息)
	 * 
	 * @param userId
	 */
	public void resetGrantInfoOfUser(Integer userId) {
		if (AOSUtils.isNotEmpty(userId)) {
			String cardKey = SystemCons.KEYS.CARDLIST + userId;
			String cardListJson = JedisUtil.getString(cardKey);
			List<Dto> cardList = AOSJson.fromJson(cardListJson);
			if (AOSUtils.isNotEmpty(cardList)) {
				for (Dto dto : cardList) {
					//卡片内部的导航树
					JedisUtil.delString(SystemCons.KEYS.CARD_TREE + userId + "." + dto.getString("cascade_id"));
				}
			}
			//卡片
			JedisUtil.delString(cardKey);

		}
	}

	/**
	 * 从缓存中获取用户信息
	 *
	 * @return
	 */
	public UserModel getUserModel() {
		Subject subject = SecurityUtils.getSubject();
		UserModel userModel = (UserModel)subject.getPrincipal();
		if (userModel != null){
			return userModel;
		}
		return null;
	}
	
	/**
	 * 用户注销
	 * 
	 * @param juid
	 */
	public void logout(String juid){
		resetUserModel(juid);
	}
	
	/**
	 * 用户心跳维持
	 *
	 */
	public void heartbeat(){
		SecurityUtils.getSubject().getSession().setTimeout(Integer.valueOf(AOSCxt.getParam("user_login_timeout")));
	}
	
	/**
	 * 统计在线用户
	 * 
	 * @param inDto
	 * @return
	 */
	public List<Dto> listOnlineUsersPage(Dto inDto){
		List<Dto> usersList = Lists.newArrayList();
		Collection<Session> sessions = redisSessionDAO.getActiveSessions();
		for (Session session : sessions) {
			UserModel userModel = new UserModel();
			SimplePrincipalCollection principalCollection;
			if(session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY )== null){
				continue;
			}else {
				principalCollection = (SimplePrincipalCollection) session.getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
				userModel = (UserModel) principalCollection.getPrimaryPrincipal();
			}
			usersList.add(userModel2OnlineUserDto(userModel));
		}
		return usersList;
	}
	
	/**
	 * 将用户模型转换为在线用户Dto
	 * 
	 * @param userModel
	 * @return
	 */
	private Dto userModel2OnlineUserDto(UserModel userModel){
		Dto userDto = Dtos.newDto();
		userDto.put("juid", userModel.getJuid());
		userDto.put("id", userModel.getId());
		userDto.put("account", userModel.getAccount());
		userDto.put("name", userModel.getName());
		userDto.put("client_ip", userModel.getClient_ip());
		userDto.put("login_time", userModel.getLogin_time());
		userDto.put("client_key", userModel.getClient_key());
		return userDto;
	}
	
	/**
	 * 清除所有用户的功能权限数据(应用重启等时候调用)
	 * 
	 */
	public void clearGrantData(){
		Jedis jedis = JedisUtil.getJedisClient();
		Set<String> keys = jedis.keys(AOSCons.KEYS.FUNCTION_GRANT + "*");
		for (String key : keys) {
			jedis.del(key);
		}
		JedisUtil.close(jedis);
	}

}
