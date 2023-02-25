package app.diy.note_taking_app.domain.entity;

import app.diy.note_taking_app.domain.common.entity.TimeStampEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notes")
public class Note extends TimeStampEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String userPermissionId;

	private String title;

	private String contents;

	@Column(nullable = false)
	private boolean deletedFlag;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "created_by_id", updatable = false, nullable = false) // FK
	private User createdUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "updated_by_id", nullable = false) // FK
	private User updatedUser;
}
