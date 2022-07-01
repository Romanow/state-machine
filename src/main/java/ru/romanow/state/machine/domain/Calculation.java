package ru.romanow.state.machine.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ru.romanow.state.machine.domain.enums.CalculationType;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "calculation")
@EntityListeners(AuditingEntityListener.class)
public class Calculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", nullable = false, updatable = false)
    private UUID uid;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private CalculationType type;

    @OneToMany(mappedBy = "calculation")
    private List<CalculationStatus> statuses;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "modified_date", nullable = false)
    private LocalDateTime modifiedDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Calculation that = (Calculation) o;

        return new EqualsBuilder().append(uid, that.uid).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(uid).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("uid", uid)
                .append("name", name)
                .append("description", description)
                .append("type", type)
                .append("createdDate", createdDate)
                .append("modifiedDate", modifiedDate)
                .toString();
    }

}
