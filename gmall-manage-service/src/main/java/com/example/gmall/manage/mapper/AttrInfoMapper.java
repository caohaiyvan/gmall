package com.example.gmall.manage.mapper;

import com.example.gmall.bean.PmsBaseAttrInfo;
import com.example.gmall.bean.PmsBaseAttrValue;
import org.apache.ibatis.annotations.*;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface AttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    @Select("select *, #{valueIdStr} valueIdStr from pms_base_attr_info where id in (${attrIdStr})")
    @Results(
            value = {
                @Result(property = "id", column = "id", id = true),
                @Result(property = "attrValueList", javaType=List.class, many =@Many(select="selectBaseAttrValue"), column = "{id=id,valueIdStr=valueIdStr}")
            }
            )

    List<PmsBaseAttrInfo> selectSkuBaseAttrInfo(@Param("attrIdStr") String attrIdStr, @Param("valueIdStr") String valueIdStr);

    @Select("select * from pms_base_attr_value where attr_id = #{id} and id in (${valueIdStr})")
    List<PmsBaseAttrValue> selectBaseAttrValue(@Param("id") String id, @Param("valueIdStr") String valueIdStr);

}
