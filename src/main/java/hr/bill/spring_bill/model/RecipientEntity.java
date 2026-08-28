package hr.bill.spring_bill.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "recipient")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;
}
