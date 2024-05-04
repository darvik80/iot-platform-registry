package xyz.crearts.registry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import xyz.crearts.registry.entity.RegistryDo;

/**
 * @author ivan.kishchenko
 * email ivan.kishchenko@lazada.com
 */
@Repository
public interface RegistryRepository extends JpaSpecificationExecutor<RegistryDo>, JpaRepository<RegistryDo, Long> {

}
