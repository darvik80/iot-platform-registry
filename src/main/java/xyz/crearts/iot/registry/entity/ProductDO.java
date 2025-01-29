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

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Getter
@Setter
@Entity(name = "product")
@Table(name = "product", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"registry_id", "name"}),
})
public class ProductDO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> metadata;

    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> configuration;
}
