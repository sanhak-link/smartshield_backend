package sanhak.smartshield.report;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentReportRepository extends JpaRepository<IncidentReport, Long> {

    // EventLog의 DB id로 조회 (1:1 가정)
    Optional<IncidentReport> findByEventLogId(Long eventLogId);

    // 가장 최근에 생성된 IncidentReport 한 건
    Optional<IncidentReport> findTopByOrderByCreatedAtDesc();

    // eventId(문자열) 기준으로 찾고 싶을 때 편하게 사용 가능
    Optional<IncidentReport> findByEventLog_EventId(String eventId);

    // 여러 event에 대한 보고서를 한 번에 가져올 때
    List<IncidentReport> findAllByEventLogIdIn(List<Long> eventLogIds);
}

