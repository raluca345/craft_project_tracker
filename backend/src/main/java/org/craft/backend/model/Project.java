package org.craft.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.craft.backend.enums.Status;

import java.util.List;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Table(check = @CheckConstraint(name = "amount_used_non_negative", constraint = "amount_used >= 0"), name = "projects")
public class Project {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String patternName;

    @Column(nullable = false)
    private String craft;

    @Column(nullable = false)
    private String toolType;

    private String yarnWeightCategory;

    private String yarnUsed;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private int amountUsed;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.NOT_STARTED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private String imageUrl;

    @ManyToMany(mappedBy = "projects")
    private List<Tag> tags;
}
