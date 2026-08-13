package mg.yoan.diploma.domain;


import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CourseAssignment {
    private final UUID id;
    private final Course course;
    private final Group group;
    private final Teacher teacher;

    public boolean isTaughtBy(UUID teacherId) {
        return teacher.getId().equals(teacherId);
    }
}