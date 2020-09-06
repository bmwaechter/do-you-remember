package com.benjaminwaechter.september.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Paths;
import java.time.Duration;

/**
 * Azure Functions to make sure that everyone remembers.
 */
public class TimerTriggerFunction {

    private static final String WEBHOOK_URI = System.getenv("WebhookUri");

    private static final String FILE_NAME = "body.json";

    /**
     * Invoked every 21st night of september
     */
    @FunctionName("the-21st-night")
    public void run(
            @TimerTrigger(name = "timerInfo", schedule = "0 0 8 21 Sep *") String timerInfo,
            final ExecutionContext context
    ) throws FileNotFoundException {

        context.getLogger().info("Function triggered");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(WEBHOOK_URI))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofFile(Paths.get(FILE_NAME)))
                .build();

        HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build()
                .sendAsync(request, BodyHandlers.ofString())
                .thenApply(this::toStatusCodeResponseLog)
                .thenAccept(context.getLogger()::info);

    }

    private <T> String toStatusCodeResponseLog(HttpResponse<T> response) {
        return String.format("Completed with status code: ", response.statusCode());
    }
}
