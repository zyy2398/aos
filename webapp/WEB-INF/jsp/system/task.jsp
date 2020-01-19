<%@ page contentType="text/html; charset=utf-8"%>
<%@ include file="/WEB-INF/jsp/common/tags.jsp"%>

<aos:html title="任务管理" base="http" lib="ext">
	<aos:body>
	</aos:body>
</aos:html>

<aos:onready>
	<aos:viewport layout="border">
		<aos:tabpanel id="_id_tabs" region="center" tabPosition="bottom" bodyBorder="0 0 0 0" margin="0 0 2 0">
			<aos:tab title="待办任务" layout="anchor" border="false">
				<aos:gridpanel id="g_todo_task" url="activitiMyTaskService.listTodoTask" onrender="g_todo_task_query" anchor="100% 100%" forceFit="true" onitemdblclick="todo_dbclick">
					<aos:menu>
						<aos:menuitem xtype="menuseparator" />
						<aos:menuitem text="刷新" onclick="#g_todo_task_store.reload();" icon="refresh.png" />
					</aos:menu>
					<aos:docked forceBoder="0 0 1 0" >
						<aos:combobox id="todocategory" dicField="model_group"  emptyText="流程分类" onselect="g_todo_task_query" margin="0 5 0 0" selectAll="true" width="180"  />
						<aos:datefield id="start_time" emptyText="起始日期" format="Y-m-d 00:00:00" editable="false" />
						<aos:datefield id="end_time" emptyText="截止日期" format="Y-m-d 23:59:59" editable="false" />
					</aos:docked>
					<aos:selmodel type="checkbox" mode="multi" />
					<aos:column type="rowno" />
					<aos:column header="标题" dataIndex="suspended" width="150" />
					<aos:column header="当前环节" dataIndex="deploymentId" width="150" />
					<aos:column header="任务内容" dataIndex="id" width="150" />
					<aos:column header="流程名称" dataIndex="key" width="150" />
					<aos:column header="创建时间" dataIndex="name" width="150" />
					<aos:column header="业务办理" rendererFn="fn_button_shenhe" align="center" width="100" minWidth="100" maxWidth="100" />
					<aos:column header="流程跟踪" rendererFn="fn_button_genzong" align="center" width="100" minWidth="100" maxWidth="100" />
				</aos:gridpanel>
			</aos:tab>
			<aos:tab title="已办任务" layout="anchor" border="false">
				<aos:gridpanel id="g_historic_task" url="activitiMyTaskService.listHistoricTask" onrender="g_historic_task_query" anchor="100% 100%" forceFit="true" onitemdblclick="historic_dbclick">
					<aos:menu>
						<aos:menuitem text="详情" onclick="" icon="del.png" />
						<aos:menuitem xtype="menuseparator" />
						<aos:menuitem text="刷新" onclick="#g_historic_task_store.reload();" icon="refresh.png" />
					</aos:menu>
					<aos:docked forceBoder="0 0 1 0" >
						<aos:combobox id="historiccategory" dicField="model_group"  emptyText="流程分类" onselect="g_historic_task_query" margin="0 5 0 0" selectAll="true" width="180"  />
						<aos:datefield id="starttime" emptyText="起始日期" format="Y-m-d 00:00:00" editable="false" />
						<aos:datefield id="endtime" emptyText="截止日期" format="Y-m-d 23:59:59" editable="false" />
					</aos:docked>
					<aos:selmodel type="checkbox" mode="multi" />
					<aos:column type="rowno" />
					<aos:column header="标题" dataIndex="processInstanceId" width="150" />
					<aos:column header="当前环节" dataIndex="processDefinitionId" width="200" />
					<aos:column header="流程名称" dataIndex="activityId" width="300" />
					<aos:column header="完成时间" dataIndex="suspended" width="120" />
				</aos:gridpanel>
			</aos:tab>
		</aos:tabpanel>
	</aos:viewport>

	<script type="text/javascript">

            //查询参数列表
            function g_todo_task_query() {
                var params = {
					todocategory: todocategory.getValue(),
					start_time: start_time.getValue(),
					end_time: end_time.getValue(),
                };
				g_todo_task_store.getProxy().extraParams = params;
				g_todo_task_store.loadPage(1);
            }

			//查询参数列表
			function g_historic_task_query() {
				var params = {
					historiccategory: historiccategory.getValue(),
					starttime: starttime.getValue(),
					endtime: endtime.getValue(),
				};
				g_historic_task_store.getProxy().extraParams = params;
				g_historic_task_store.loadPage(1);
			}

			function todo_dbclick(){

			}

			function historic_dbclick(){

			}

			//审核
			function fn_button_shenhe(value, metaData, record, rowIndex, colIndex, store) {
				if(!record.data.suspended){
					return '<input type="button" value="业务办理" class="cbtn" onclick="fn_shenhe_button_onclick(\'suspend\');" />';
				}else{
					return '<input type="button" value="业务签收" class="cbtn" onclick="fn_shenhe_button_onclick(\'active\');" />';
				}

			}

			//流程跟踪
			function fn_button_genzong(value, metaData, record, rowIndex, colIndex, store) {
				return '<input type="button" value="流程跟踪" class="cbtn_danger" onclick="fn_genzong_button_onclick(\'suspend\');" />';
			}

        </script>
</aos:onready>

<script>
	function fn_shenhe_button_onclick(obj){
		var record = AOS.selectone(Ext.getCmp('g_process'));
		AOS.ajax({
			params: {'procDefId':record.data.id,'state':obj},
			url: 'activitiMyTaskService.updateState',
			ok: function (data) {
				if(data.appcode == '1'){
					Ext.getCmp('g_process').getStore().reload();
					AOS.tip(data.appmsg);
				}else{
					AOS.err(data.appmsg);
				}
			}
		});
	}

	function fn_genzong_button_onclick(obj){
		var record = AOS.selectone(Ext.getCmp('g_process'));
		AOS.ajax({
			params: {'procDefId':record.data.id,'state':obj},
			url: 'activitiMyTaskService.updateState',
			ok: function (data) {
				if(data.appcode == '1'){
					Ext.getCmp('g_process').getStore().reload();
					AOS.tip(data.appmsg);
				}else{
					AOS.err(data.appmsg);
				}
			}
		});
	}
</script>