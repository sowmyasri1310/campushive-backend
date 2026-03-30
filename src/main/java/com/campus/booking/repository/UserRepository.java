package com.campus.booking.repository;

import com.campus.booking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // All users in a branch (for branch admin)
    List<User> findByBranchOrderByRoleAscNameAsc(String branch);

    // By branch AND role (faculty of a branch, students of a branch)
    List<User> findByBranchAndRoleOrderByNameAsc(String branch, User.Role role);

    // All admins (for super admin view)
    List<User> findByRoleOrderByBranchAscNameAsc(User.Role role);
}