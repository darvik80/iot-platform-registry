package xyz.crearts.registry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import xyz.crearts.registry.entity.DeviceDo;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
public interface  DeviceRepository extends JpaSpecificationExecutor<DeviceDo>, JpaRepository<DeviceDo, Long> {
}
