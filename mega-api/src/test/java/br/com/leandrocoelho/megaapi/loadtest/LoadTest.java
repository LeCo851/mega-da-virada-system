package br.com.leandrocoelho.megaapi.loadtest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadTest {

    // CONFIGURAÇÕES DO ATAQUE
    private static final int TOTAL_REQUESTS = 200_000; // Total de tiros
    private static final int CONCURRENCY_LIMIT = 500; // Máximo de conexões simultâneas (Semáforo)

    // Alvo
    private static final String URL = "http://localhost:8080/bets/redis";
    //private static final String URL = "http://localhost:8080/bets/kafka";

    private static final String JSON_BODY = """
        {
            "userId": 12345,
            "numbers": [1, 10, 20, 30, 40, 50]
        }
    """;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("🔥 INICIANDO ATAQUE CONTROLADO: " + URL);
        System.out.println("🎯 Total: " + TOTAL_REQUESTS);
        System.out.println("🚦 Concorrência Máxima: " + CONCURRENCY_LIMIT);

        // Executor de Virtual Threads (Muda a estratégia para não fritar o SO)
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        // O SEMÁFORO: Impede que abramos mais conexões do que o Windows aguenta
        Semaphore semaphore = new Semaphore(CONCURRENCY_LIMIT);

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            // Adquire permissão. Se tiver 500 rodando, ele espera uma terminar.
            semaphore.acquire();

            executor.submit(() -> {
                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(URL))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(JSON_BODY))
                            .timeout(Duration.ofSeconds(5))
                            .build();

                    HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

                    if (response.statusCode() == 202) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    if (errorCount.get() == 0) {
                        System.out.println("❌ Erro: " + e.getMessage());
                    }
                    errorCount.incrementAndGet();
                } finally {
                    // Libera a permissão para o próximo
                    semaphore.release();
                }
            });
        }

        // Aguarda todos terminarem
        while (successCount.get() + errorCount.get() < TOTAL_REQUESTS) {
            Thread.sleep(100);
        }

        long end = System.currentTimeMillis();
        long durationMs = end - start;
        double seconds = durationMs / 1000.0;
        long rps = (long) (TOTAL_REQUESTS / seconds);

        System.out.println("\n🛑 FIM DO TESTE 🛑");
        System.out.println("--------------------------------");
        System.out.println("Tempo Total: " + seconds + " s");
        System.out.println("Sucessos: " + successCount.get());
        System.out.println("Erros: " + errorCount.get());
        System.out.println("--------------------------------");
        System.out.println("🚀 RPS (Requests Por Segundo): " + rps);
        System.out.println("--------------------------------");

        executor.shutdown();
    }
}