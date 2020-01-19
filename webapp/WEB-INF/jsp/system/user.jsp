<%@ page contentType="text/html; charset=utf-8"%>
<%@ include file="/WEB-INF/jsp/common/tags.jsp"%>

<aos:html title="用户管理" base="http" lib="ext">
<aos:body>
</aos:body>
</aos:html>

<aos:onready>
	<aos:viewport layout="border">
		<aos:treepanel id="t_org" region="west" bodyBorder="0 1 0 0" width="250" singleClick="false"
			onitemclick="g_user_query" url="userService.getTreeData" nodeParam="parent_id" rootId="${rootPO.id}"
			rootText="${rootPO.name}" rootVisible="true" rootIcon="${rootPO.icon_name}"
			rootAttribute="cascade_id :'${rootPO.cascade_id}'">
			<aos:docked forceBoder="0 1 1 0">
				<aos:dockeditem xtype="tbtext" text="组织部门树" />
				<aos:dockeditem xtype="tbfill" />
				<aos:checkbox boxLabel="级联显示" id="id_cascade" onchange="g_user_query" checked="true" />
			</aos:docked>
			<aos:menu>
				<aos:menuitem text="新增用户" onclick="#w_user.show();" icon="add.png" />
				<aos:menuitem xtype="menuseparator" />
				<aos:menuitem text="刷新" onclick="t_org_refresh" icon="refresh.png" />
			</aos:menu>
		</aos:treepanel>
		<aos:gridpanel id="g_user" url="userService.listUsers" region="center" bodyBorder="1 0 1 0"
			onitemdblclick="#w_user_u.show();">
			<aos:docked forceBoder="0 0 1 0">
				<aos:dockeditem text="新增" onclick="#w_user.show();" icon="add.png" />
				<aos:dockeditem text="修改" onclick="#w_user_u.show()" icon="edit.png" />
				<aos:dockeditem text="删除" onclick="g_user_del" icon="del.png" />
				<aos:dockeditem xtype="tbseparator" />
				<aos:dockeditem text="重置密码" onclick="w_pwd_show" icon="key.png" />
				<aos:dockeditem xtype="tbseparator" />
				<aos:triggerfield emptyText="登录账号|姓名" id="id_na" onenterkey="g_user_query" trigger1Cls="x-form-search-trigger"
					onTrigger1Click="g_user_query" width="180" />
			</aos:docked>
			<aos:menu>
				<aos:menuitem xtype="menuseparator" />
				<aos:menuitem text="新增用户" onclick="#w_user.show();" icon="add.png" />
				<aos:menuitem text="修改用户" onclick="#w_user_u.show();" icon="edit.png" />
				<aos:menuitem text="删除用户" onclick="g_user_del" icon="del.png" />
				<aos:menuitem xtype="menuseparator" />
				<aos:menuitem text="重置密码" onclick="w_pwd_show" icon="key.png" />
				<aos:menuitem xtype="menuseparator" />
				<aos:menuitem text="刷新" onclick="#g_user_store.reload();" icon="refresh.png" />
			</aos:menu>
			<aos:selmodel type="checkbox" mode="multi" />
			<aos:column type="rowno" />
			<aos:column header="用户流水号" dataIndex="id" fixedWidth="150" hidden="true" />
			<aos:column header="所属组织流水号" dataIndex="org_id" fixedWidth="150" hidden="true" />
			<aos:column header="登录帐号" dataIndex="account" fixedWidth="120" />
			<aos:column header="姓名" dataIndex="name" fixedWidth="100" />
			<aos:column header="所属组织" dataIndex="org_name" fixedWidth="150" />
			<aos:column header="所属角色" dataIndex="roles" celltip="true" minWidth="100" />
			<aos:column header="性别" dataIndex="sex" fixedWidth="60" rendererField="sex" />
			<aos:column header="用户状态" dataIndex="status" fixedWidth="80" rendererField="user_status" />
			<aos:column header="用户类型" dataIndex="type" fixedWidth="80" rendererField="user_type" />
			<aos:column header="皮肤" dataIndex="skin" fixedWidth="80" />
			<aos:column header="扩展码" dataIndex="biz_code" minWidth="100" />
			<aos:column header="联系地址" dataIndex="address" fixedWidth="150" hidden="true" />
			<aos:column header="备注" dataIndex="remark" fixedWidth="150" hidden="true" />
			<aos:column header="创建人" dataIndex="create_by" fixedWidth="150" hidden="true" />
			<aos:column header="创建时间" dataIndex="create_time" fixedWidth="150" hidden="true" />
			<aos:column header="选择角色" rendererFn="_btn_role_render" align="center" fixedWidth="80" />
		</aos:gridpanel>
	</aos:viewport>

	<aos:window id="w_user" title="新增用户" onshow="w_user_onshow">
		<aos:formpanel id="f_user" width="650" labelWidth="65">
			<aos:hiddenfield fieldLabel="所属部门流水号" name="org_id" />
			<aos:fieldset title="基础信息" labelWidth="75" columnWidth="1">
				<aos:textfield name="org_name" fieldLabel="所属部门" allowBlank="false" readOnly="true" columnWidth="0.5" />
				<aos:textfield name="name" fieldLabel="姓名" allowBlank="false" maxLength="20" columnWidth="0.49" />
				<aos:textfield name="account" fieldLabel="登录帐号" allowBlank="false" maxLength="20" columnWidth="0.5" />
				<aos:textfield name="password" fieldLabel="登录密码" allowBlank="false" maxLength="20" columnWidth="0.49" />
				<aos:combobox name="sex" fieldLabel="性别" allowBlank="false" dicField="sex" value="3" columnWidth="0.5" />
				<aos:textfield name="email" fieldLabel="电子邮件" vtype="email" maxLength="50" columnWidth="0.49" />
				<aos:textfield name="mobile" fieldLabel="联系电话" maxLength="50" columnWidth="0.5" />
				<aos:textfield name="idno" fieldLabel="身份证号" maxLength="50" columnWidth="0.49" />
				<aos:combobox name="type" fieldLabel="用户类型" allowBlank="false" value="1" dicField="user_type" columnWidth="0.5" />
				<aos:combobox name="status" fieldLabel="用户状态" allowBlank="false" value="1" dicField="user_status"
					columnWidth="0.49" />
				<aos:textfield name="address" fieldLabel="联系地址" maxLength="500" columnWidth="0.99" />
				<aos:textareafield name="remark" fieldLabel="备注" maxLength="4000" height="60" columnWidth="0.99" />
			</aos:fieldset>
			<aos:fieldset title="配置信息" labelWidth="75" columnWidth="1">
				<aos:combobox name="skin" fieldLabel="界面皮肤" allowBlank="false" dicField="skin" value="blue" columnWidth="0.5" />
				<aos:textfield name="biz_code" fieldLabel="扩展码" maxLength="200" columnWidth="0.49" />
			</aos:fieldset>
		</aos:formpanel>
		<aos:docked dock="bottom" ui="footer">
			<aos:dockeditem xtype="tbfill" />
			<aos:dockeditem onclick="f_user_save" text="保存" icon="ok.png" />
			<aos:dockeditem onclick="#w_user.hide();" text="关闭" icon="close.png" />
		</aos:docked>
	</aos:window>

	<aos:window id="w_org_find" title="所属部门[双击选择]" height="-10" autoScroll="true" onshow="t_org_find_load">
		<aos:treepanel id="t_org_find" singleClick="false" width="330" bodyBorder="0 0 0 0" url="userService.getTreeData"
			nodeParam="parent_id" rootId="${rootPO.id}" rootText="${rootPO.name}" rootVisible="true" rootExpanded="false"
			rootIcon="${rootPO.icon_name}" onitemdblclick="t_org_find_select">
			<aos:menu>
				<aos:menuitem text="选择" icon="ok1.png" onclick="t_org_find_select" />
				<aos:menuitem xtype="menuseparator" />
				<aos:menuitem text="关闭" onclick="#w_org_find.hide();" icon="del.png" />
			</aos:menu>
		</aos:treepanel>
	</aos:window>

	<aos:window id="w_user_u" title="修改用户" onshow="w_user_u_onshow">
		<aos:formpanel id="f_user_u" width="650" labelWidth="65">
			<aos:hiddenfield fieldLabel="用户流水号" name="id" />
			<aos:hiddenfield fieldLabel="所属部门流水号" name="org_id" />
			<aos:hiddenfield fieldLabel="所属部门流水号" name="old_org_id" />
			<aos:fieldset title="基础信息" labelWidth="75" columnWidth="1">
				<aos:triggerfield fieldLabel="所属部门" name="org_name" allowBlank="false" editable="false" onfocus="w_org_find_show"
					trigger1Cls="x-form-search-trigger" onTrigger1Click="w_org_find_show" columnWidth="0.5" />
				<aos:textfield name="name" fieldLabel="姓名" allowBlank="false" maxLength="20" columnWidth="0.49" />
				<aos:textfield name="account" fieldLabel="登录帐号" allowBlank="false" maxLength="20" columnWidth="0.5" />
				<aos:combobox name="sex" fieldLabel="性别" allowBlank="false" dicField="sex" value="3" columnWidth="0.49" />
				<aos:textfield name="email" fieldLabel="电子邮件" vtype="email" maxLength="50" columnWidth="0.5" />
				<aos:textfield name="mobile" fieldLabel="联系电话" maxLength="50" columnWidth="0.49" />
				<aos:textfield name="idno" fieldLabel="身份证号" maxLength="50" columnWidth="0.5" />
				<aos:combobox name="type" fieldLabel="用户类型" allowBlank="false" value="1" dicField="user_type" columnWidth="0.49" />
				<aos:combobox name="status" fieldLabel="用户状态" allowBlank="false" value="1" dicField="user_status"
					columnWidth="0.5" />
				<aos:textfield name="address" fieldLabel="联系地址" maxLength="500" columnWidth="0.99" />
				<aos:textareafield name="remark" fieldLabel="备注" maxLength="4000" height="60" columnWidth="0.99" />
			</aos:fieldset>
			<aos:fieldset title="配置信息" labelWidth="75" columnWidth="1">
				<aos:combobox name="skin" fieldLabel="界面皮肤" allowBlank="false" dicField="skin" value="blue" columnWidth="0.5" />
				<aos:textfield name="biz_code" fieldLabel="扩展码" maxLength="200" columnWidth="0.49" />
			</aos:fieldset>
		</aos:formpanel>
		<aos:docked dock="bottom" ui="footer">
			<aos:dockeditem xtype="tbfill" />
			<aos:dockeditem onclick="f_user_u_save" text="保存" icon="ok.png" />
			<aos:dockeditem onclick="#w_user_u.hide();" text="关闭" icon="close.png" />
		</aos:docked>
	</aos:window>

	<aos:window id="w_pwd" title="重置密码">
		<aos:formpanel id="f_pwd" width="400" layout="anchor" labelWidth="75">
			<aos:hiddenfield name="aos_rows" fieldLabel="已选中的用户ID集合" />
			<aos:textfield name="password" fieldLabel="新密码" allowBlank="false" inputType="password" maxLength="20" />
			<aos:textfield name="password_b" fieldLabel="确认新密码" allowBlank="false" inputType="password" maxLength="20" />
		</aos:formpanel>
		<aos:docked dock="bottom" ui="footer">
			<aos:dockeditem xtype="tbfill" />
			<aos:dockeditem onclick="f_pwd_save" text="保存" icon="ok.png" />
			<aos:dockeditem onclick="#w_pwd.hide();" text="关闭" icon="close.png" />
		</aos:docked>
	</aos:window>

	<aos:window id="w_user_role" title="选择角色" width="800" height="-10" layout="border" onshow="w_user_role_onshow">
		<aos:gridpanel id="g_role" bodyBorder="1 0 0 0" url="userService.listRoles" hidePagebar="true" region="center">
			<aos:docked forceBoder="0 0 1 0">
				<aos:dockeditem xtype="tbtext" text="待选角色" />
				<aos:dockeditem xtype="tbfill" />
				<aos:triggerfield emptyText="角色名称" id="id_role_name" onenterkey="g_role_query" trigger1Cls="x-form-search-trigger"
					onTrigger1Click="g_role_query" width="180" />
			</aos:docked>
			<aos:menu>
				<aos:menuitem onclick="user_role_save" text="选中授权" icon="ok1.png" />
				<aos:menuitem xtype="menuseparator" />
				<aos:menuitem text="刷新" onclick="#g_role_store.reload();" icon="refresh.png" />
			</aos:menu>
			<aos:selmodel type="checkbox" mode="simple" />
			<aos:column type="rowno" />
			<aos:column header="角色流水号" dataIndex="id" hidden="true" />
			<aos:column header="角色名称" dataIndex="name" flex="1" />
		</aos:gridpanel>
		<aos:panel region="east" width="410">
			<aos:layout type="hbox" align="stretch" />
			<aos:panel bodyBorder="0 1 0 1" bodyPadding="3" width="65">
				<aos:layout type="vbox" align="center" pack="center" />
				<aos:button onclick="user_role_save" text="选中" tooltip="选中授权" iconVec="fa-angle-double-right" iconVecAlign="right"
					iconVecSize="16" />
				<aos:button onclick="_user_role_cancel" margin="20 0 0 0" text="撤消" tooltip="撤消授权" iconVec="fa-angle-double-left"
					iconVecSize="16" />
			</aos:panel>
			<aos:gridpanel id="g_role_selected" forceFit="false" url="userService.listSelectedRoles" width="345"
				bodyBorder="1 0 0 0" hidePagebar="true">
				<aos:docked forceBoder="0 0 1 0">
					<aos:dockeditem xtype="tbtext" text="已选角色" />
					<aos:dockeditem xtype="tbfill" />
					<aos:triggerfield emptyText="角色名称" id="id_role_nameselected" onenterkey="g_role_selected_query"
						trigger1Cls="x-form-search-trigger" onTrigger1Click="g_role_selected_query" width="180" />
				</aos:docked>
				<aos:menu>
					<aos:menuitem onclick="_user_role_cancel" text="撤消授权" icon="del.png" />
					<aos:menuitem xtype="menuseparator" />
					<aos:menuitem text="刷新" onclick="#g_role_selected_store.reload();" icon="refresh.png" />
				</aos:menu>
				<aos:selmodel type="checkbox" mode="simple" />
				<aos:column type="rowno" />
				<aos:column header="授权流水号" dataIndex="user_role_id" hidden="true" />
				<aos:column header="角色名称" dataIndex="name" flex="1" />
			</aos:gridpanel>
		</aos:panel>
		<aos:docked dock="bottom" ui="footer">
			<aos:dockeditem xtype="tbfill" />
			<aos:dockeditem onclick="#w_user_role.hide();" text="关闭" icon="close.png" />
		</aos:docked>
	</aos:window>

	<script type="text/javascript">

		//保存用户角色关联信息
	    function user_role_save() {
	        var rows = AOS.rows(g_role);
	        if (rows === 0) {
	            AOS.tip('操作被取消，请先选中要关联的角色。');
	            return;
	        }
	        var record = AOS.selectone(g_user);
	        var params = {
	            aos_rows: AOS.selection(g_role, 'id'),
	            user_id: record.data.id
	        };
	        AOS.ajax({
	            params: params,
	            url: 'userService.saveUserRoleGrantInfo',
	            ok: function (data) {
	                AOS.tip(data.appmsg);
	                g_role_store.reload();
	                g_role_selected_query();
	            }
	        });
	    }
	
	    //取消用户角色关联
	    function _user_role_cancel() {
	        var rows = AOS.rows(g_role_selected);
	        if (rows === 0) {
	            AOS.tip('操作被取消，请先选中要撤消的角色。');
	            return;
	        }
	        AOS.ajax({
	            url: 'userService.delUserRoleGrantInfo',
	            params: {
	                aos_rows: AOS.selection(g_role_selected, 'user_role_id')
	            },
	            ok: function (data) {
	                AOS.tip(data.appmsg);
	                g_role_selected_store.reload();
	                g_role_store.reload();
	            }
	        });
	    }
	    //监听选择角色窗口弹出事件
	    function w_user_role_onshow() {
	        var record = AOS.selectone(g_user);
	        var title = AOS.merge('选择角色 [{0}]', record.data.name);
	        w_user_role.setTitle(AOS.title(title));
	        g_role_query();
	        g_role_selected_query();
	    }
	    
        //查询选择角色窗口角色列表
        function g_role_query() {
            var params = {
                name : id_role_name.getValue(),
                user_id : AOS.selectone(g_user).data.id
            };
            g_role_store.getProxy().extraParams = params;
            g_role_store.loadPage(1);
        }

        //查询选择角色窗口角色列表(已选择)
        function g_role_selected_query() {
            var params = {
                name : id_role_nameselected.getValue(),
                user_id : AOS.selectone(g_user).data.id
            };
            g_role_selected_store.getProxy().extraParams = params;
            g_role_selected_store.loadPage(1);
        }
    
		 //选择角色按钮列转换
		function _btn_role_render(value, metaData, record, rowIndex, colIndex,store) {
		   if(record.data.id == '${super_user_id}'){
			   return '--';
		   }else{
			   return '<input type="button" value="选择角色" class="cbtn" onclick="_btn_role_onclick();" />';
		   }
		}
	
	    //刷新所属部门选择树
	    function t_org_find_load() {
	        var refreshnode = AOS.selectone(t_org_find);
	        if (!refreshnode) {
	            refreshnode = t_org_find.getRootNode();
	        }
	        if (refreshnode.isLeaf()) {
	            refreshnode = refreshnode.parentNode;
	        }
	        t_org_find_store.load({
	            node: refreshnode,
	            callback: function () {
	                //收缩再展开才能在刷新时正确显示expanded=true的节点的子节点
	                refreshnode.collapse();
	                refreshnode.expand();
	            }
	        });
	    }
	
	    //所属组织树节点双击事件
	    function t_org_find_select() {
	        var record = AOS.selectone(t_org_find);
	        AOS.setValue('f_user_u.org_id', record.raw.id);
	        AOS.setValue('f_user_u.org_name', record.raw.text);
	        w_org_find.hide();
	    }
	
	   //弹出部门选择出口
	   function w_org_find_show(){
		   w_org_find.show();
	   }
	
		//查询人员列表
		function g_user_query() {
			var params = {
				na : id_na.getValue(),
				cascade : id_cascade.getValue()
			};
			var record = AOS.selectone(t_org);
			if(!AOS.empty(record)){
				params.org_id = record.raw.id;
				params.org_cascade_id = record.raw.a;
			}
			g_user_store.getProxy().extraParams = params;
			g_user_store.loadPage(1);
		}
		
		//自动选中根节点
		AOS.job(function(){
			t_org.getSelectionModel().select(t_org.getRootNode());
			g_user_query();
		},10);
		
	    //弹出新增人员窗口
		function w_user_onshow(){
			AOS.reset(f_user);
			var record = AOS.selectone(t_org);
			if(!AOS.empty(record)){
				AOS.setValue('f_user.org_id', record.raw.id); 
				AOS.setValue('f_user.org_name', record.raw.text); 
			} 
		}
			
		//刷新部门树
		function t_org_refresh() {
			var refreshnode = AOS.selectone(t_org);
	        if (!refreshnode) {
	            refreshnode = t_org.getRootNode();
	        }
	        if (refreshnode.isLeaf()) {
	            refreshnode = refreshnode.parentNode;
	        }
			t_org_store.load({
				node : refreshnode,
				callback : function() {
					//收缩再展开才能在刷新时正确显示expanded=true的节点的子节点
					refreshnode.collapse();
					refreshnode.expand();
					t_org.getSelectionModel().select(refreshnode);
				}
			});
		}
	    
		//新增用户保存
		function f_user_save(){
				AOS.ajax({
					forms : f_user,
					url : 'userService.saveUser',
					ok : function(data) {
						if(data.appcode=='1'){
							AOS.tip(data.appmsg);
							w_user.hide();
							g_user_store.reload();
						}else{
							AOS.get('f_user.account').markInvalid(data.appmsg);
							AOS.err(data.appmsg);
						}
					}
			});
		}	
		
	    //监听修改用户窗口弹出事件
		function w_user_u_onshow(){
			AOS.reset(f_user_u);
			var record = AOS.selectone(g_user);
			AOS.ajax({
				params:{
					id : record.data.id
				},
                url: 'userService.getUser',
                ok: function (data) {
                	AOS.setValues(f_user_u, data);
                }
            });
		}	
		
		//修改用户保存
		function f_user_u_save(){
				AOS.ajax({
				forms : f_user_u,
				url : 'userService.updateUser',
				ok : function(data) {
					if(data.appcode == '1'){
						w_user_u.hide();
						g_user_store.reload();
						AOS.tip(data.appmsg);
					}else{
						AOS.get('f_user_u.account').markInvalid(data.appmsg);
						AOS.err(data.appmsg);
					}
				}
			});
		}			
 
        //删除用户
	 	function g_user_del(){
				var selection = AOS.selection(g_user, 'id');
				if(AOS.empty(selection)){
					AOS.tip('删除前请先选中数据。');
					return;
				}
				var rows = AOS.rows(g_user);
				var msg =  AOS.merge('确认要删除选中的{0}个用户吗？', rows);
				AOS.confirm(msg, function(btn){
					if(btn == 'cancel'){
						AOS.tip('删除操作被取消。');
						return;
					}
					AOS.ajax({
						url : 'userService.deleteUser',
						params:{
							aos_rows: selection
						},
						ok : function(data) {
							AOS.tip(data.appmsg);
							g_user_store.reload();
						}
					});
				});
			}
        
        //弹出密码重置窗口
        function w_pwd_show() {
            AOS.reset(f_pwd);
            var selection = AOS.selection(g_user, 'id');
            if (AOS.empty(selection)) {
                AOS.tip('请先选中需要重置密码的用户。');
                return;
            }
            AOS.setValue('f_pwd.aos_rows', selection);
            w_pwd.show();
        }
        
        //重置密码
        function f_pwd_save() {
            if (AOS.getValue('f_pwd.password') !== AOS.getValue('f_pwd.password_b')) {
            	var msg = '两次输入密码不一致, 请重新输入。';
            	AOS.get('f_pwd.password_b').markInvalid(msg);
            	AOS.err(msg);
                return;
            }
            var msg = AOS.merge('确认要重置选中[{0}]个用户的密码吗？', AOS.rows(g_user));
            AOS.confirm(msg, function (btn) {
                if (btn === 'cancel') {
                    AOS.tip('重置密码操作被取消。');
                    return;
                }
                AOS.ajax({
                    forms: f_pwd,
                    url: 'userService.resetPassword',
                    ok: function (data) {
                        w_pwd.hide();
                        AOS.tip(data.appmsg);
                    }
                });
            });
        }
	</script>
</aos:onready>

<script type="text/javascript">
	//选择角色按钮
	function _btn_role_onclick(){
		Ext.getCmp('w_user_role').show();
	}
</script>