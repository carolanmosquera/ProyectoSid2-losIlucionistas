package co.icesi.UniPlan.service;

import co.icesi.UniPlan.exception.BusinessException;
import co.icesi.UniPlan.exception.ResourceNotFoundException;
import co.icesi.UniPlan.model.mongo.Organization;
import co.icesi.UniPlan.repository.mongo.OrganizationRepository;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnBean(MongoTemplate.class)
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    public List<Organization> findAll() {
        return organizationRepository.findAll();
    }

    public Organization findById(String id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organizacion no encontrada: " + id));
    }

    public Organization create(Organization organization) {
        if (organization.getName() == null || organization.getName().isBlank()) {
            throw new BusinessException("La organizacion debe tener nombre");
        }
        return organizationRepository.save(organization);
    }

    public Organization update(String id, Organization organization) {
        Organization current = findById(id);
        current.setName(organization.getName());
        current.setDescription(organization.getDescription());
        current.setInChargeStudents(organization.getInChargeStudents());
        return organizationRepository.save(current);
    }
}
