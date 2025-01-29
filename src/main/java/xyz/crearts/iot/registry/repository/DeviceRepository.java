package xyz.crearts.iot.registry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import xyz.crearts.iot.registry.entity.DeviceDO;

@Repository
public interface  DeviceRepository extends JpaSpecificationExecutor<DeviceDO>, JpaRepository<DeviceDO, Long> {
    DeviceDO findFirstByClientIdAndProductId(String clientId, long productId);
    DeviceDO findFirstBySessionId(String sessionId);
    DeviceDO findFirstByNameAndProductId(String name, long productId);
    DeviceDO findFirstByNameAndProduct_Name(String name, String productName);
}