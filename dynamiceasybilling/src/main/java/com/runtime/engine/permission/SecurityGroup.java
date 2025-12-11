package com.runtime.engine.permission;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.runtime.engine.util.JsonAttributesConverter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "security_groups")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private String tenantId;
    
    @ElementCollection
    @CollectionTable(name = "security_group_permissions", joinColumns = @JoinColumn(name = "security_group_id"))
    @Column(name = "permission")
    private List<String> permissions = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "security_group_members", joinColumns = @JoinColumn(name = "security_group_id"))
    @Column(name = "member_id")
    private List<String> members = new ArrayList<>();
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> attributes;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Convert(converter = JsonAttributesConverter.class)
    @Column(columnDefinition = "JSON")
    private Map<String, Object> metadata;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    private String createdBy;
    
    private String updatedBy;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public void addPermission(String permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission);
        }
    }
    
    public void removePermission(String permission) {
        permissions.remove(permission);
    }
    
    public void addMember(String memberId) {
        if (!members.contains(memberId)) {
            members.add(memberId);
        }
    }
    
    public void removeMember(String memberId) {
        members.remove(memberId);
    }
}
