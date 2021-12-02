import lombok.*;
import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceService {
    private Map<Resource, Owner> resourceMap;

    public void rearrangeResource(ResourceSetterDto resourceSetterDto) {
        Set<Resource> firstTimeOwnedResources = getFirstTimeOwnedResources(resourceSetterDto);

        Set<Resource> reassignedResources = resourceSetterDto.getResourceSet().stream().filter(n -> !firstTimeOwnedResources.contains(n)).collect(Collectors.toSet());

        Owner dtoOwner = resourceSetterDto.getOwner();

        if (!resourceSetterDto.isAddingOperation()) {
            reassignedResources.forEach(e -> {
                Owner owner = resourceMap.get(e);
                if (owner.equals(dtoOwner)) {
                    resourceMap.put(e, null);
                }
            });
            return;
        }

        Map<Owner, List<Resource>> listMap = reassignedResources.stream().collect(Collectors.groupingBy((e -> resourceMap.get(e))));

        final int[] minResourceSize = {firstTimeOwnedResources.size()};

        reassignedResources.forEach(e -> {
            Owner owner = resourceMap.get(e);
            List<Resource> resources = listMap.get(owner);

            if ((resources.size() - 1) >= minResourceSize[0] && !resourceSetterDto.isLowPriority()) {
                resourceMap.put(e, dtoOwner);
                minResourceSize[0]++;
            }
        });

        firstTimeOwnedResources.forEach(e -> resourceMap.put(e, dtoOwner));
    }

    public Set<Owner> getExistingOwners() {

        return resourceMap.values()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<Owner> getModifyingOwners(ResourceSetterDto resourceSetterDto) {
        Set<Resource> resourceSet = resourceSetterDto.getResourceSet();

        Set<Resource> resources = resourceMap.keySet();

        Set<Owner> ownerSet = resources.stream().filter(resourceSet::contains).map(e -> resourceMap.get(e)).collect(Collectors.toSet());

        ownerSet.add(resourceSetterDto.getOwner());

        return ownerSet;
    }

    public Set<Resource> getFirstTimeOwnedResources(ResourceSetterDto resourceSetterDto) {

        return resourceMap.entrySet().stream().filter(e -> e.getValue() == null && resourceSetterDto.getResourceSet().contains(e.getKey()))
                .map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    public Optional<Owner> getNewOwner(ResourceSetterDto resourceSetterDto) {
        Set<Owner> existingOwners = getExistingOwners();
        Owner owner = resourceSetterDto.getOwner();

        if (!existingOwners.contains(owner)) {
            return Optional.of(owner);
        }

        return Optional.empty();
    }
}