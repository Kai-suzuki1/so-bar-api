package app.diy.note_taking_app.domain.entity;

import com.fasterxml.jackson.databind.ObjectMapper;

import app.diy.note_taking_app.domain.dto.PermissionType;
import app.diy.note_taking_app.exceptions.JsonConversionFailureException;
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "user_permissions")
public class UserPermission {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "note_id", nullable = false) // FK
	private Note note;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false) // FK
	private User user;

	@Column(nullable = false)
	private byte[] hash;

	@Column(nullable = false)
	private String type;

	@Column(nullable = false)
	private String invitedUserName;

	@Column(nullable = false)
	private boolean deletedFlag;

	@Column(nullable = false)
	private boolean acceptedFlag;

	public PermissionType toPermissionType() {
		try {
			return new ObjectMapper().readValue(type, PermissionType.class);
		} catch (Exception e) {
			throw new JsonConversionFailureException(e.getMessage(), e);
		}
	}
}
