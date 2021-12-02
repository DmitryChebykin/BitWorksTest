import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResourceServiceTest {
    private ResourceService resourceService;

    /**
     * This is integration test for task example
     */
    @Test
    void rearrangeResource() {
        Map<Resource, Owner> collect = new HashMap<>();

        LongStream.range(0, 10).mapToObj(Resource::new).forEach(e -> collect.put(e, null));

        assertThat(collect, aMapWithSize(10));

        Owner s1 = Owner.builder().name("S1").build();

        Set<Resource> hashSet = Stream.of(2L, 3L).map(Resource::new).collect(Collectors.toSet());

        resourceService = new ResourceService();
        resourceService.setResourceMap(collect);
        ResourceSetterDto resourceSetterDto = ResourceSetterDto.builder().isAddingOperation(true).isLowPriority(false).owner(s1).resourceSet(hashSet).build();

        resourceService.rearrangeResource(resourceSetterDto);

        Resource res1 = new Resource(2L);
        assertThat(collect, IsMapContaining.hasEntry(res1, s1));
        Resource res3 = new Resource(3L);
        assertThat(collect, IsMapContaining.hasEntry(res3, s1));

        long freeResourceCount = collect.values().stream().filter(Objects::isNull).count();

        assertThat(freeResourceCount, is(8L));

        hashSet = Stream.of(2L, 4L, 5L).map(Resource::new).collect(Collectors.toSet());
        Owner s2 = Owner.builder().name("S2").build();

        resourceSetterDto = ResourceSetterDto.builder().isAddingOperation(true).isLowPriority(false).owner(s2).resourceSet(hashSet).build();

        resourceService.rearrangeResource(resourceSetterDto);

        assertThat(collect, IsMapContaining.hasEntry(res1, s1));
        assertThat(collect, IsMapContaining.hasEntry(res3, s1));
        Resource res4 = new Resource(4L);
        assertThat(collect, IsMapContaining.hasEntry(res4, s2));
        Resource res5 = new Resource(5L);
        assertThat(collect, IsMapContaining.hasEntry(res5, s2));

        freeResourceCount = collect.values().stream().filter(Objects::isNull).count();

        assertThat(freeResourceCount, is(6L));

        hashSet = new HashSet<>() {{
            add(res1);
        }};

        Owner s3 = Owner.builder().name("S3").build();
        resourceSetterDto = ResourceSetterDto.builder().isAddingOperation(true).isLowPriority(false).owner(s3).resourceSet(hashSet).build();

        resourceService.rearrangeResource(resourceSetterDto);

        freeResourceCount = collect.values().stream().filter(Objects::isNull).count();

        assertThat(freeResourceCount, is(6L));

        assertThat(collect, IsMapContaining.hasEntry(res1, s3));
        assertThat(collect, IsMapContaining.hasEntry(res3, s1));
        assertThat(collect, IsMapContaining.hasEntry(res4, s2));
        assertThat(collect, IsMapContaining.hasEntry(res5, s2));

        hashSet = Stream.of(2L, 4L, 5L).map(Resource::new).collect(Collectors.toSet());

        resourceSetterDto = ResourceSetterDto.builder().isAddingOperation(false).isLowPriority(false).owner(s2).resourceSet(hashSet).build();

        resourceService.rearrangeResource(resourceSetterDto);

        freeResourceCount = collect.values().stream().filter(Objects::isNull).count();

        assertThat(freeResourceCount, is(8L));
        assertThat(collect, IsMapContaining.hasEntry(res1, s3));
        assertThat(collect, IsMapContaining.hasEntry(res3, s1));

        hashSet = Stream.of(2L, 3L).map(Resource::new).collect(Collectors.toSet());

        resourceSetterDto = ResourceSetterDto.builder().isAddingOperation(true).isLowPriority(true).owner(Owner.builder().name("S4").build()).resourceSet(hashSet).build();

        resourceService.rearrangeResource(resourceSetterDto);

        freeResourceCount = collect.values().stream().filter(Objects::isNull).count();

        assertThat(freeResourceCount, is(8L));
        assertThat(collect, IsMapContaining.hasEntry(res1, s3));
        assertThat(collect, IsMapContaining.hasEntry(res3, s1));
    }

    @Test
    void getExistingOwners() {
        Map<Resource, Owner> collect = new HashMap<>();
        LongStream.range(0, 10).mapToObj(Resource::new).forEach(e -> collect.put(e, null));

        Owner s1 = Owner.builder().name("S1").build();

        collect.put(new Resource(2L), s1);
        collect.put(new Resource(3L), s1);

        resourceService = new ResourceService();

        resourceService.setResourceMap(collect);

        assertThat(resourceService.getExistingOwners(), hasSize(1));

        Owner s2 = Owner.builder().name("S2").build();

        collect.put(new Resource(4L), s2);
        collect.put(new Resource(6L), s2);

        assertThat(resourceService.getExistingOwners(), hasSize(2));

        assertThat(resourceService.getExistingOwners(), hasItems(s1, s2));
    }

    @Test
    void getOwnedResources() {
    }

    @Test
    void getModifyingOwners() {
        Map<Resource, Owner> collect = new HashMap<>();
        LongStream.range(0, 10).mapToObj(Resource::new).forEach(e -> collect.put(e, null));

        Owner s1 = Owner.builder().name("S1").build();

        collect.put(new Resource(2L), s1);
        collect.put(new Resource(3L), s1);

        Owner s2 = Owner.builder().name("S2").build();

        collect.put(new Resource(4L), s2);
        collect.put(new Resource(6L), s2);

        resourceService = new ResourceService();

        resourceService.setResourceMap(collect);

        HashSet<Resource> hashSet = new HashSet<>(Arrays.asList(new Resource(3L), new Resource(6L)));

        Owner s3 = Owner.builder().name("S3").build();

        ResourceSetterDto resourceSetterDto = ResourceSetterDto.builder().isAddingOperation(true).isLowPriority(false).owner(s3).resourceSet(hashSet).build();

        Set<Owner> modifyingOwners = resourceService.getModifyingOwners(resourceSetterDto);

        assertThat(modifyingOwners, hasSize(3));
        assertThat(modifyingOwners, hasItems(s2, s1, s3));
    }

    @Test
    void getFirstTimeOwnedResource() {
        Map<Resource, Owner> collect = new HashMap<>();
        LongStream.range(0, 10).mapToObj(Resource::new).forEach(e -> collect.put(e, null));

        Owner s1 = Owner.builder().name("S1").build();

        collect.put(new Resource(2L), s1);
        collect.put(new Resource(3L), s1);

        Owner s2 = Owner.builder().name("S2").build();

        collect.put(new Resource(4L), s2);
        collect.put(new Resource(6L), s2);

        resourceService = new ResourceService();

        resourceService.setResourceMap(collect);

        HashSet<Resource> hashSet = new HashSet<>(Arrays.asList(new Resource(3L), new Resource(6L), new Resource(8L)));

        Owner s3 = Owner.builder().name("S3").build();

        ResourceSetterDto resourceSetterDto = ResourceSetterDto.builder().isAddingOperation(true).isLowPriority(false).owner(s3).resourceSet(hashSet).build();

        Set<Resource> firstTimeOwnedResource = resourceService.getFirstTimeOwnedResources(resourceSetterDto);

        assertThat(firstTimeOwnedResource, hasSize(1));
        assertThat(firstTimeOwnedResource, contains(hasProperty("resourceId", is(8L))));
    }

    @Test
    void getNewOwner() {
        Map<Resource, Owner> collect = new HashMap<>();
        LongStream.range(0, 10).mapToObj(Resource::new).forEach(e -> collect.put(e, null));

        Owner s1 = Owner.builder().name("S1").build();

        collect.put(new Resource(2L), s1);
        collect.put(new Resource(3L), s1);

        Owner s2 = Owner.builder().name("S2").build();

        collect.put(new Resource(4L), s2);
        collect.put(new Resource(6L), s2);

        resourceService = new ResourceService();

        resourceService.setResourceMap(collect);

        HashSet<Resource> hashSet = new HashSet<>(Arrays.asList(new Resource(3L), new Resource(6L), new Resource(8L)));

        Owner s3 = Owner.builder().name("S3").build();

        ResourceSetterDto resourceSetterDto = ResourceSetterDto.builder().isAddingOperation(true).isLowPriority(false).owner(s3).resourceSet(hashSet).build();

        Optional<Owner> option = resourceService.getNewOwner(resourceSetterDto);

        assertTrue(option.isPresent());

        assertThat(option.get(), is(s3));
    }
}