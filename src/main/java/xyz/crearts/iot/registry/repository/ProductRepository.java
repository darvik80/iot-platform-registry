package xyz.crearts.iot.registry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import xyz.crearts.iot.registry.entity.ProductDO;

@Repository
public interface ProductRepository extends JpaSpecificationExecutor<ProductDO>, JpaRepository<ProductDO, Long> {
    ProductDO findFirstByNameAndRegistryId(String name, long registryId);
    ProductDO findFirstByNameAndRegistry_Name(String name, String registry);
}