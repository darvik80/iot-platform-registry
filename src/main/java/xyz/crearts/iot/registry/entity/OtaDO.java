package xyz.crearts.iot.registry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Getter
@Setter
@Entity(name = "ota")
@Table(name = "ota", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"prodict_id", "version"}),
})
public class OtaDO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private ProductDO product;

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
    private String version;

    @Column
    private String link;

    @Column
    private Boolean status;
}
