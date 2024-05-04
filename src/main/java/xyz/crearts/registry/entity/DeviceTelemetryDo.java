package xyz.crearts.registry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Getter
@Setter
@Entity
@Table(name="device_telemetry")
public class DeviceTelemetryDo {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    @CreationTimestamp
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    private DeviceDo device;

    @JdbcTypeCode(SqlTypes.JSON)
    private String jsonData;
}
