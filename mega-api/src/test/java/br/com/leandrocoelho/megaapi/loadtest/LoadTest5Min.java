package br.com.leandrocoelho.megaapi.loadtest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTest5Min {

    // 🎯 CONFIGURAÇÕES
    private static final int DURATION_MINUTES = 5; // Tempo de execução
    private static final int CONCURRENCY_LIMIT = 500; // Máximo de conexões simultâneas


    //private static final String URL = "http://localhost:8080/bets/redis";
    private static final String URL = "http://localhost:8080/bets/kafka";

    // ⚠️ JSON FIXO (EXATAMENTE COMO VOCÊ PEDIU)
    // Certifique-se que seu DTO no Java espera "userId" e "numbers"
    private static final String JSON_BODY = """
        {
            "userId": 12345,
            "numbers": [1, 10, 20, 30, 40, 50]
        }
    """;

    public static void main(String[] args) throws InterruptedException {
        long durationMillis = DURATION_MINUTES * 60 * 1000L;
        long endTime = System.currentTimeMillis() + durationMillis;

        System.out.println("🔥 INICIANDO ATAQUE DE 5 MINUTOS 🔥");
        System.out.println("🎯 Alvo: " + URL);
        System.out.println("📦 Payload Fixo: " + JSON_BODY.replace("\n", "").replace(" ", ""));
        System.out.println("--------------------------------------------------");

        // Usando Virtual Threads (Java 21+)
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            Semaphore semaphore = new Semaphore(CONCURRENCY_LIMIT);
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            AtomicLong successCount = new AtomicLong(0);
            AtomicLong errorCount = new AtomicLong(0);

            // Dispara as threads trabalhadoras
            for (int i = 0; i < CONCURRENCY_LIMIT; i++) {
                executor.submit(() -> {
                    while (System.currentTimeMillis() < endTime) {
                        try {
                            semaphore.acquire(); // Controla a concorrência

                            HttpRequest request = HttpRequest.newBuilder()
                                    .uri(URI.create(URL))
                                    .header("Content-Type", "application/json")
                                    .POST(HttpRequest.BodyPublishers.ofString(JSON_BODY))
                                    .timeout(Duration.ofSeconds(5))
                                    .build();

                            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());

                            if (response.statusCode() > 299) {
                                long err = errorCount.incrementAndGet();
                                if (err == 1) System.err.println("🚨 ERRO HTTP: " + response.statusCode());
                            } else {
                                successCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            long err = errorCount.incrementAndGet();
                            if (err == 1) System.err.println("🚨 ERRO EXCEPTION: " + e.getMessage());
                        } finally {
                            semaphore.release();
                        }
                    }
                });
            }

            // Loop de monitoramento (Console)
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() < endTime) {
                Thread.sleep(5000);

                long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                if (elapsedSeconds == 0) elapsedSeconds = 1;

                long total = successCount.get() + errorCount.get();
                long rps = total / elapsedSeconds;

                System.out.printf("⏱️ %02d:%02d | ✅: %,d | ❌: %,d | 🚀 RPS: %,d%n",
                        elapsedSeconds / 60, elapsedSeconds % 60, successCount.get(), errorCount.get(), rps);
            }

            System.out.println("🛑 FIM DO TESTE");
            System.out.println("Total Sucessos: " + successCount.get());
        }
    }
}