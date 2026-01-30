package com.furever;

import com.fasterxml.jackson.core.type.TypeReference;
import com.furever.entity.*;
import com.furever.service.HttpService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeonParserServiceTest {

    @Mock
    HttpService httpService;

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream out;

    @AfterEach
    void tearDown() {
        if (out != null) System.setOut(originalOut);
    }

    @Test
    void start_fetchesSports_filtersTopLeagues_fetchesTwoMatchesAndPrints() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));

        ExecutorService directExecutor = new DirectExecutorService();
        LeonParserService service = new LeonParserService(httpService, directExecutor);

        League top = new League();
        top.setId(1970324836975744L);
        top.setName("Top League");
        top.setTop(true);

        League notTop = new League();
        notTop.setId(1L);
        notTop.setName("Not Top");
        notTop.setTop(false);

        Region region = new Region();
        region.setLeagues(List.of(top, notTop));

        SportItem sport = new SportItem();
        sport.setId(10L);
        sport.setName("Football");
        sport.setRegions(List.of(region));

        when(httpService.get(
                eq("https://leonbets.com/api-2/betline/sports?ctag=en-US&flags=urlv2"),
                ArgumentMatchers.<TypeReference<List<SportItem>>>any()
        )).thenReturn(List.of(sport));

        EventsAllResponse events = getEventsAllResponse();

        when(httpService.get(startsWith("https://leonbets.com/api-2/betline/events/all?"), eq(EventsAllResponse.class)))
                .thenReturn(events);

        Runner r = new Runner();
        r.setId(7L);
        r.setName("Home");
        r.setPrice(2.05);

        Market m = new Market();
        m.setName("1X2");
        m.setRunners(List.of(r));

        MarketsAllResponse markets = new MarketsAllResponse();
        markets.setMarkets(List.of(m));

        when(httpService.get(startsWith("https://leonbets.com/api-2/betline/event/all?"), eq(MarketsAllResponse.class)))
                .thenReturn(markets);

        service.start();

        String printed = out.toString(StandardCharsets.UTF_8);
        assertTrue(printed.contains("Football, Top League"));
        assertTrue(printed.contains("Match 1"));
        assertTrue(printed.contains("Match 2"));
        assertFalse(printed.contains("Match 3"));
        assertTrue(printed.contains("1X2"));
        assertTrue(printed.contains("Home, 2.05, 7"));
        assertTrue(printed.contains("--------------------------------------------------"));

        verify(httpService, times(1)).get(
                eq("https://leonbets.com/api-2/betline/sports?ctag=en-US&flags=urlv2"),
                ArgumentMatchers.<TypeReference<List<SportItem>>>any()
        );
        verify(httpService, times(1)).get(startsWith("https://leonbets.com/api-2/betline/events/all?"), eq(EventsAllResponse.class));
        verify(httpService, times(2)).get(startsWith("https://leonbets.com/api-2/betline/event/all?"), eq(MarketsAllResponse.class));
        verifyNoMoreInteractions(httpService);

        directExecutor.shutdown();
    }

    private static EventsAllResponse getEventsAllResponse() {
        EventDetail e1 = new EventDetail();
        e1.setId(100L);
        e1.setName("Match 1");
        e1.setKickoff(1700000000000L);

        EventDetail e2 = new EventDetail();
        e2.setId(101L);
        e2.setName("Match 2");
        e2.setKickoff(1700000000001L);

        EventDetail e3 = new EventDetail();
        e3.setId(102L);
        e3.setName("Match 3");
        e3.setKickoff(1700000000002L);

        EventsAllResponse events = new EventsAllResponse();
        events.setEvents(List.of(e1, e2, e3));
        return events;
    }

    private static final class DirectExecutorService extends java.util.concurrent.AbstractExecutorService {
        private volatile boolean terminated = false;

        @Override
        public void shutdown() {
            terminated = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            terminated = true;
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return terminated;
        }

        @Override
        public boolean isTerminated() {
            return terminated;
        }

        @Override
        public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) {
            return true;
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }
}