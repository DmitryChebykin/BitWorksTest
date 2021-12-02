import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Resource {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Resource)) {
            return false;
        }

        Resource resource = (Resource) o;

        return getResourceId().equals(resource.getResourceId());
    }

    @Override
    public int hashCode() {
        return getResourceId().hashCode();
    }

    private Long resourceId;
}