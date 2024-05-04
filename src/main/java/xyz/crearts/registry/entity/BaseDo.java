package xyz.crearts.registry.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@MappedSuperclass
@Getter
@Setter
public class BaseDo {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    @CreationTimestamp
    @ColumnDefault("current_timestamp")
    private LocalDateTime createdAt;

    @Column
    @ColumnDefault("'system'")
    private String createdBy;

    @Column
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

}
