package xyz.crearts.registry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Getter
@Setter
@Entity
@Table(name="device")
public class DeviceDo extends BaseDo {
    @ManyToOne
    @JoinColumn(name = "registry_id")
    private RegistryDo registry;

    @Column(nullable = false)
    private String name;

    @Column(unique=true, nullable = false, updatable = false)
    @ColumnDefault("gen_random_uuid()")
    private String uuid;

    public enum Status {
        Unknown,
        Online,
        Offline,
    }

    @Enumerated(EnumType.STRING)
    private Status status;
}
