package xyz.crearts.iot.registry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import xyz.crearts.iot.registry.entity.converter.HashMapConverter;

import java.sql.Timestamp;
import java.util.Map;

@Getter
@Setter
@Entity(name = "device")
@Table(name = "device", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"registry_id", "name"}),
})
public class DeviceDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "registry_id")
    private RegistryDO registry;

    @Column
    @CreationTimestamp
    @ColumnDefault("current_timestamp")
    private Timestamp createdAt;

    @Column(updatable = false)
    @ColumnDefault("'system'")
    private String createdBy;

    @Column(insertable = false)
    @UpdateTimestamp
    private Timestamp updatedAt;

    @Column(insertable = false)
    private String updatedBy;

    @Column
    private String name;

    @Column
    private String type;

    @Column(nullable = false)
    @ColumnDefault("gen_random_uuid()")
    private String uuid;

    @Column(unique = true)
    private String clientId;

    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> metadata;

    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> configuration;

    @Column
    @Enumerated(EnumType.ORDINAL)
    DeviceStatus status;

    @Column(unique = true)
    String sessionId;
}
