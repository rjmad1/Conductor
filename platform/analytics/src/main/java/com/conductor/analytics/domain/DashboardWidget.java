package com.conductor.analytics.domain;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

/** Individual widget within a dashboard. Stores metric query configuration and position. */
@Entity
@Table(name = "analytics_dashboard_widgets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardWidget {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dashboard_id", nullable = false)
  private Dashboard dashboard;

  @Column(nullable = false)
  private String widgetType;

  @Column(nullable = false)
  private String metricQuery;

  private int positionX;
  private int positionY;
  private int width;
  private int height;

  @Column(columnDefinition = "TEXT")
  private String configJson;
}
