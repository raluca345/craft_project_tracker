package org.craft.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String patternName;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String craft;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String toolType;

    @Size(max = 100)
    private String yarnWeightCategory;

    @Size(max = 255)
    private String yarnUsed;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Min(0)
    private int amountUsed;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.NOT_STARTED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Size(max = 2048)
    private String imageUrl;

    @ManyToMany(mappedBy = "projects")
    private List<Tag> tags;
}
