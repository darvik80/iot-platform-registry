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
@Entity(name = "registry")
@Table(name = "registry", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"name"})
})
public class RegistryDO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

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

    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> metadata;
}
