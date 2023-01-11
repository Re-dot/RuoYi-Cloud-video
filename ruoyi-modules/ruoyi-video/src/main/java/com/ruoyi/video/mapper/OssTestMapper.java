package com.ruoyi.video.mapper;

import com.ruoyi.system.api.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OssTestMapper {

    /**
     * 根据条件分页查询用户列表
     *
     * @param sysUser 用户信息
     * @return 用户信息集合信息
     */
    public List<SysUser> selectUserList(SysUser sysUser);
    @Select("select * from sys_user")
    public List<SysUser> testselectAll();

    public List<SysUser> selectAll();
}
