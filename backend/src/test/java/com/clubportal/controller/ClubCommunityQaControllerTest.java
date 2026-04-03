package com.clubportal.controller;

import com.clubportal.dto.ClubCommunityAnswerCreateRequest;
import com.clubportal.dto.ClubCommunityAnswerResponse;
import com.clubportal.dto.ClubCommunityQuestionBoardResponse;
import com.clubportal.dto.ClubCommunityQuestionCreateRequest;
import com.clubportal.dto.ClubCommunityQuestionResponse;
import com.clubportal.service.ClubCommunityQaService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClubCommunityQaControllerTest {

    @Test
    void getBoardReturnsOk() {
        ClubCommunityQaService service = mock(ClubCommunityQaService.class);
        ClubCommunityQuestionBoardResponse board = new ClubCommunityQuestionBoardResponse(
                7,
                true,
                true,
                false,
                1,
                List.of(new ClubCommunityQuestionResponse(
                        12,
                        9,
                        "alice",
                        "Is parking available?",
                        false,
                        0,
                        true,
                        true,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        List.of()
                ))
        );
        when(service.getBoard(7)).thenReturn(board);

        ClubCommunityQaController controller = new ClubCommunityQaController(service);
        ResponseEntity<?> response = controller.getBoard(7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ClubCommunityQuestionBoardResponse.class, response.getBody());
        assertEquals(1, ((ClubCommunityQuestionBoardResponse) response.getBody()).questionCount());
        assertEquals("private, no-store, max-age=0", response.getHeaders().getFirst("Cache-Control"));
        assertEquals("no-cache", response.getHeaders().getFirst("Pragma"));
    }

    @Test
    void askQuestionReturnsCreated() {
        ClubCommunityQaService service = mock(ClubCommunityQaService.class);
        ClubCommunityQuestionResponse question = new ClubCommunityQuestionResponse(
                15,
                9,
                "alice",
                "Do you offer parking nearby?",
                false,
                0,
                true,
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of()
        );
        when(service.askQuestion(4, new ClubCommunityQuestionCreateRequest("Do you offer parking nearby?"))).thenReturn(question);

        ClubCommunityQaController controller = new ClubCommunityQaController(service);
        ResponseEntity<?> response = controller.askQuestion(4, new ClubCommunityQuestionCreateRequest("Do you offer parking nearby?"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(ClubCommunityQuestionResponse.class, response.getBody());
        assertEquals("Do you offer parking nearby?", ((ClubCommunityQuestionResponse) response.getBody()).questionText());
    }

    @Test
    void answerQuestionReturnsCreated() {
        ClubCommunityQaService service = mock(ClubCommunityQaService.class);
        ClubCommunityAnswerResponse answer = new ClubCommunityAnswerResponse(
                99,
                22,
                "manba basketball",
                "CLUB",
                "Yes, parking is available nearby.",
                true,
                true,
                LocalDateTime.now()
        );
        when(service.answerQuestion(4, 15, new ClubCommunityAnswerCreateRequest("Yes, parking is available nearby."))).thenReturn(answer);

        ClubCommunityQaController controller = new ClubCommunityQaController(service);
        ResponseEntity<?> response = controller.answerQuestion(4, 15, new ClubCommunityAnswerCreateRequest("Yes, parking is available nearby."));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(ClubCommunityAnswerResponse.class, response.getBody());
        assertEquals("CLUB", ((ClubCommunityAnswerResponse) response.getBody()).responderType());
    }

    @Test
    void updateAndDeleteRoutesReturnSuccess() {
        ClubCommunityQaService service = mock(ClubCommunityQaService.class);
        ClubCommunityQuestionResponse question = new ClubCommunityQuestionResponse(
                15,
                9,
                "alice",
                "Updated question",
                false,
                0,
                true,
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of()
        );
        ClubCommunityAnswerResponse answer = new ClubCommunityAnswerResponse(
                88,
                22,
                "manba basketball",
                "CLUB",
                "Updated answer",
                true,
                true,
                LocalDateTime.now()
        );
        when(service.updateQuestion(4, 15, new ClubCommunityQuestionCreateRequest("Updated question"))).thenReturn(question);
        when(service.updateAnswer(4, 15, 88, new ClubCommunityAnswerCreateRequest("Updated answer"))).thenReturn(answer);

        ClubCommunityQaController controller = new ClubCommunityQaController(service);

        ResponseEntity<?> updateQuestionResponse = controller.updateQuestion(4, 15, new ClubCommunityQuestionCreateRequest("Updated question"));
        ResponseEntity<?> updateAnswerResponse = controller.updateAnswer(4, 15, 88, new ClubCommunityAnswerCreateRequest("Updated answer"));
        ResponseEntity<?> deleteQuestionResponse = controller.deleteQuestion(4, 15);
        ResponseEntity<?> deleteAnswerResponse = controller.deleteAnswer(4, 15, 88);

        assertEquals(HttpStatus.OK, updateQuestionResponse.getStatusCode());
        assertEquals(HttpStatus.OK, updateAnswerResponse.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, deleteQuestionResponse.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, deleteAnswerResponse.getStatusCode());
    }

    @Test
    void serviceErrorsAreMappedToStatusCode() {
        ClubCommunityQaService service = mock(ClubCommunityQaService.class);
        when(service.answerQuestion(4, 15, new ClubCommunityAnswerCreateRequest("No"))).thenThrow(
                new ResponseStatusException(HttpStatus.FORBIDDEN, "Only club staff or booked members can answer")
        );

        ClubCommunityQaController controller = new ClubCommunityQaController(service);
        ResponseEntity<?> response = controller.answerQuestion(4, 15, new ClubCommunityAnswerCreateRequest("No"));

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Only club staff or booked members can answer", response.getBody());
    }
}
