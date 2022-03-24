package com.lecy.service;

import com.lecy.annotation.AddDataPermission;

public interface PermissionSqllnterface {
    String sqlPacket(String sql, AddDataPermission addDataPermission);
}
