package com.danone.pdpbackend.Utils.mappers;

import com.danone.pdpbackend.entities.Notification;
import com.danone.pdpbackend.entities.dto.NotificationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public abstract class NotificationMapper {
    public abstract NotificationDTO toDTO(Notification notification);
    public abstract Notification toEntity(NotificationDTO notificationDTO);

    @Mapping(target = "id", ignore = true)
    public abstract void updateEntity(NotificationDTO notificationDTO,@MappingTarget Notification notification);
    @Mapping(target = "id", ignore = true)
    public abstract void updateDTO(Notification notification, @MappingTarget NotificationDTO notificationDTO);
    public abstract List<NotificationDTO> toDTOList(List<Notification> notifications);
    public abstract List<Notification> toEntityList(List<NotificationDTO> notificationDTOs);

}
