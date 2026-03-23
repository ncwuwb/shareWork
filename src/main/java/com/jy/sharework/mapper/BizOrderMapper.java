package com.jy.sharework.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jy.sharework.dto.AppOrderView;
import com.jy.sharework.entity.BizOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface BizOrderMapper extends BaseMapper<BizOrder> {

    @Select("SELECT o.id, o.order_no AS orderNo, o.workstation_id AS workstationId, " +
            "w.code AS workstationCode, " +
            "s4.center_longitude AS signCenterLongitude, s4.center_latitude AS signCenterLatitude, " +
            "o.status, o.start_time AS startTime, o.end_time AS endTime, " +
            "o.sign_time AS signTime, o.actual_end_time AS actualEndTime, o.create_time AS createTime, " +
            "s4.name AS areaName, s3.name AS floorName, s2.name AS buildingName, s1.name AS campusName, " +
            "CONCAT_WS(' / ', s1.name, s2.name, s3.name, s4.name) AS spacePath " +
            "FROM biz_order o " +
            "LEFT JOIN biz_workstation w ON o.workstation_id = w.id AND w.is_deleted = 0 " +
            "LEFT JOIN biz_space s4 ON w.space_id = s4.id AND s4.is_deleted = 0 " +
            "LEFT JOIN biz_space s3 ON s4.parent_id = s3.id AND s3.is_deleted = 0 " +
            "LEFT JOIN biz_space s2 ON s3.parent_id = s2.id AND s2.is_deleted = 0 " +
            "LEFT JOIN biz_space s1 ON s2.parent_id = s1.id AND s1.is_deleted = 0 " +
            "WHERE o.is_deleted = 0 AND o.user_id = #{userId} " +
            "ORDER BY o.create_time DESC")
    Page<AppOrderView> selectViewPage(Page<AppOrderView> page, @Param("userId") Long userId);

    @Select("SELECT o.id, o.order_no AS orderNo, o.workstation_id AS workstationId, " +
            "w.code AS workstationCode, " +
            "s4.center_longitude AS signCenterLongitude, s4.center_latitude AS signCenterLatitude, " +
            "o.status, o.start_time AS startTime, o.end_time AS endTime, " +
            "o.sign_time AS signTime, o.actual_end_time AS actualEndTime, o.create_time AS createTime, " +
            "s4.name AS areaName, s3.name AS floorName, s2.name AS buildingName, s1.name AS campusName, " +
            "CONCAT_WS(' / ', s1.name, s2.name, s3.name, s4.name) AS spacePath " +
            "FROM biz_order o " +
            "LEFT JOIN biz_workstation w ON o.workstation_id = w.id AND w.is_deleted = 0 " +
            "LEFT JOIN biz_space s4 ON w.space_id = s4.id AND s4.is_deleted = 0 " +
            "LEFT JOIN biz_space s3 ON s4.parent_id = s3.id AND s3.is_deleted = 0 " +
            "LEFT JOIN biz_space s2 ON s3.parent_id = s2.id AND s2.is_deleted = 0 " +
            "LEFT JOIN biz_space s1 ON s2.parent_id = s1.id AND s1.is_deleted = 0 " +
            "WHERE o.is_deleted = 0 AND o.user_id = #{userId} AND o.id = #{orderId}")
    AppOrderView selectViewById(@Param("userId") Long userId, @Param("orderId") Long orderId);

    @Select("SELECT COUNT(1) FROM biz_order WHERE workstation_id=#{wid} AND is_deleted=0 " +
            "AND status IN (0,1) AND start_time < #{end} AND end_time > #{start}")
    int countWorkstationOverlap(@Param("wid") Long workstationId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    @Select("SELECT COUNT(1) FROM biz_order WHERE user_id=#{uid} AND is_deleted=0 " +
            "AND status IN (0,1) AND start_time < #{end} AND end_time > #{start}")
    int countUserOverlap(@Param("uid") Long userId,
                         @Param("start") LocalDateTime start,
                         @Param("end") LocalDateTime end);

    @Select("SELECT * FROM biz_order WHERE is_deleted=0 AND status=0 AND start_time <= #{deadline}")
    List<BizOrder> listPendingSignBefore(@Param("deadline") LocalDateTime deadline);

    @Select("SELECT COUNT(1) FROM biz_order WHERE is_deleted=0 AND DATE(start_time)=CURDATE()")
    int countTodayOrders();

    @Select("SELECT COUNT(1) FROM biz_order WHERE is_deleted=0 AND DATE(start_time)=CURDATE() AND status IN (1,2) AND sign_time IS NOT NULL")
    int countTodaySigned();

    @Select("SELECT COUNT(1) FROM biz_order WHERE is_deleted=0 AND status=4 AND create_time >= #{from}")
    int countBreachSince(@Param("from") LocalDateTime from);

    @Select("SELECT w.space_id AS spaceId, COUNT(1) AS cnt FROM biz_order o JOIN biz_workstation w ON o.workstation_id=w.id " +
            "WHERE o.is_deleted=0 AND w.is_deleted=0 AND o.create_time >= #{from} GROUP BY w.space_id ORDER BY cnt DESC LIMIT 3")
    List<Map<String, Object>> hotRegions(@Param("from") LocalDateTime from);

    @Select("SELECT workstation_id AS workstationId, COUNT(1) AS cnt FROM biz_order WHERE is_deleted=0 AND create_time >= #{from} " +
            "GROUP BY workstation_id ORDER BY cnt DESC LIMIT 10")
    List<Map<String, Object>> hotWorkstations(@Param("from") LocalDateTime from);

    @Select("SELECT u.dept_id AS deptId, SUM(CASE WHEN o.status=4 THEN 1 ELSE 0 END) AS breach, COUNT(o.id) AS total " +
            "FROM biz_order o JOIN sys_user u ON o.user_id=u.id AND u.is_deleted=0 " +
            "WHERE o.is_deleted=0 AND o.create_time >= #{from} AND u.dept_id IS NOT NULL " +
            "GROUP BY u.dept_id")
    List<Map<String, Object>> deptBreachStats(@Param("from") LocalDateTime from);
}
