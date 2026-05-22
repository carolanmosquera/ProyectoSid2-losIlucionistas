package co.icesi.UniPlan.service;

import co.icesi.UniPlan.exception.BusinessException;
import co.icesi.UniPlan.exception.ResourceNotFoundException;
import co.icesi.UniPlan.model.mongo.Role;
import co.icesi.UniPlan.repository.mongo.RoleRepository;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(MongoTemplate.class)
public class RoleService {

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role findById(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + id));
    }

    public Role create(Role role) {
        if (role.getName() == null || role.getName().isBlank()) {
            throw new BusinessException("El rol debe tener nombre");
        }
        roleRepository.findByName(role.getName()).ifPresent(existing -> {
            throw new BusinessException("Ya existe un rol con ese nombre");
        });
        return roleRepository.save(role);
    }

    public Role update(String id, Role role) {
        Role current = findById(id);
        current.setName(role.getName());
        current.setDescription(role.getDescription());
        current.setPermissions(role.getPermissions());
        return roleRepository.save(current);
    }
}
