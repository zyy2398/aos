package aos.system.dao.po;

import aos.framework.core.typewrap.PO;
import java.util.Date;

/**
 * <b>aos_role_module[aos_role_module]数据持久化对象</b>
 * <p>
 * 注意:此文件由AOS平台自动生成-禁止手工修改。
 * </p>
 * 
 * @author xiongchun
 * @date 2017-03-23 17:54:25
 */
public class AosRoleModulePO extends PO {

	private static final long serialVersionUID = 1L;

	/**
	 * 流水号
	 */
	private Integer id;
	
	/**
	 *  角色流水号
	 */
	private Integer role_id;
	
	/**
	 * 功能模块流水号
	 */
	private Integer module_id;
	
	/**
	 * 权限类型
	 */
	private String grant_type;
	
	/**
	 * 创建时间
	 */
	private Date create_time;
	
	/**
	 * 创建人ID
	 */
	private Integer create_by;
	

	/**
	 * 流水号
	 * 
	 * @return id
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 *  角色流水号
	 * 
	 * @return role_id
	 */
	public Integer getRole_id() {
		return role_id;
	}
	
	/**
	 * 功能模块流水号
	 * 
	 * @return module_id
	 */
	public Integer getModule_id() {
		return module_id;
	}
	
	/**
	 * 权限类型
	 * 
	 * @return grant_type
	 */
	public String getGrant_type() {
		return grant_type;
	}
	
	/**
	 * 创建时间
	 * 
	 * @return create_time
	 */
	public Date getCreate_time() {
		return create_time;
	}
	
	/**
	 * 创建人ID
	 * 
	 * @return create_by
	 */
	public Integer getCreate_by() {
		return create_by;
	}
	

	/**
	 * 流水号
	 * 
	 * @param id
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 *  角色流水号
	 * 
	 * @param role_id
	 */
	public void setRole_id(Integer role_id) {
		this.role_id = role_id;
	}
	
	/**
	 * 功能模块流水号
	 * 
	 * @param module_id
	 */
	public void setModule_id(Integer module_id) {
		this.module_id = module_id;
	}
	
	/**
	 * 权限类型
	 * 
	 * @param grant_type
	 */
	public void setGrant_type(String grant_type) {
		this.grant_type = grant_type;
	}
	
	/**
	 * 创建时间
	 * 
	 * @param create_time
	 */
	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
	
	/**
	 * 创建人ID
	 * 
	 * @param create_by
	 */
	public void setCreate_by(Integer create_by) {
		this.create_by = create_by;
	}
	

}