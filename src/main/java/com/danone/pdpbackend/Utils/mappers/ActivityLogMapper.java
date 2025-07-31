package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.entities.ActivityLog;
import com.danone.pdpbackend.entities.dto.ActivityLogDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


import java.util.List;

@Mapper(componentModel = "spring")
public abstract class ActivityLogMapper {
    //toDTO
    //toEntity
    //updateEntity
    //updateDTO
    //toDTOList
    //toEntityList

    public abstract ActivityLogDTO toDTO(ActivityLog activityLog);
    public abstract ActivityLog toEntity(ActivityLogDTO activityLogDTO);

    @Mapping(target = "id", ignore = true)
    public abstract void updateEntity(ActivityLogDTO activityLogDTO,@MappingTarget ActivityLog activityLog);
    @Mapping(target = "id", ignore = true)
    public abstract void updateDTO(ActivityLog activityLog, @MappingTarget ActivityLogDTO activityLogDTO);

    public abstract List<ActivityLogDTO> toDTOList(List<ActivityLog> activityLogs);
    public abstract List<ActivityLog> toEntityList(List<ActivityLogDTO> activityLogDTOs);

}
