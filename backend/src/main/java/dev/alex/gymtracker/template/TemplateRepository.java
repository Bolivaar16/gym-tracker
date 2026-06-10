package dev.alex.gymtracker.template;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {

    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("""
            SELECT t FROM Template t
            LEFT JOIN FETCH t.exercises te
            LEFT JOIN FETCH te.exercise
            WHERE t.id = :id
            """)
    Optional<Template> findByIdWithDetail(Long id);

    @Query(value = """
            SELECT t.id, t.name, t.notes, COUNT(te.id) AS exerciseCount
            FROM template t
            LEFT JOIN template_exercise te ON te.template_id = t.id
            GROUP BY t.id, t.name, t.notes
            ORDER BY t.name ASC
            """, nativeQuery = true)
    List<TemplateSummaryRow> findSummaries();
}
