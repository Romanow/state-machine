package ru.romanow.state.machine.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ru.romanow.state.machine.models.States;

@Getter
@Setter
@Accessors(chain = true)
@Entity
@Table(name = "calculation_status")
@EntityListeners(AuditingEntityListener.class)
public class CalculationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private States status;

    @ManyToOne
    @JoinColumn(name = "calculation_id", foreignKey = @ForeignKey(name = "fk_calculation_status_calculation_id"))
    private Calculation calculation;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final CalculationStatus that = (CalculationStatus) o;

        return new EqualsBuilder().append(id, that.id)
                                  .append(status, that.status)
                                  .append(createdDate, that.createdDate)
                                  .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id)
                                          .append(status)
                                          .append(createdDate)
                                          .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("status", status)
                .append("createdDate", createdDate)
                .toString();
    }

}
