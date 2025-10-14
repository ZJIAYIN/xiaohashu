package com.quanxiaoha.xiaohashu.auth.domain.mapper;

import com.quanxiaoha.xiaohashu.auth.domain.dataobject.RoleDO;
import com.quanxiaoha.xiaohashu.auth.domain.dataobject.RolePermissionDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RolePermissionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RolePermissionDO record);

    int insertSelective(RolePermissionDO record);

    RolePermissionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePermissionDO record);

    int updateByPrimaryKey(RolePermissionDO record);

    /**
     * 根据角色id集合查出所有的对应权限id
     * @param RolesIds
     * @return
     */
    List<RolePermissionDO> selectByRoleIds(@Param("RoleIds")List<Long> RolesIds);
}