<%@ page contentType="text/html; charset=utf-8"%>
<%@ include file="/WEB-INF/jsp/common/tags.jsp"%>

<aos:html title="流程管理" base="http" lib="ext">
	<aos:body>
	</aos:body>
</aos:html>

<aos:onready>
	<aos:viewport layout="border">
		<aos:tabpanel id="_id_tabs" region="center" tabPosition="bottom" bodyBorder="0 0 0 0" margin="0 0 2 0">
			<aos:tab title="流程管理" layout="anchor" border="false">
				<aos:gridpanel id="g_process" url="activitiProcessService.listProcess" onrender="g_process_query" anchor="100% 100%" forceFit="true">
					<aos:menu>
						<aos:menuitem text="部署流程" onclick="#w_process.show();" icon="add.png" />
						<aos:menuitem text="转换为模型" onclick="g_process_zhuanhuan" icon="shape_group.png" />
						<aos:menuitem text="删除" onclick="g_process_del" icon="del.png" />
						<aos:menuitem xtype="menuseparator" />
						<aos:menuitem text="刷新" onclick="#g_process_store.reload();" icon="refresh.png" />
					</aos:menu>
					<aos:docked forceBoder="0 0 1 0" >
						<aos:dockeditem onclick="#w_process.show();" text="部署流程" icon="add.png" />
						<aos:dockeditem onclick="g_process_zhuanhuan" text="转换为模型" icon="shape_group.png" />
						<aos:dockeditem onclick="g_process_del" text="删除" icon="del.png" />
						<aos:dockeditem xtype="tbseparator"/>
						<aos:combobox id="category" dicField="model_group"  emptyText="流程分类" onselect="g_process_query" margin="0 5 0 0" selectAll="true" width="180"  />
					</aos:docked>
					<aos:selmodel type="checkbox" mode="multi" />
					<aos:plugins>
						<aos:editing id="id_plugin" ptype="cellediting" clicksToEdit="2" />
					</aos:plugins>
					<aos:column type="rowno" />
					<aos:column header="是否挂起" dataIndex="suspended" hidden="true" />
					<aos:column header="部署ID" dataIndex="deploymentId" hidden="true" />
					<aos:column header="流程分类(可编辑)" dataIndex="category" rendererField="model_group" width="120">
						<aos:combobox dicField="model_group">
							<%-- 可编辑表格的表单项的监听事件只能通过这样的方法来实现监听 --%>
							<aos:on fn="card_type_onselect" event="select" />
						</aos:combobox>
					</aos:column>
					<aos:column header="流程ID" dataIndex="id" fixedWidth="150" />
					<aos:column header="流程标识" dataIndex="key" width="150" />
					<aos:column header="流程名称" dataIndex="name" width="150" />
					<aos:column header="流程版本" dataIndex="version" width="100" />
					<aos:column header="流程XML" dataIndex="resourceName" width="100"/>
					<aos:column header="流程图片" dataIndex="diagramResourceName" width="100"/>
					<aos:column header="部署时间" dataIndex="deploymentTime" width="150" />
					<aos:column header="操作" rendererFn="fn_button_caozuo" align="center" width="70" minWidth="70" maxWidth="70" />
				</aos:gridpanel>
			</aos:tab>
			<aos:tab title="运行中流程" layout="anchor" border="false">
				<aos:gridpanel id="g_run_process" url="activitiProcessService.listRunProcess" onrender="g_run_process_query" anchor="100% 100%" forceFit="true">
					<aos:menu>
						<aos:menuitem text="删除" onclick="g_run_process_del" icon="del.png" />
						<aos:menuitem xtype="menuseparator" />
						<aos:menuitem text="刷新" onclick="#g_run_process_store.reload();" icon="refresh.png" />
					</aos:menu>
					<aos:docked forceBoder="0 0 1 0" >
						<aos:dockeditem onclick="g_run_process_del" text="删除" icon="del.png" />
						<aos:dockeditem xtype="tbseparator"/>
						<aos:triggerfield emptyText="流程实例ID" id="procInsId" onenterkey="g_run_process_query" trigger1Cls="x-form-search-trigger"
										  onTrigger1Click="g_run_process_query" width="180" />
						<aos:triggerfield emptyText="流程定义Key" id="procDefKey" onenterkey="g_run_process_query" trigger1Cls="x-form-search-trigger"
										  onTrigger1Click="g_run_process_query" width="180" />
					</aos:docked>
					<aos:selmodel type="checkbox" mode="multi" />
					<aos:column type="rowno" />
					<aos:column header="执行ID" dataIndex="id" width="120"/>
					<aos:column header="流程实例ID" dataIndex="processInstanceId" width="200" />
					<aos:column header="流程定义ID" dataIndex="processDefinitionId" width="200" />
					<aos:column header="当前环节" dataIndex="activityId" width="300" />
					<aos:column header="是否挂起" dataIndex="suspended" width="120" />
				</aos:gridpanel>
			</aos:tab>
		</aos:tabpanel>
	</aos:viewport>


	<aos:window id="w_process" title="部署流程" onshow="#AOS.reset(f_process);" >
		<aos:formpanel id="f_process" width="500" layout="anchor" labelWidth="65" >
		    <aos:combobox name="category" fieldLabel="流程分类" dicField="model_group" value="1" allowBlank="false" />
			<aos:filefield name="file" fieldLabel="流程文件" allowBlank="false" />
		</aos:formpanel>
		<aos:docked dock="bottom" ui="footer">
			<aos:dockeditem xtype="tbfill" />
			<aos:dockeditem onclick="f_process_submit" text="保存" icon="ok.png" />
			<aos:dockeditem onclick="#w_process.hide();" text="关闭" icon="close.png" />
		</aos:docked>
	</aos:window>
	<script type="text/javascript">

			//监听选择事件
			function card_type_onselect(me){
				var selectionId = AOS.selectone(g_process);
				AOS.ajax({
					url: 'activitiProcessService.updateCategory',
					params: {'procDefId':selectionId.data.id,'category':me.getValue()},
					ok: function (data) {
						if(data.appcode == '1'){
							g_process_store.reload();
							AOS.tip(data.appmsg);
						}else{
							AOS.err(data.appmsg);
						}
					}
				});
			}

            //查询参数列表
            function g_process_query() {
                var params = {
					category: category.getValue()
                };
                g_process_store.getProxy().extraParams = params;
                g_process_store.loadPage(1);
            }

			//查询参数列表
			function g_run_process_query() {
				var params = {
					procInsId: procInsId.getValue(),
					procDefKey: procDefKey.getValue()
				};
				g_run_process_store.getProxy().extraParams = params;
				g_run_process_store.loadPage(1);
			}

            //删除流程实例
            function g_process_del() {
                var selectionIds = AOS.selection(g_process, 'deploymentId');
                if (AOS.empty(selectionIds)) {
                    AOS.tip('删除前请先选中数据。');
                    return;
                }
                var rows = AOS.rows(g_process);
                var msg = AOS.merge('确认要删除选中的[{0}]条数据吗？', rows);
                AOS.confirm(msg, function (btn) {
                    if (btn === 'cancel') {
                        AOS.tip('删除操作被取消。');
                        return;
                    }
                    AOS.ajax({
                        url: 'activitiProcessService.deleteProcess',
                        params: {
                            aos_rows: selectionIds
                        },
                        ok: function (data) {
                            AOS.tip(data.appmsg);
                            g_process_store.reload();
                        }
                    });
                });

            }

            //转换为模型
			function g_process_zhuanhuan(){
				var selectionIds = AOS.selection(g_process, 'id');
				if (AOS.empty(selectionIds)) {
					AOS.tip('转换前请先选中数据。');
					return;
				}
				var rows = AOS.rows(g_process);
				var msg = AOS.merge('确认要转换选中的[{0}]条数据吗？', rows);
				AOS.confirm(msg, function (btn) {
					if (btn === 'cancel') {
						AOS.tip('转换操作被取消。');
						return;
					}
					AOS.ajax({
						url: 'activitiProcessService.convertToModel',
						params: {
							aos_rows: selectionIds
						},
						ok: function (data) {
							AOS.tip(data.appmsg);
							g_process_store.reload();
						}
					});
				});
			}

			//删除运行中流程
			function g_run_process_del() {
				var selectionIds = AOS.selection(g_process, 'processInstanceId');
				if (AOS.empty(selectionIds)) {
					AOS.tip('删除前请先选中数据。');
					return;
				}
				var rows = AOS.rows(g_process);
				var msg = AOS.merge('确认要删除选中的[{0}]条数据吗？', rows);
				AOS.confirm(msg, function (btn) {
					if (btn === 'cancel') {
						AOS.tip('删除操作被取消。');
						return;
					}
					AOS.ajax({
						url: 'activitiProcessService.deleteRunProcess',
						params: {
							aos_rows: selectionIds
						},
						ok: function (data) {
							AOS.tip(data.appmsg);
							g_process_store.reload();
						}
					});
				});

			}

            //部署流程实例
            function f_process_submit() {
				f_process.submit({
					url: 'do?router=activitiProcessService.deploy',
					method : 'POST',
					waitMsg:'正在部署....',
					success : function(form, action) {
						if(action.result.appcode == '1'){
							w_process.hide();
							g_process_store.reload();
							AOS.tip(action.result.appmsg);
						}else{
							AOS.err(action.result.appmsg);
						}
					},
					failure : function(form, action) {
						if(action.result.appcode == '1'){
							w_process.hide();
							g_process_store.reload();
							AOS.tip(action.result.appmsg);
						}else{
							AOS.err(action.result.appmsg);
						}
					}
				});
            }

			//部署按钮
			function fn_button_caozuo(value, metaData, record, rowIndex, colIndex, store) {
				if(!record.data.suspended){
					return '<input type="button" value="挂起" class="cbtn_danger" onclick="fn_bushu_button_onclick(\'suspend\');" />';
				}else{
					return '<input type="button" value="激活" class="cbtn" onclick="fn_bushu_button_onclick(\'active\');" />';
				}

			}

        </script>
</aos:onready>

<script>
	function fn_bushu_button_onclick(obj){
		var record = AOS.selectone(Ext.getCmp('g_process'));
		AOS.ajax({
			params: {'procDefId':record.data.id,'state':obj},
			url: 'activitiProcessService.updateState',
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