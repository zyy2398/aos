<%--
  Created by IntelliJ IDEA.
  User: papio
  Date: 2020/1/17
  Time: 10:52
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="/WEB-INF/jsp/common/tags.jsp"%>
<aos:html title="工作列表" base="http" lib="ext">
    <aos:body>
        <div id="remark" class="x-hidden">
            <ul style="margin: 0px 0px 10px 66px;">
                <li>Bean调用示例：ryTask.ryParams('ry')</li>
                <li>Class类调用示例：com.ruoyi.quartz.task.RyTask.ryParams('ry')</li>
                <li>参数说明：支持字符串，布尔类型，长整型，浮点型，整型</li>
            </ul>
        </div>
    </aos:body>
</aos:html>

<aos:onready>
    <aos:viewport layout="fit">
        <aos:gridpanel id="g_job" url="jobService.listJob" onrender="g_job_query"  forceFit="true" onitemdblclick="w_job_u_show">
            <aos:menu>
                <aos:menuitem text="新增" onclick="#w_job.show();" icon="add.png" />
                <aos:menuitem text="修改" onclick="w_job_u_show" icon="edit.png" />
                <aos:menuitem text="删除" onclick="g_job_del" icon="del.png" />
                <aos:menuitem text="日志" onclick="w_jobLog_u_show" icon="bug.png" />
                <aos:menuitem xtype="menuseparator" />
                <aos:menuitem text="刷新" onclick="#g_job_store.reload();" icon="refresh.png" />
            </aos:menu>
            <aos:docked forceBoder="0 0 1 0" >
                <aos:dockeditem onclick="#w_job.show();" text="新增" icon="add.png" />
                <aos:dockeditem onclick="w_job_u_show" text="修改" icon="edit.png" />
                <aos:dockeditem onclick="g_job_del" text="删除" icon="del.png" />
                <aos:dockeditem onclick="w_jobLog_u_show" text="日志" icon="bug.png" />
                <aos:dockeditem xtype="tbseparator"/>
                <aos:triggerfield emptyText="任务名称" id="job_name" trigger1Cls="x-form-search-trigger" onenterkey="g_job_query" onTrigger1Click="g_job_query"/>
                <aos:combobox id="job_group" dicField="job_group"  emptyText="任务分组" onselect="g_job_query" selectAll="true" />
                <aos:combobox id="status" dicField="job_status"  emptyText="任务状态" onselect="g_job_query" selectAll="true"/>
            </aos:docked>
            <aos:selmodel type="checkbox" mode="multi" />
            <aos:plugins>
                <aos:editing id="id_plugin" ptype="cellediting" clicksToEdit="1" />
            </aos:plugins>
            <aos:column header="任务编号" dataIndex="job_id" width="100" />
            <aos:column header="任务名称" dataIndex="job_name" width="200" />
            <aos:column header="任务分组" dataIndex="job_group" rendererField="job_group" width="100" />
            <aos:column header="调用目标字符串" dataIndex="invoke_target" width="300" />
            <aos:column header="执行表达式" dataIndex="cron_expression" width="200"/>
            <aos:column header="任务状态" dataIndex="status" rendererField="job_status" width="100">
                <aos:combobox dicField="job_status">
                    <%-- 可编辑表格的表单项的监听事件只能通过这样的方法来实现监听 --%>
                    <aos:on fn="job_status_onselect" event="select" />
                </aos:combobox>
            </aos:column>
            <aos:column header="执行策略" dataIndex="misfire_policy" hidden="true"/>
            <aos:column header="并发执行" dataIndex="concurrent" hidden="true"/>
            <aos:column header="创建人" dataIndex="create_by" width="100" />
            <aos:column header="创建时间" dataIndex="create_time" width="200" />
            <aos:column header="备注" dataIndex="remark" width="200"/>
            <aos:column header="操作" rendererFn="fn_button_zhixing" align="center" width="100" minWidth="100" maxWidth="100" />
        </aos:gridpanel>
    </aos:viewport>
    <aos:window id="w_job" title="添加任务" onshow="#AOS.reset(f_job);" >
        <aos:formpanel id="f_job" width="500" layout="anchor" labelWidth="100" >
            <aos:textfield name="job_name" fieldLabel="任务名称" allowBlank="false" maxLength="50" />
            <aos:combobox name="job_group" fieldLabel="任务分组" dicField="job_group" value="DEFAULT" allowBlank="false" />
            <aos:textfield name="invoke_target" fieldLabel="调度目标字符串" allowBlank="false" maxLength="500" />
            <aos:container contentEl="remark"/>
            <aos:textfield name="cron_expression" fieldLabel="cron表达式" allowBlank="false" maxLength="500" />
            <aos:combobox name="status" fieldLabel="任务状态" dicField="job_status" value="1" allowBlank="false" />
            <aos:radioboxgroup fieldLabel="执行策略" columns="4" margin="0">
                <aos:radiobox name="misfire_policy" boxLabel="立即执行" checked="true" inputValue="1" />
                <aos:radiobox name="misfire_policy" boxLabel="执行一次" inputValue="2" />
                <aos:radiobox name="misfire_policy" boxLabel="放弃执行" inputValue="3" />
            </aos:radioboxgroup>
            <aos:radioboxgroup fieldLabel="并发执行" columns="4" >
                <aos:radiobox name="concurrent" boxLabel="允许" checked="true" inputValue="0" />
                <aos:radiobox name="concurrent" boxLabel="禁止" inputValue="1" />
            </aos:radioboxgroup>
            <aos:textareafield fieldLabel="备注" name="remark" maxLength="2000" height="60" />
        </aos:formpanel>
        <aos:docked dock="bottom" ui="footer">
            <aos:dockeditem xtype="tbfill" />
            <aos:dockeditem onclick="f_job_submit" text="保存" icon="ok.png" />
            <aos:dockeditem onclick="#w_job.hide();" text="关闭" icon="close.png" />
        </aos:docked>
    </aos:window>
    <aos:window id="w_job_u" title="修改任务" >
        <aos:formpanel id="f_job_u" width="500" layout="anchor" labelWidth="100">
            <aos:hiddenfield name="job_id"/>
            <aos:textfield name="job_name" fieldLabel="任务名称" allowBlank="false" maxLength="50" />
            <aos:combobox name="job_group" fieldLabel="任务分组" dicField="job_group" value="DEFAULT" allowBlank="false" />
            <aos:textfield name="invoke_target" fieldLabel="调度目标字符串" allowBlank="false" maxLength="500" />
            <aos:container contentEl="remark"/>
            <aos:textfield name="cron_expression" fieldLabel="cron表达式" allowBlank="false" maxLength="500" />
            <aos:combobox name="status" fieldLabel="任务状态" dicField="job_status" value="1" allowBlank="false" />
            <aos:radioboxgroup fieldLabel="执行策略" columns="4" >
                <aos:radiobox name="misfire_policy" boxLabel="立即执行" checked="true" inputValue="1" />
                <aos:radiobox name="misfire_policy" boxLabel="执行一次" inputValue="2" />
                <aos:radiobox name="misfire_policy" boxLabel="放弃执行" inputValue="3" />
            </aos:radioboxgroup>
            <aos:radioboxgroup fieldLabel="并发执行" columns="4" >
                <aos:radiobox name="concurrent" boxLabel="允许" checked="true" inputValue="0" />
                <aos:radiobox name="concurrent" boxLabel="禁止" inputValue="1" />
            </aos:radioboxgroup>
            <aos:textareafield fieldLabel="备注" name="remark" maxLength="2000" height="60" />
        </aos:formpanel>
        <aos:docked dock="bottom" ui="footer">
            <aos:dockeditem xtype="tbfill" />
            <aos:dockeditem onclick="f_job_update" text="保存" icon="ok.png" />
            <aos:dockeditem onclick="#w_job_u.hide();" text="关闭" icon="close.png" />
        </aos:docked>
    </aos:window>

    <aos:window id="w_jobLog" title="调度日志" width="-50" height="-50" maximizable="true">
        <aos:gridpanel id="g_jobLog" url="jobLogService.listJobLog" onrender="g_jobLog_query"  forceFit="true" onitemdblclick="w_jobLog_u_show">
            <aos:docked forceBoder="0 0 1 0" >
                <aos:combobox id="zhixing_status" emptyText="执行状态" onselect="g_jobLog_query" selectAll="true">
                    <aos:option value="0" display="成功" />
                    <aos:option value="1" display="失败" />
                </aos:combobox>
                <aos:datefield id="start_time" emptyText="起始日期" format="Y-m-d 00:00:00" editable="false" onchange="g_jobLog_query" />
                <aos:datefield id="end_time" emptyText="截止日期" format="Y-m-d 23:59:59" editable="false" onchange="g_jobLog_query" />
            </aos:docked>
            <aos:column type="rowno" />
            <aos:column header="日志编号" dataIndex="job_log_id" width="100"/>
            <aos:column header="任务名称" dataIndex="job_name" width="100" />
            <aos:column header="任务分组" dataIndex="job_group" rendererField="job_group" width="100" />
            <aos:column header="调用目标字符串" dataIndex="invoke_target" width="300" />
            <aos:column header="日志信息" dataIndex="job_message" width="300"/>
            <aos:column header="状态" rendererFn="fn_swatch_status" dataIndex="status" width="100"/>
            <aos:column header="异常信息" dataIndex="exception_info" celltip="true" width="300"/>
            <aos:column header="创建时间" dataIndex="create_time" width="200" sortable="true"/>
        </aos:gridpanel>
        <aos:docked dock="bottom" ui="footer">
            <aos:dockeditem xtype="tbfill" />
            <aos:dockeditem onclick="#w_jobLog.hide();" text="关闭" icon="close.png" />
        </aos:docked>
    </aos:window>

    <script>
        function g_job_query(){
            var params = {
                job_name: job_name.getValue(),
                job_group: job_group.getValue(),
                status: status.getValue()
            };
            g_job_store.getProxy().extraParams = params;
            g_job_store.loadPage(1);
        }

        function g_jobLog_query(){
            var record = AOS.selectone(g_job);
            var params = {
                job_name: record.data.job_name,
                invoke_target: record.data.invoke_target,
                status: zhixing_status.getValue(),
                start_time: start_time.getValue(),
                end_time: end_time.getValue()
            };
            g_jobLog_store.getProxy().extraParams = params;
            g_jobLog_store.loadPage(1);
        }

        function w_job_u_show(){
            AOS.reset(f_job_u);
            var record = AOS.selectone(g_job);
            if(record){
                w_job_u.show();
                f_job_u.loadRecord(record);
            }
        }

        function f_job_submit(){
            AOS.ajax({
                forms: f_job,
                url: 'jobService.addJob',
                ok: function(data){
                    if(data.appcode == '1'){
                        w_job_u.hide();
                        g_job_store.reload();
                        AOS.tip(data.appmsg);
                    }else{
                        AOS.err(data.appmsg);
                    }

                }
            });
        }

        function f_job_update(){
            AOS.ajax({
                forms: f_job_u,
                url: 'jobService.updateJob',
                ok: function(data){
                    if(data.appcode == '1'){
                        w_job_u.hide();
                        g_job_store.reload();
                        AOS.tip(data.appmsg);
                    }else{
                        AOS.err(data.appmsg);
                    }
                }
            });
        }

        function w_jobLog_u_show(){
            var record = AOS.selectone(g_job);
            if(record){
                w_jobLog.show();
            }
        }

        //删除参数
        function g_job_del() {
            var selectionIds = AOS.selection(g_job, 'job_id');
            if (AOS.empty(selectionIds)) {
                AOS.tip('删除前请先选中数据。');
                return;
            }
            var rows = AOS.rows(g_job);
            var msg = AOS.merge('确认要删除选中的[{0}]条数据吗？', rows);
            AOS.confirm(msg, function (btn) {
                if (btn === 'cancel') {
                    AOS.tip('删除操作被取消。');
                    return;
                }
                AOS.ajax({
                    url: 'jobService.deleteJob',
                    params: {
                        aos_rows: selectionIds
                    },
                    ok: function (data) {
                        AOS.tip(data.appmsg);
                        g_job_store.reload();
                    }
                });
            });
        }

        function job_status_onselect(me){
            var selectionId = AOS.selectone(g_job);
            AOS.ajax({
                url: 'jobService.changeStatus',
                params: {
                    job_id: selectionId.data.job_id,
                    status: me.getValue()
                },
                ok: function (data) {
                    g_job_store.reload();
                    AOS.tip(data.appmsg);
                }
            });
        }

        function fn_button_zhixing(value, metaData, record, rowIndex, colIndex, store) {
            return '<input type="button" value="执行一次" class="cbtn" onclick="fn_zhixing_button_onclick();" />';
        }

        function fn_swatch_status(value, metaData, record, rowIndex, colIndex, store){
            if(value=='1'){
                return '<button class="cbtn">正常</button>';
            }else{
                return '<button class="cbtn_danger">错误</button>';
            }
        }
    </script>

</aos:onready>

<script>
    //导出事件
    function fn_zhixing_button_onclick(obj) {
        var record = AOS.selectone(Ext.getCmp('g_job'));
        AOS.confirm("确认要立即执行一次任务吗？", function (btn) {
            if (btn === 'cancel') {
                AOS.tip('执行操作被取消。');
                return;
            }
            AOS.ajax({
                url: 'jobService.run',
                params: {
                    job_id: record.data.job_id,
                    job_group: record.data.job_group
                },
                ok: function (data) {
                    AOS.tip(data.appmsg);
                }
            });
        });
    }
</script>