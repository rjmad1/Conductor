package com.conductor.shared.middleware.tenant;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = UUID.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public abstract class TenantAwareEntity {

  @Column(name = "tenant_id", nullable = false, updatable = false)
  private UUID tenantId;

  public UUID getTenantId() {
    return tenantId;
  }

  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  @PrePersist
  public void prePersist() {
    if (this.tenantId == null) {
      UUID currentTenant = TenantContext.getCurrentTenantId();
      if (currentTenant == null) {
        throw new IllegalStateException(
            "Cannot persist tenant-scoped entity without active TenantContext");
      }
      this.tenantId = currentTenant;
    }
  }
}
