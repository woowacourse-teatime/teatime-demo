package com.woowacourse.teatime.auth.service;


import static com.woowacourse.teatime.teatime.domain.Role.COACH;
import static com.woowacourse.teatime.teatime.domain.Role.CREW;

import com.woowacourse.teatime.auth.controller.dto.LoginRequest;
import com.woowacourse.teatime.auth.controller.dto.UserAuthDto;
import com.woowacourse.teatime.auth.domain.UserAuthInfo;
import com.woowacourse.teatime.auth.infrastructure.JwtTokenProvider;
import com.woowacourse.teatime.teatime.controller.dto.request.ReservationApproveRequest;
import com.woowacourse.teatime.teatime.controller.dto.request.ReservationReserveRequest;
import com.woowacourse.teatime.teatime.controller.dto.request.SheetQuestionUpdateRequest;
import com.woowacourse.teatime.teatime.domain.Coach;
import com.woowacourse.teatime.teatime.domain.Crew;
import com.woowacourse.teatime.teatime.domain.Reservation;
import com.woowacourse.teatime.teatime.domain.Schedule;
import com.woowacourse.teatime.teatime.exception.NotFoundReservationException;
import com.woowacourse.teatime.teatime.repository.CoachRepository;
import com.woowacourse.teatime.teatime.repository.CrewRepository;
import com.woowacourse.teatime.teatime.repository.ReservationRepository;
import com.woowacourse.teatime.teatime.repository.ScheduleRepository;
import com.woowacourse.teatime.teatime.service.QuestionService;
import com.woowacourse.teatime.teatime.service.ReservationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class LoginService {

    private static final String DEFAULT_COACH_IMAGE = "https://user-images.githubusercontent.com/52141636/196610751-f761f99d-8305-448a-81bf-3c920a0870b1.png";
    private static final String DEFAULT_CREW_IMAGE = "https://avatars.slack-edge.com/2022-09-16/4091280804290_59b52a871e8a942a7969_192.png";
    private static final String DEFAULT_SUFFIX_EMAIL = "@email.com";
    private static final String DEFAULT_QUESTION_1 = "이번 면담을 통해 논의하고 싶은 내용";
    private static final String DEFAULT_QUESTION_2 = "최근에 자신이 긍정적으로 보는 시도와 변화";
    private static final String DEFAULT_QUESTION_3 = "이번 면담을 통해 생기기를 원하는 변화";

    private final CrewRepository crewRepository;
    private final CoachRepository coachRepository;
    private final ScheduleRepository scheduleRepository;
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final QuestionService questionService;
    private final UserAuthService userAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    public UserAuthDto login(LoginRequest loginRequest) {
        String name = loginRequest.getName();
        String role = loginRequest.getRole();

        Coach coach = coachRepository.findByName(name)
                .orElseGet(() -> saveCoachAndDefaultQuestions(name));
        Crew crew = crewRepository.findByName(name)
                .orElseGet(() -> saveCrew(name));

        if (role.equals("COACH")) {
            return getCoachLoginResponse(coach);
        }
        return getCrewLoginResponse(crew);
    }

    private UserAuthDto getCoachLoginResponse(Coach coach) {
        Map<String, Object> claims = Map.of("id", coach.getId(), "role", COACH);
        String accessToken = jwtTokenProvider.createToken(claims);
        String refreshToken = UUID.randomUUID().toString();
        userAuthService.save(new UserAuthInfo(refreshToken, accessToken, coach.getId(), COACH.name()));
        return new UserAuthDto(accessToken, refreshToken, COACH, coach.getImage(), coach.getName());
    }

    @NotNull
    private Coach saveCoachAndDefaultQuestions(String name) {
        Coach coach = coachRepository.save(new Coach(
                "UXXX01B38BC",
                name,
                name + DEFAULT_SUFFIX_EMAIL,
                DEFAULT_COACH_IMAGE));

        List<SheetQuestionUpdateRequest> defaultQuestionDtos = List.of(
                new SheetQuestionUpdateRequest(1, DEFAULT_QUESTION_1, true),
                new SheetQuestionUpdateRequest(2, DEFAULT_QUESTION_2, true),
                new SheetQuestionUpdateRequest(3, DEFAULT_QUESTION_3, true));

        questionService.update(coach.getId(), defaultQuestionDtos);

        // 더미스케줄 저장
        LocalDateTime now = LocalDateTime.now();
        LocalDate tomorrow = now.plusDays(1).toLocalDate();
        LocalDate yesterday = now.minusDays(1).toLocalDate();
        Schedule schedule1 = scheduleRepository.save(new Schedule(coach, LocalDateTime.of(tomorrow, LocalTime.of(9, 0))));
        Schedule schedule2 = scheduleRepository.save(new Schedule(coach, LocalDateTime.of(tomorrow, LocalTime.of(10, 0))));
        Schedule schedule3 = scheduleRepository.save(new Schedule(coach, LocalDateTime.of(yesterday, LocalTime.of(11, 0))));

        Long reservationId1 = reservationService.save(629891L, new ReservationReserveRequest(schedule1.getId()));
        Long reservationId2 = reservationService.save(629892L, new ReservationReserveRequest(schedule2.getId()));
        Long reservationId3 = reservationService.save(629893L, new ReservationReserveRequest(schedule3.getId()));

        reservationService.approve(coach.getId(), reservationId2, new ReservationApproveRequest(true));
        reservationService.approve(coach.getId(), reservationId3, new ReservationApproveRequest(true));

        Reservation reservation = reservationRepository.findById(reservationId3)
                .orElseThrow(NotFoundReservationException::new);
        reservation.updateReservationStatusToInProgress();

        return coach;
    }

    private UserAuthDto getCrewLoginResponse(Crew crew) {
        Map<String, Object> claims = Map.of("id", crew.getId(), "role", CREW);
        String accessToken = jwtTokenProvider.createToken(claims);
        String refreshToken = UUID.randomUUID().toString();
        userAuthService.save(new UserAuthInfo(refreshToken, accessToken, crew.getId(), CREW.name()));
        return new UserAuthDto(accessToken, refreshToken, CREW, crew.getImage(), crew.getName());
    }

    @NotNull
    private Crew saveCrew(String name) {
        Crew crew = new Crew(
                "UXXX01B38BC",
                name,
                name + DEFAULT_SUFFIX_EMAIL,
                DEFAULT_CREW_IMAGE);
        return crewRepository.save(crew);
    }
}
