package com.postgresql.StudentMarket.Repository;

import com.postgresql.StudentMarket.Entities.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
}
