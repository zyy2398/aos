<%@ page contentType="text/html; charset=utf-8"%>
<%@ include file="/WEB-INF/jsp/common/tags.jsp"%>

<aos:html title="流程模型" base="http" lib="ext">
	<aos:body>
	</aos:body>
</aos:html>

<aos:onready>
	<aos:viewport layout="fit">
		<aos:gridpanel id="g_model" url="activitiModelService.listModel" onrender="g_model_query"  forceFit="true" onitemdblclick="w_model_u_show">
			<aos:menu>
				<aos:menuitem text="新增" onclick="#w_model.show();" icon="add.png" />
				<aos:menuitem text="修改" onclick="w_model_u_show" icon="edit.png" />
				<aos:menuitem text="删除" onclick="g_model_del" icon="del.png" />
				<aos:menuitem xtype="menuseparator" />
				<aos:menuitem text="刷新" onclick="#g_model_store.reload();" icon="refresh.png" />
			</aos:menu>
			<aos:docked forceBoder="0 0 1 0" >
				<aos:dockeditem onclick="#w_model.show();" text="新增" icon="add.png" />
				<aos:dockeditem onclick="w_model_u_show" text="修改" icon="edit.png" />
				<aos:dockeditem onclick="g_model_del" text="删除" icon="del.png" />
				<aos:dockeditem xtype="tbseparator"/>
				<aos:combobox id="category" dicField="model_group"  emptyText="流程分类" onselect="g_model_query" margin="0 5 0 0" selectAll="true" width="180"  />
			</aos:docked>
			<aos:selmodel type="checkbox" mode="multi" />
			<aos:plugins>
				<aos:editing id="id_plugin" ptype="cellediting" clicksToEdit="1" />
			</aos:plugins>
			<aos:column type="rowno" />
			<aos:column header="流水号" dataIndex="id" fixedWidth="150" hidden="true" />
			<aos:column header="流程分类(可编辑)" dataIndex="category" rendererField="model_group" width="120">
				<aos:combobox dicField="model_group">
					<%-- 可编辑表格的表单项的监听事件只能通过这样的方法来实现监听 --%>
					<aos:on fn="card_type_onselect" event="select" />
				</aos:combobox>
			</aos:column>
			<aos:column header="模型标识" dataIndex="key" width="200" />
			<aos:column header="模型名称" dataIndex="name" width="300" />
			<aos:column header="版本号" dataIndex="version" width="120" />
			<aos:column header="创建时间" dataIndex="createTime" width="200"/>
			<aos:column header="最后更新时间" dataIndex="lastUpdateTime" width="200"/>
			<aos:column header="部署" rendererFn="fn_button_bushu" align="center" width="70" minWidth="70" maxWidth="70" />
			<aos:column header="导出" rendererFn="fn_button_daochu" align="center" width="70" minWidth="70" maxWidth="70" />
		</aos:gridpanel>
	</aos:viewport>
	<aos:window id="w_model" title="新增模型"  onshow="#AOS.reset(f_model);" >
		<aos:formpanel id="f_model" width="500" layout="anchor" labelWidth="65" >
		    <aos:combobox name="category" fieldLabel="流程分类" dicField="model_group" value="1" allowBlank="false" />
			<aos:textfield name="name" fieldLabel="模块名称" allowBlank="false" maxLength="50" />
			<aos:textfield name="key" fieldLabel="模块标识" allowBlank="false" maxLength="50" />
			<aos:textareafield fieldLabel="模块描述" name="description" maxLength="2000" height="60" />
		</aos:formpanel>
		<aos:docked dock="bottom" ui="footer">
			<aos:dockeditem xtype="tbfill" />
			<aos:dockeditem onclick="f_model_submit" text="保存" icon="ok.png" />
			<aos:dockeditem onclick="#w_model.hide();" text="关闭" icon="close.png" />
		</aos:docked>
	</aos:window>
	<script type="text/javascript">

			//监听选择事件
			function card_type_onselect(me){
				var selectionId = AOS.selectone(g_model);
				AOS.ajax({
					url: 'activitiModelService.updateModel',
					params: {'id':selectionId.data.id,'category':me.getValue()},
					ok: function (data) {
						if(data.appcode == '1'){
							g_model_store.reload();
							AOS.tip(data.appmsg);
						}else{
							AOS.err(data.appmsg);
						}
					}
				});
			}

            //修改流程模型
            function w_model_u_show() {
                var record = AOS.selectone(g_model);
                if (record) {
					window.open('${cxt}/modeler.html?modelId='+record.data.id,'_blank');
                }
            }

            //查询参数列表
            function g_model_query() {
                var params = {
					category: category.getValue()
                };
                g_model_store.getProxy().extraParams = params;
                g_model_store.loadPage(1);
            }

            //删除参数
            function g_model_del() {
                var selectionIds = AOS.selection(g_model, 'id');
                if (AOS.empty(selectionIds)) {
                    AOS.tip('删除前请先选中数据。');
                    return;
                }
                var rows = AOS.rows(g_model);
                var msg = AOS.merge('确认要删除选中的[{0}]条数据吗？', rows);
                AOS.confirm(msg, function (btn) {
                    if (btn === 'cancel') {
                        AOS.tip('删除操作被取消。');
                        return;
                    }
                    AOS.ajax({
                        url: 'activitiModelService.deleteModel',
                        params: {
                            aos_rows: selectionIds
                        },
                        ok: function (data) {
                            AOS.tip(data.appmsg);
                            g_model_store.reload();
                        }
                    });
                });

            }

            //新增参数保存
            function f_model_submit() {
                AOS.ajax({
                    forms: f_model,
                    url: 'activitiModelService.saveModel',
                    ok: function (data) {
                    	if(data.appcode == '1'){
							window.open('${cxt}/modeler.html?modelId='+data.id,'_blank');
                            w_model.hide();
                            g_model_store.reload();
                            AOS.tip(data.appmsg);
                    	}else{
                    		AOS.err(data.appmsg);
                    	}
                    }
                });
            }

			//部署按钮
			function fn_button_bushu(value, metaData, record, rowIndex, colIndex, store) {
				return '<input type="button" value="部署" class="cbtn" onclick="fn_bushu_button_onclick();" />';
			}

			//导出按钮
			function fn_button_daochu(value, metaData, record, rowIndex, colIndex, store) {
				return '<input type="button" value="导出" class="cbtn" onclick="fn_daochu_button_onclick();" />';
			}

        </script>
</aos:onready>

<script type="text/javascript">
	//导出事件
	function fn_daochu_button_onclick(obj) {
		var record = AOS.selectone(Ext.getCmp('g_model'));
		AOS.file("do?router=activitiModelService.export&id="+record.data.id);
	}

	//部署事件
	function fn_bushu_button_onclick(obj) {
		var record = AOS.selectone(Ext.getCmp('g_model'));
		AOS.ajax({
			params: {'id':record.data.id},
			url: 'activitiModelService.deploy',
			ok: function (data) {
				if(data.appcode == '1'){
					Ext.getCmp('g_model').getStore().reload();
					AOS.tip(data.appmsg);
				}else{
					AOS.err(data.appmsg);
				}
			}
		});
	}
</script>