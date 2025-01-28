package xyz.crearts.iot.registry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import xyz.crearts.iot.registry.entity.DeviceDO;

@Repository
public interface  DeviceRepository extends JpaSpecificationExecutor<DeviceDO>, JpaRepository<DeviceDO, Long> {
    DeviceDO findFirstByClientIdAndRegistryId(String clientId, long registryId);
    DeviceDO findFirstBySessionId(String sessionId);
    DeviceDO findFirstByNameAndRegistryId(String name, long registryId);
    DeviceDO findFirstByNameAndRegistry_Name(String name, String registry);
}