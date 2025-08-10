package xyz.crearts.iot.registry.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import xyz.crearts.iot.registry.repository.RegistryRepository;
import xyz.crearts.iot.registry.rpc.JsonRpcDispatcher;

@Service
public class RegistryService {
    public RegistryService(RegistryRepository registryRepository, ObjectMapper mapper, JsonRpcDispatcher rpcDispatcher) {
        rpcDispatcher.register("getDeviceRegistries", params -> mapper.valueToTree(registryRepository.findAll()));
    }
}
