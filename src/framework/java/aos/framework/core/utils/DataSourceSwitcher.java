package aos.framework.core.utils;

import aos.framework.core.typewrap.Dto;
import cn.hutool.db.dialect.DriverUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * change data source
 *
 * Created by hzlaojiaqi on 2017/10/16.
 */
public class DataSourceSwitcher extends AbstractRoutingDataSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceSwitcher.class);

    private static final ThreadLocal<String> dataSourceKey = new ThreadLocal<String>();
    private static Map<Object, Object> dataSourceMap = new HashMap<>();

    public static void clearDataSourceType() {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("thread:{},remove,dataSource:{}", Thread.currentThread().getName());
        }
        dataSourceKey.remove();
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String s = dataSourceKey.get();
        return s;
    }

    public static void setDataSourceKey(String dataSource) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("thread:{},set,dataSource:{}", Thread.currentThread().getName(), dataSource);
        }
        dataSourceKey.set(dataSource);
    }

    public static String getDataSourceKey() {
        String s = dataSourceKey.get();
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("thread:{},get,dataSource:{}", Thread.currentThread().getName(), s);
        }
        return s;
    }

    /**
     * 动态增加数据源
     *
     * @param map 数据源属性
     * @return
     */
    public synchronized boolean addDataSource(Dto map) {
        try {
            Connection connection = null;
            // 排除连接不上的错误
            try {
                Class.forName(DriverUtil.identifyDriver((String)map.get(DruidDataSourceFactory.PROP_URL)));
                connection = DriverManager.getConnection(
                        (String)map.get(DruidDataSourceFactory.PROP_URL),
                        (String)map.get(DruidDataSourceFactory.PROP_USERNAME),
                        (String)map.get(DruidDataSourceFactory.PROP_PASSWORD));
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                return false;
            } finally {
                if (connection != null && !connection.isClosed())
                    connection.close();
            }
            String database = (String)map.get("database");//获取要添加的数据库名
            if (StringUtils.isBlank(database)) return false;
            DruidDataSource druidDataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(map);
            druidDataSource.init();
            Map<Object, Object> targetMap = DataSourceSwitcher.dataSourceMap;
            targetMap.put(database, druidDataSource);
            // 当前 targetDataSources 与 父类 targetDataSources 为同一对象 所以不需要set
            this.setTargetDataSources(targetMap);
            LOGGER.info("dataSource {} has been added", database);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        dataSourceMap.putAll(targetDataSources);
        super.afterPropertiesSet();// 必须添加该句，否则新添加数据源无法识别到
    }

}
