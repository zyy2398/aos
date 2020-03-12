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
				<aos:gridpanel id="g_todo_task" url="activitiMyTaskService.listTodoTask" onrender="g_todo_task_query" anchor="100% 100%" forceFit="true">
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
					<aos:column header="任务ID" dataIndex="task_id" hidden="true" />
					<aos:column header="流程实例ID" dataIndex="processInstanceId" hidden="true" />
					<aos:column header="业务实例ID" dataIndex="businessKey" hidden="true" />
					<aos:column header="业务表单路径" dataIndex="form_path" hidden="true" />
					<aos:column header="执行人" dataIndex="assignee" hidden="true" />
					<aos:column header="标题" dataIndex="title" width="150" />
					<aos:column header="当前环节" dataIndex="taskNameLabel" width="150" />
					<aos:column header="任务内容" dataIndex="description" width="150" />
					<aos:column header="流程名称" dataIndex="processName" width="150" />
					<aos:column header="创建时间" dataIndex="create_time" width="150" />
					<aos:column header="任务状态" dataIndex="status" hidden="true" />
					<aos:column header="业务办理" rendererFn="fn_button_shenhe" align="center" width="100" minWidth="100" maxWidth="100" />
					<aos:column header="流程跟踪" rendererFn="fn_button_genzong" align="center" width="100" minWidth="100" maxWidth="100" />
				</aos:gridpanel>
			</aos:tab>
			<aos:tab title="已办任务" layout="anchor" border="false">
				<aos:gridpanel id="g_historic_task" url="activitiMyTaskService.listHistoricTask" onrender="g_historic_task_query" anchor="100% 100%" forceFit="true">
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
					<aos:column header="流程实例ID" dataIndex="processInstanceId" hidden="true" />
					<aos:column header="业务实例ID" dataIndex="businessKey" hidden="true" />
					<aos:column header="业务表单路径" dataIndex="form_path" hidden="true" />
					<aos:column header="标题" dataIndex="title" width="150" />
					<aos:column header="当前环节" dataIndex="taskNameLabel" width="200" />
					<aos:column header="流程名称" dataIndex="processName" width="300" />
					<aos:column header="完成时间" dataIndex="end_time" width="120" />
					<aos:column header="表单数据" rendererFn="fn_button_view" align="center" width="100" minWidth="100" maxWidth="100" />
					<aos:column header="流程跟踪" rendererFn="fn_button_genzong" align="center" width="100" minWidth="100" maxWidth="100" />
				</aos:gridpanel>
			</aos:tab>
		</aos:tabpanel>
	</aos:viewport>

	<aos:window id="w_img" title="流程图跟踪(标红表示已结束或活动中的流程)" layout="fit" width="900" height="500" maximizable="true" maximized="true">
		<aos:iframe id="frame_img"/>
		<aos:docked dock="bottom" ui="footer">
			<aos:dockeditem xtype="tbfill" />
			<aos:dockeditem onclick="#w_img.hide();" text="关闭" icon="close.png" />
		</aos:docked>
	</aos:window>

	<aos:window id="w_frame_business" title="任务表单查看" onclose="fn_w_close" icon="big64/10.png" layout="fit" maximizable="true" maximized="true" height="-50" width="-50">
		<aos:iframe id="frame_business"/>
	</aos:window>

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

			function fn_w_close(){
				g_todo_task_query();
				g_historic_task_query();
			}

			//审核
			function fn_button_shenhe(value, metaData, record, rowIndex, colIndex, store) {
				return '<input type="button" value="业务办理" class="cbtn" onclick="fn_shenhe_button_onclick(\''+record.data.form_path+'\',\''+record.data.businessKey+'\');" />';
			}

			//审核
			function fn_button_view(value, metaData, record, rowIndex, colIndex, store) {
				return '<input type="button" value="表单数据" class="cbtn" onclick="fn_shenhe_button_onclick(\''+record.data.form_path+'\',\''+record.data.businessKey+'\');" />';
			}

			//流程跟踪
			function fn_button_genzong(value, metaData, record, rowIndex, colIndex, store) {
				return '<input type="button" value="流程跟踪" class="cbtn_danger" onclick="fn_genzong_button_onclick(\''+record.data.processInstanceId+'\')" />';
			}

        </script>
</aos:onready>

<script>
	function fn_shenhe_button_onclick(form_path,businessKey){
		Ext.getCmp("w_frame_business").show();
		Ext.getCmp("frame_business").load("${cxt}/http/do?router="+form_path+"&id="+businessKey);
	}

	function fn_genzong_button_onclick(pProcessInstanceId){
		Ext.getCmp('w_img').show();
		Ext.getCmp('frame_img').load("${cxt}/http/do?router=activitiMyTaskService.getActivitiProccessImage&pProcessInstanceId="+pProcessInstanceId);
	}
</script>