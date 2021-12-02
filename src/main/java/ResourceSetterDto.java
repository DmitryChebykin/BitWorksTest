import lombok.*;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceSetterDto {
    boolean isAddingOperation;
    boolean isLowPriority;
    private Owner owner;
    private Set<Resource> resourceSet;
}