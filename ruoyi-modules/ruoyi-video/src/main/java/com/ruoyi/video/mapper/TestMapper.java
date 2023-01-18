package com.ruoyi.video.mapper;

import com.ruoyi.system.api.domain.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

@Mapper
public interface TestMapper {

    public List<HashMap> UserAll();
}
