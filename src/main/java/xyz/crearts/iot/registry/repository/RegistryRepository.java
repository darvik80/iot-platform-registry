package xyz.crearts.iot.registry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import xyz.crearts.iot.registry.entity.DeviceDO;
import xyz.crearts.iot.registry.entity.RegistryDO;

import java.util.List;

@Repository
public interface RegistryRepository extends JpaSpecificationExecutor<RegistryDO>, JpaRepository<RegistryDO, Long> {
    List<RegistryDO> getByName(String name);

    RegistryDO findFirstByName(String name);
}