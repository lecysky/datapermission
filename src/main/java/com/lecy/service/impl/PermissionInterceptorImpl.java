package com.lecy.service.impl;

import com.lecy.annotation.AddDataPermission;
import com.lecy.service.PermissionSqllnterface;
import com.lecy.utils.ReflectUtil;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Properties;

@Intercepts(
        { @Signature(method = "prepare", type = StatementHandler.class, args = {Connection.class, Integer.class})})
public class PermissionInterceptorImpl implements Interceptor {

    @Autowired
    PermissionSqllnterface permissionSqllnterface;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (invocation.getTarget() instanceof RoutingStatementHandler) {
            //获取路由RoutingStatementHandler
            RoutingStatementHandler statementHandler = (RoutingStatementHandler) invocation.getTarget();
            //获取StatementHandler
            StatementHandler delegate = (StatementHandler) ReflectUtil.getFieldValue(statementHandler, "delegate");

            //获取sql
            BoundSql boundSql = delegate.getBoundSql();

            //获取mapper接口
            MappedStatement mappedStatement = (MappedStatement) ReflectUtil.getFieldValue(delegate, "mappedStatement");
            //获取mapper类文件
            Class<?> clazz = Class.forName(mappedStatement.getId().substring(0, mappedStatement.getId().lastIndexOf(".")));
            //获取mapper执行方法名
            int length=mappedStatement.getId().length();
            String mName = mappedStatement.getId().substring(mappedStatement.getId().lastIndexOf(".") + 1, length);

            //遍历方法
            for (Method method : clazz.getDeclaredMethods()) {
                //方法是否含有RequiredPermission注解，如果含有注解则将数据结果过滤
                if (method.isAnnotationPresent(AddDataPermission.class) && mName.equals(method.getName())) {
                    AddDataPermission addDataPermission =  method.getAnnotation(AddDataPermission.class);
                    //判断是否为select语句
                    if (mappedStatement.getSqlCommandType().toString().equals("SELECT")) {
                        String sql = boundSql.getSql();
                        //根据用户权限拼接sql
                        String newSql = permissionSqllnterface.sqlPacket(sql, addDataPermission);
                        //将sql注入boundSql
                        ReflectUtil.setFieldValue(boundSql, "sql", newSql);
                        break;
                    }
                }
            }
        }
        return invocation.proceed();
    }

    //代理配置
    @Override
    public Object plugin(Object arg0) {
        if (arg0 instanceof StatementHandler) {
            return Plugin.wrap(arg0, this);
        } else {
            return arg0;
        }
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
