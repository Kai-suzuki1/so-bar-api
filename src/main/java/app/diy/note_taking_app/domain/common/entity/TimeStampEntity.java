package app.diy.note_taking_app.domain.common.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class TimeStampEntity {

	@CreatedDate
	@Column(updatable = false)
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	protected LocalDateTime createdAt;

	@LastModifiedDate
	@Column()
	@JsonFormat(pattern = "yyyy/MM/dd HH:mm")
	protected LocalDateTime updatedAt;
}
