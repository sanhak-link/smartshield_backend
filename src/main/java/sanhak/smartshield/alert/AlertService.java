package sanhak.smartshield.alert;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AlertService {
    private final List<SseEmitter> clients = new CopyOnWriteArrayList<>();

    // 🔸 main.jsx 호환을 위한 “현재 활성 알림 여부”
    private final AtomicBoolean active = new AtomicBoolean(false);

    public SseEmitter subscribe() {
        SseEmitter e = new SseEmitter(30 * 60 * 1000L); // 30분
        clients.add(e);
        System.out.println("👥 [SSE] subscribers=" + clients.size());

        // 선택) 연결 직후 하트비트 한 번
        try { e.send(SseEmitter.event().comment("connected")); } catch (Exception ignore) {}

        e.onCompletion(() -> {
            clients.remove(e);
            System.out.println("👋 [SSE] completion, subscribers=" + clients.size());
        });
        e.onTimeout(() -> {
            clients.remove(e);
            System.out.println("⏱ [SSE] timeout, subscribers=" + clients.size());
        });
        e.onError((ex) -> {
            clients.remove(e);
            System.out.println("💥 [SSE] error=" + ex.getMessage() + ", subscribers=" + clients.size());
        });
        return e;
    }

    public void broadcast(Object payload) {
        System.out.println("📡 [SSE] broadcasting to " + clients.size() + " subscribers: " + payload);
        for (SseEmitter e : clients) {
            try {
                e.send(SseEmitter.event().name("alert").data(payload));
            } catch (IOException ex) {
                System.out.println("⚠️ [SSE] send failed: " + ex.getMessage());
                e.complete();
                clients.remove(e);
            }
        }
    }

    /** main.jsx용: 알림 발생 신호 */
    public void signalCreated() {
        active.set(true);
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter e : clients) {
            try {
                e.send(SseEmitter.event().name("alert.created").data("{}"));
            } catch (IOException ex) {
                e.complete();
                dead.add(e);
            }
        }
        clients.removeAll(dead);
    }

    /** main.jsx용: 알림 해제 신호 */
    public void signalResolved() {
        active.set(false);
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter e : clients) {
            try {
                e.send(SseEmitter.event().name("alert.resolved").data("{}"));
            } catch (IOException ex) {
                e.complete();
                dead.add(e);
            }
        }
        clients.removeAll(dead);
    }

    /** main.jsx 초기 동기화용 */
    public boolean isActive() {
        return active.get();
    }
}
