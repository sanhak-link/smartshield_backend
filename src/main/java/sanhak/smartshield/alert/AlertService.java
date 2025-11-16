package sanhak.smartshield.alert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class AlertService {

    // Thread-safe한 리스트 사용 (잘 하셨습니다)
    private final List<SseEmitter> clients = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public AlertService() {
        // 10초마다 heartbeat 전송
        scheduler.scheduleAtFixedRate(this::sendHeartbeat, 10, 10, TimeUnit.SECONDS);
    }

    public SseEmitter subscribe() {
        // 타임아웃 0L (무제한)은 Heartbeat가 확실하면 괜찮습니다.
        SseEmitter emitter = new SseEmitter(0L);
        clients.add(emitter);

        // 콜백에서는 리스트에서 제거만 수행합니다.
        emitter.onCompletion(() -> {
            clients.remove(emitter);
            log.debug("SSE completed - client removed");
        });

        emitter.onTimeout(() -> {
            clients.remove(emitter);
            emitter.complete();
            log.debug("SSE timeout - client removed");
        });

        emitter.onError((ex) -> {
            clients.remove(emitter);
            // 이미 에러가 난 상태이므로 emitter.complete()를 호출하지 않는 것이 안전합니다.
            log.debug("SSE error - client removed: {}", ex.getMessage());
        });

        // [선택 사항] 초기 연결 시 더미 데이터 전송 (연결 확인용)
        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (Exception e) {
            // 초기 전송 실패 시 바로 제거
            clients.remove(emitter);
        }

        return emitter;
    }

    private void sendHeartbeat() {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event()
                        .name("heartbeat")
                        .data("keepalive"));
            } catch (Exception ex) {
                // [중요 수정] Broken Pipe 발생 시 그냥 리스트에서 빼버려야 합니다.
                // 여기서 emitter.complete()를 호출하면 IllegalStateException이 발생합니다.
                clients.remove(emitter);
                
                // Broken Pipe는 흔한 일이므로 warn 대신 debug 레벨 권장
                log.debug("Heartbeat failed (Client disconnected), removing emitter.");
            }
        }
    }

    public void broadcast(Object payload) {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event()
                        .name("alert")
                        .data(payload));
            } catch (Exception ex) {
                // [중요 수정] 여기도 마찬가지로 제거만 수행
                clients.remove(emitter);
                log.debug("Alert send failed, removing emitter: {}", ex.getMessage());
            }
        }
    }

    public void signalCreated() { broadcastEvent("alert.created"); }
    public void signalResolved() { broadcastEvent("alert.resolved"); }

    private void broadcastEvent(String eventName) {
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventName)
                                .data("{}")
                );
            } catch (Exception ex) {
                // [중요 수정] 제거만 수행
                clients.remove(emitter);
                log.debug("{} send failed, removing emitter.", eventName);
            }
        }
    }
}