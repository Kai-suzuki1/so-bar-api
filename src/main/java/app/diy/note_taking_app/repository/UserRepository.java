package app.diy.note_taking_app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.diy.note_taking_app.domain.dto.UserDetailResponse;
import app.diy.note_taking_app.domain.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	Optional<UserDetailResponse> findById(int id);
}
