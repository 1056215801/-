package com.mit.community.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("access_card")
public class AccessCard extends BaseEntity{
    @TableField("card_num")
    private String cardNum;

    @TableField("household_id")
    private Integer houseHoldId;

    @TableField("device_num")
    private String deviceNum;

<<<<<<< .mine
<<<<<<< HEAD
    /*@TableField("dnake_device_info_id")
    private String dnakeDeviceInfoId;*/

    @TableField("is_upload")
    private int is_upload;//是否成功上传到机器；1否；2是
=======
    @TableField("dnake_device_info_id")
    private String dnakeDeviceInfoId;
=======
    /*@TableField("dnake_device_info_id")
    private String dnakeDeviceInfoId;*/

    @TableField("device_group_id")
    private Integer deviceGroupId;

    @TableField("is_upload")
    private int isUpload;//是否成功上传到机器；1否；2是

>>>>>>> .theirs

}