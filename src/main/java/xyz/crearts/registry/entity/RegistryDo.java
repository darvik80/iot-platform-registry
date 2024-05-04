package xyz.crearts.registry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
@Table(name="registry")
public class RegistryDo extends BaseDo {
    @Column(nullable = false)
    private String name;

    @Column(unique=true, nullable=false)
    @ColumnDefault("gen_random_uuid()")
    private String uuid;

}
