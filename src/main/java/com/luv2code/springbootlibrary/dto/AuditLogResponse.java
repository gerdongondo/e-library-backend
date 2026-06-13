package com.luv2code.springbootlibrary.dto;

import com.luv2code.springbootlibrary.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private Long userId;
    private String userEmail;
    private String userRole;
    private String actionType;
    private String entityType;
    private Long entityId;
    private String entityName;
    private String details;
    private String ipAddress;
    private LocalDateTime createdAt;

    // Méthode de conversion depuis l'entité
    public static AuditLogResponse fromEntity(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.getId(),
                auditLog.getUserId(),
                auditLog.getUserEmail(),
                auditLog.getUserRole(),
                auditLog.getActionType().name(),
                auditLog.getEntityType(),
                auditLog.getEntityId(),
                auditLog.getEntityName(),
                auditLog.getDetails(),
                auditLog.getIpAddress(),
                auditLog.getCreatedAt()
        );
    }
}