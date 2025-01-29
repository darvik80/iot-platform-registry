package xyz.crearts.iot.registry.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import xyz.crearts.iot.registry.entity.OtaDO;

@Repository
public interface OtaRepository extends JpaSpecificationExecutor<OtaDO>, JpaRepository<OtaDO, Long> {
}