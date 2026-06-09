package com.datang.aibase.common.interceptor;

import com.datang.aibase.common.entity.BaseEntity;
import com.datang.aibase.common.util.SnowflakeIdGenerator;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

@Intercepts(@Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}))
public class SnowflakeAutoFillInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];

        if (parameter instanceof BaseEntity entity) {
            fill(ms, entity);
        } else if (parameter instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                if (value instanceof BaseEntity entity) {
                    fill(ms, entity);
                } else if (value instanceof Collection<?> coll) {
                    for (Object item : coll) {
                        if (item instanceof BaseEntity entity) {
                            fill(ms, entity);
                        }
                    }
                }
            }
        }

        return invocation.proceed();
    }

    private void fill(MappedStatement ms, BaseEntity entity) {
        if (ms.getSqlCommandType() == SqlCommandType.INSERT) {
            if (entity.getId() == null || entity.getId().isBlank()) {
                entity.setId(SnowflakeIdGenerator.nextId());
            }
            LocalDateTime now = LocalDateTime.now();
            if (entity.getCreatedAt() == null) {
                entity.setCreatedAt(now);
            }
            if (entity.getUpdatedAt() == null) {
                entity.setUpdatedAt(now);
            }
        } else if (ms.getSqlCommandType() == SqlCommandType.UPDATE) {
            entity.setUpdatedAt(LocalDateTime.now());
        }
    }
}
