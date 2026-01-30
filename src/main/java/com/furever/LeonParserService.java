package com.furever;

import com.fasterxml.jackson.core.type.TypeReference;
import com.furever.entity.*;
import com.furever.service.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LeonParserService {

    private final ExecutorService customExecutor;
    private final HttpService httpService;
    private static final Logger logger = LoggerFactory.getLogger(LeonParserService.class);
    private static final Set<String> TARGET_SPORTS = Set.of(
            "Football",
            "Tennis",
            "Ice Hockey",
            "Basketball"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'")
            .withZone(ZoneId.of("UTC"));

    public LeonParserService(HttpService httpService, ExecutorService executor) {
        this.httpService = httpService;
        this.customExecutor = executor;
    }

    public static void main(String[] args) {
        ExecutorService realExecutor = Executors.newFixedThreadPool(3);
        HttpService realHttp = new HttpService();

        LeonParserService parser = new LeonParserService(realHttp, realExecutor);
        try {
            parser.start();
        } finally {
            realExecutor.shutdown();
        }
    }

    public void start() {
        logger.info("Fetching all sport names...");
        List<SportItem> targetSports = getAllSportItems().stream()
                .filter(sport -> TARGET_SPORTS.contains(sport.getName()))
                .toList();

        for (SportItem sportItem : targetSports) {

            List<League> topLeagues = fetchTopLeagues(sportItem);

            List<CompletableFuture<Void>> futures = topLeagues.stream()
                    .map(league -> CompletableFuture.runAsync(() ->
                            processLeague(sportItem.getName(), league), customExecutor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    private void processLeague(String sportName, League league) {
        try {
            String url = String.format(
                    "https://leonbets.com/api-2/betline/events/all?ctag=en-US&league_id=%s&hideClosed=true&flags=reg,urlv2,orn2,mm2,rrc,nodup",
                    league.getId()
            );

            EventsAllResponse response = httpService.get(url, EventsAllResponse.class);
            List<EventDetail> allMatches = response.getEvents() != null ? response.getEvents() : List.of();

            List<EventDetail> targetMatches = allMatches.stream()
                    .limit(2)
                    .toList();

            for (EventDetail match : targetMatches) {
                processMatchDetail(sportName, league.getName(), match);
            }
        } catch (Exception e) {
            logger.error("Error processing league {}", league.getName(), e);
        }
    }

    private void processMatchDetail(String sportName, String leagueName, EventDetail basicMatch) {
        try {
            String url = String.format(
                    "https://leonbets.com/api-2/betline/event/all?ctag=en-US&eventId=%s&flags=reg,urlv2,orn2,mm2,rrc,nodup,smgv2,outv2,wd3",
                    basicMatch.getId()
            );

            MarketsAllResponse response = httpService.get(url, MarketsAllResponse.class);
            List<Market> allMarkets = response.getMarkets() != null ? response.getMarkets() : List.of();

            printPrettyOutput(sportName, leagueName, basicMatch, allMarkets);

        } catch (Exception e) {
            logger.error("Error processing match {}", basicMatch.getId(), e);
        }
    }

    private synchronized void printPrettyOutput(String sport, String league, EventDetail match, List<Market> markets) {
        StringBuilder sb = new StringBuilder();

        sb.append(sport).append(", ").append(league).append("\n");

        String dateStr = DATE_FORMATTER.format(Instant.ofEpochMilli(match.getKickoff()));
        sb.append(match.getName())
                .append(", ")
                .append(dateStr)
                .append(", ")
                .append(match.getId())
                .append("\n");

        for (Market market : markets) {
            sb.append(market.getName()).append("\n");

            if (market.getRunners() != null) {
                for (Runner runner : market.getRunners()) {
                    sb.append(runner.getName())
                            .append(", ")
                            .append(runner.getPrice())
                            .append(", ")
                            .append(runner.getId())
                            .append("\n");
                }
            }
        }
        sb.append("--------------------------------------------------\n");

        System.out.println(sb);
    }


    private List<SportItem> getAllSportItems() {
        return httpService.get(
                "https://leonbets.com/api-2/betline/sports?ctag=en-US&flags=urlv2",
                new TypeReference<List<SportItem>>() {}
        );
    }

    private List<League> fetchTopLeagues(SportItem sportItem) {
        if (sportItem.getRegions() == null) return List.of();

        return sportItem.getRegions().stream()
                .flatMap(region -> region.getLeagues().stream())
                .filter(League::isTop)
                .toList();
    }
}
